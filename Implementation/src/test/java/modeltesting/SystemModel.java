package modeltesting;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.FsmModel;
import org.junit.Assert;
import org.junit.Assume;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import util.Utils;

public class SystemModel implements FsmModel {

    private static final String BASE_URL = "localhost:" + webapp.StartJettyHandler.PORT_NUMBER;
    private static final int MAX_MESSAGE_LENGTH = 140;
    private static final int MAX_MESSAGES_SENT = 25;
    private static final int MAX_MESSAGE_RECEIVED = 25;

    private WebDriver driver1 = null;
    private final WebDriver driver2 = new ChromeDriver();

    private SystemModelState currentState = SystemModelState.UNREGISTERED;

    private String agentID;
    private int messagesSentInThisSession;
    private int messagesRecvInThisSession;
    private int messagesInMailbox;

    private boolean agentRegisteredInSystem;
    private boolean agentWasAutoLoggedOut;

    private long uniqueness = 0;

    @Action
    public void normalRegister() {
        agentWasAutoLoggedOut = false;

        registerAgentHelper(driver1, agentID);
        Assert.assertTrue(driver1.getCurrentUrl().endsWith(BASE_URL + "/login"));
        currentState = SystemModelState.REGISTERED;
    }

    public boolean normalRegisterGuard() {
        return currentState == SystemModelState.UNREGISTERED;
    }

    @Action
    public void spyRegister() {
        registerAgentHelper(driver1, "spy-" + Utils.getNRandomCharacters(5));
        Assert.assertTrue(driver1.getCurrentUrl().endsWith(BASE_URL + "/register"));
        currentState = SystemModelState.UNREGISTERED;
    }

    public boolean spyRegisterGuard() {
        return currentState == SystemModelState.UNREGISTERED;
    }

    @Action
    public void validLoginKeyLogin() {
        agentWasAutoLoggedOut = false;

        loginAgentHelper(driver1, driver1.findElement(By.id("lKey")).getText());
        Assert.assertTrue(driver1.getCurrentUrl().endsWith(BASE_URL + "/loggedin"));
        agentRegisteredInSystem = true;
        messagesSentInThisSession = 0;
        messagesRecvInThisSession = 0;
        currentState = SystemModelState.LOGGED_IN;
    }

    public boolean validLoginKeyLoginGuard() {
        return currentState == SystemModelState.REGISTERED;
    }

    @Action
    public void invalidLoginKeyLogin() {
        loginAgentHelper(driver1, "invalidLoginKey");
        Assert.assertTrue(driver1.getCurrentUrl().endsWith(BASE_URL + "/login"));
        currentState = SystemModelState.REGISTERED;
    }

    public boolean invalidLoginKeyLoginGuard() {
        return currentState == SystemModelState.REGISTERED;
    }

    @Action
    public void gotoSendMessagePage() {
        driver1.findElement(By.id("sendMessage")).click();

        if (common_checkAgentWasNotAutoLoggedOut(driver1)) {
            Assert.assertTrue(driver1.getCurrentUrl().endsWith(BASE_URL + "/sendmessage"));
            currentState = SystemModelState.SENDING_MESSAGE;
        }
    }

    public boolean gotoSendMessagePageGuard() {
        return currentState == SystemModelState.LOGGED_IN;
    }

    @Action
    public void sendNormalMessage() {
        sendMessageHelper(driver1, agentID, "Msg");

        if (messagesRecvInThisSession >= MAX_MESSAGE_RECEIVED) {
            messagesSentInThisSession = 0;
            messagesRecvInThisSession = 0;
            agentWasAutoLoggedOut = true;
        }
        common_checkThatSuccessfulOrAgentLoggedOut();
    }

    public boolean sendNormalMessageGuard() {
        return currentState == SystemModelState.SENDING_MESSAGE;
    }

    @Action
    public void sendLongMessage() {
        sendMessageHelper(driver1, agentID, Utils.getNCharacters(MAX_MESSAGE_LENGTH + 10));

        // Note: agent cannot be logged out since no message was sent
        // but may have still been logged out due to receiveMessage

        if (common_checkAgentWasNotAutoLoggedOut(driver1)) {
            Assert.assertTrue(driver1.getCurrentUrl().endsWith(BASE_URL + "/sendmessage"));
            final String notificationText = driver1.findElement(By.id("notif")).getText();
            Assert.assertTrue(notificationText.equals("Message not sent since it is longer than 140 characters."));
            currentState = SystemModelState.SENDING_MESSAGE;
        }
    }

    public boolean sendLongMessageGuard() {
        return currentState == SystemModelState.SENDING_MESSAGE;
    }

    @Action
    public void sendMessageWithBlockedWords() {
        sendMessageHelper(driver1, agentID, "Get the nuclEAR REcipe with GinGer");

        if (messagesRecvInThisSession >= MAX_MESSAGE_RECEIVED) {
            messagesSentInThisSession = 0;
            messagesRecvInThisSession = 0;
            agentWasAutoLoggedOut = true;
        }
        common_checkThatSuccessfulOrAgentLoggedOut();
    }

    public boolean sendMessageWithBlockedWordsGuard() {
        return currentState == SystemModelState.SENDING_MESSAGE;
    }

    @Action
    public void sendMessageToNonExistentTarget() {
        // "AGENT_2" prefix so that it doesn't clash with primary agent's id
        final String AGENT_2 = "AGENT_2_" + Utils.getNRandomCharacters(5);
        sendMessageHelper(driver1, AGENT_2, "Msg");

        // Note: agent cannot be logged out since no message was sent
        // but may have still been logged out due to receiveMessage

        if (common_checkAgentWasNotAutoLoggedOut(driver1)) {
            Assert.assertTrue(driver1.getCurrentUrl().endsWith(BASE_URL + "/sendmessage"));
            final String notificationText = driver1.findElement(By.id("notif")).getText();
            Assert.assertTrue(notificationText.equals("Message not sent since the target agent does not exist."));
            currentState = SystemModelState.SENDING_MESSAGE;
        }
    }

    public boolean sendMessageToNonExistentTargetGuard() {
        return currentState == SystemModelState.SENDING_MESSAGE;
    }

    @Action
    public void gotoReadMessagePage() {
        driver1.findElement(By.id("consumeMessage")).click();

        if (common_checkAgentWasNotAutoLoggedOut(driver1)) {
            Assert.assertTrue(driver1.getCurrentUrl().endsWith(BASE_URL + "/readmessage"));
            currentState = SystemModelState.READING_MESSAGE;
        }
    }

    public boolean gotoReadMessagePageGuard() {
        return currentState == SystemModelState.LOGGED_IN;
    }

    @Action
    public void consumeAnotherMessage() {
        driver1.findElement(By.id("consume")).click();

        if (common_checkAgentWasNotAutoLoggedOut(driver1)) {
            Assert.assertTrue(driver1.getCurrentUrl().endsWith(BASE_URL + "/readmessage"));
            currentState = SystemModelState.READING_MESSAGE;
        }
    }

    public boolean consumeAnotherMessageGuard() {
        return currentState == SystemModelState.HAS_READ_MESSAGE;
    }

    @Action
    public void consumeMessage() {
        Assert.assertTrue(driver1.getCurrentUrl().endsWith(BASE_URL + "/readmessage"));

        if (messagesInMailbox == 0) {
            Assert.assertEquals("You have no new messages.", driver1.findElement(By.id("messageContainer")).getText());
            Assert.assertEquals("Try again", driver1.findElement(By.id("consume")).getText());
        } else {
            Assert.assertNotEquals("You have no new messages.", driver1.findElement(By.id("messageContainer")).getText());
            Assert.assertEquals("Consume another message", driver1.findElement(By.id("consume")).getText());
            messagesInMailbox--;
        }
        currentState = SystemModelState.HAS_READ_MESSAGE;
    }

    public boolean consumeMessageGuard() {
        return currentState == SystemModelState.READING_MESSAGE;
    }

    @Action
    public void goBack() {
        driver1.findElement(By.id("backToMailbox")).click();

        if (common_checkAgentWasNotAutoLoggedOut(driver1)) {
            Assert.assertTrue(driver1.getCurrentUrl().endsWith(BASE_URL + "/loggedin"));
            currentState = SystemModelState.LOGGED_IN;
        }
    }

    public boolean goBackGuard() {
        return currentState == SystemModelState.SENDING_MESSAGE
                || currentState == SystemModelState.HAS_READ_MESSAGE;
    }

    @Action
    public void manualLogout() {
        driver1.findElement(By.id("logout")).click();
        Assert.assertTrue(driver1.getCurrentUrl().endsWith(BASE_URL + "/register"));
        messagesSentInThisSession = 0;
        messagesRecvInThisSession = 0;
        currentState = SystemModelState.UNREGISTERED;
    }

    public boolean manualLogoutGuard() {
        return currentState == SystemModelState.LOGGED_IN;
    }

    @Action
    public void receiveMessage() {
        // Sender registers ("AGENT_2" prefix for no clashes)
        final String AGENT_2 = "AGENT_2_" + Utils.getNRandomCharacters(5);
        registerAgentHelper(driver2, AGENT_2);

        // Sender logs in and sends message
        loginAgentHelper(driver2, driver2.findElement(By.id("lKey")).getText());
        Assume.assumeTrue(driver2.getCurrentUrl().endsWith(BASE_URL + "/loggedin"));
        driver2.findElement(By.id("sendMessage")).click();
        Assume.assumeTrue(driver2.getCurrentUrl().endsWith(BASE_URL + "/sendmessage"));
        sendMessageHelper(driver2, agentID, "Msg " + messagesRecvInThisSession);

        final String notificationText = driver2.findElement(By.id("notif")).getText();
        if (messagesRecvInThisSession < MAX_MESSAGE_RECEIVED) {
            Assert.assertTrue(notificationText.equals("Message sent successfully."));
            messagesRecvInThisSession++;
            messagesInMailbox++;
        } else {
            Assert.assertTrue(notificationText.equals("Message not sent since target agent's quota exceeded."));
            messagesSentInThisSession = 0;
            messagesRecvInThisSession = 0;
            agentWasAutoLoggedOut = true;
        }
        // current state does not change
    }

    public boolean receiveMessageGuard() {
        return agentRegisteredInSystem && currentState != SystemModelState.READING_MESSAGE;
    }

    @Override
    public Object getState() {
        return currentState;
    }

    @Override
    public void reset(boolean driverReset) {
        currentState = SystemModelState.UNREGISTERED;
        agentID = (uniqueness++) + "_" + Utils.getNRandomCharacters(5);
        messagesSentInThisSession = 0;
        messagesRecvInThisSession = 0;
        messagesInMailbox = 0;
        agentRegisteredInSystem = false;
        agentWasAutoLoggedOut = false;

        if (driverReset) {
            if (driver1 != null) driver1.quit();
            driver1 = new ChromeDriver();
        }
    }

    private boolean common_checkAgentWasNotAutoLoggedOut(WebDriver driver) {
        if (!agentWasAutoLoggedOut) {
            return true;
        } else {
            Assert.assertTrue(driver.getCurrentUrl().endsWith(BASE_URL + "/register"));
            currentState = SystemModelState.UNREGISTERED;
            return false;
        }
    }

    private void common_checkThatSuccessfulOrAgentLoggedOut() {
        if (messagesSentInThisSession >= MAX_MESSAGES_SENT || agentWasAutoLoggedOut) {
            Assert.assertTrue(driver1.getCurrentUrl().endsWith(BASE_URL + "/register"));
            currentState = SystemModelState.UNREGISTERED;
        } else {
            Assert.assertTrue(driver1.getCurrentUrl().endsWith(BASE_URL + "/sendmessage"));
            final String notificationText = driver1.findElement(By.id("notif")).getText();
            Assert.assertTrue(notificationText.equals("Message sent successfully."));
            messagesSentInThisSession++;
            messagesRecvInThisSession++;
            messagesInMailbox++;
            currentState = SystemModelState.SENDING_MESSAGE;
        }
    }

    /**
     * Registers the agent. No checks are done if this was successful
     *
     * @param driver  the WebDriver to use
     * @param agentId the agent id to login
     */
    private static void registerAgentHelper(WebDriver driver, String agentId) {
        driver.get(BASE_URL + "/register");
        Assume.assumeTrue(driver.getCurrentUrl().endsWith(BASE_URL + "/register"));

        driver.findElement(By.id("idInput")).click();
        driver.findElement(By.id("idInput")).sendKeys(agentId);
        driver.findElement(By.id("submit")).click();
    }

    /**
     * Logs in the agent on the specified WebDriver. No checks are done.
     *
     * @param driver   the WebDriver to use
     * @param loginKey the loginKey to use
     */
    private static void loginAgentHelper(WebDriver driver, String loginKey) {
        Assume.assumeTrue(driver.getCurrentUrl().endsWith(BASE_URL + "/login"));

        driver.findElement(By.id("lKeyInput")).click();
        driver.findElement(By.id("lKeyInput")).sendKeys(loginKey);
        driver.findElement(By.id("submit")).click();
    }

    /**
     * Sends a message to the target agent. No checks are done
     *
     * @param driver        the WebDriver to use
     * @param targetAgentId the target agent's id
     * @param message       the message to send
     */
    private static void sendMessageHelper(WebDriver driver, String targetAgentId, String message) {
        Assume.assumeTrue(driver.getCurrentUrl().endsWith(BASE_URL + "/sendmessage"));

        driver.findElement(By.id("destination")).click();
        driver.findElement(By.id("destination")).sendKeys(targetAgentId);
        driver.findElement(By.id("messageBody")).click();
        driver.findElement(By.id("messageBody")).sendKeys(message);
        driver.findElement(By.id("submit")).click();
    }

    public void quitWebDrivers() {
        if (driver1 != null) driver1.quit();
        if (driver2 != null) driver2.quit();
    }
}