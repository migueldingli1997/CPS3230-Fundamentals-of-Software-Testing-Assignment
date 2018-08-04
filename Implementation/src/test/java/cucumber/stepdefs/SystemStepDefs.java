package cucumber.stepdefs;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.junit.Assume;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import static webapp.StartJettyHandler.PORT_NUMBER;

public class SystemStepDefs {

    private static final int LOGIN_KEY_LENGTH = 10;

    private static final String baseUrl = "localhost:" + PORT_NUMBER;
    private static final String AGENT_ID = "1234xy";
    private static final String OTHER_AGENT_ID = "4567ab";
    private WebDriver driver;

    /**
     * Registers the agent. No checks are done if this was successful
     *
     * @param driver  the WebDriver to use
     * @param agentId the agent id to login
     */
    private static void registerAgent(WebDriver driver, String agentId) {
        driver.get(baseUrl + "/register");
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
    private static void loginAgent(WebDriver driver, String loginKey) {
        driver.findElement(By.id("lKeyInput")).click();
        driver.findElement(By.id("lKeyInput")).sendKeys(loginKey);
        driver.findElement(By.id("submit")).click();
    }

    /**
     * Goes to the read message page on the specified WebDriver. No checks are done.
     *
     * @param driver the WebDriver to use
     */
    private static void gotoReadMessagePage(WebDriver driver) {
        driver.findElement(By.id("consumeMessage")).click();
    }

    /**
     * Goes to the send message page on the specified WebDriver. No checks are done.
     *
     * @param driver the WebDriver to use
     */
    private static void gotoSendMessagePage(WebDriver driver) {
        driver.findElement(By.id("sendMessage")).click();
    }

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "chromedriver");
        driver = new ChromeDriver();
    }

    @After
    public void tearDown() {
        driver.quit();
    }

    @Given("^I am an agent trying to log in$")
    public void i_am_an_agent_trying_to_log_in() {
        driver.get(baseUrl + "/register");
    }

    @When("^I obtain a key from the supervisor using a valid id$")
    public void i_obtain_a_key_from_the_supervisor_using_a_valid_id() {
        Assume.assumeTrue(driver.getCurrentUrl().endsWith(baseUrl + "/register"));

        driver.findElement(By.id("idInput")).click();
        driver.findElement(By.id("idInput")).sendKeys(AGENT_ID);
        driver.findElement(By.id("submit")).click();
    }

    @Then("^the supervisor should give me a valid key$")
    public void the_supervisor_should_give_me_a_valid_key() {
        Assume.assumeTrue(driver.getCurrentUrl().endsWith(baseUrl + "/login"));

        final String loginKey = driver.findElement(By.id("lKey")).getText();
        Assert.assertTrue(loginKey.length() == LOGIN_KEY_LENGTH);
    }

    @When("^I log in using that key$")
    public void i_log_in_using_that_key() {
        Assume.assumeTrue(driver.getCurrentUrl().endsWith(baseUrl + "/login"));

        final String loginKey = driver.findElement(By.id("lKey")).getText();
        loginAgent(driver, loginKey);
    }

    @Then("^I should be allowed to log in$")
    public void i_should_be_allowed_to_log_in() {
        System.out.println(driver.getCurrentUrl());
        Assert.assertTrue(driver.getCurrentUrl().endsWith(baseUrl + "/loggedin"));
    }

    @When("^I wait for (\\d+) seconds$")
    public void i_wait_for_seconds(int seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000);
    }

    @Then("^I should not be allowed to log in$")
    public void i_should_not_be_allowed_to_log_in() {
        Assert.assertTrue(driver.getCurrentUrl().endsWith(baseUrl + "/login"));
    }

    @Given("^I am a logged in agent$")
    public void i_am_a_logged_in_agent() {
        registerAgent(driver, AGENT_ID);
        Assume.assumeTrue(driver.getCurrentUrl().endsWith(baseUrl + "/login"));

        final String loginKey = driver.findElement(By.id("lKey")).getText();
        loginAgent(driver, loginKey);
    }

    @When("^I attempt to send (\\d+) messages$")
    public void i_attempt_to_send_messages(int arg1) {
        gotoSendMessagePage(driver);
        Assume.assumeTrue(driver.getCurrentUrl().endsWith(baseUrl + "/sendmessage"));

        for (int i = 0; i < arg1; i++) {
            driver.findElement(By.id("destination")).click();
            driver.findElement(By.id("destination")).sendKeys(AGENT_ID);
            driver.findElement(By.id("messageBody")).click();
            driver.findElement(By.id("messageBody")).sendKeys("message_" + i);
            driver.findElement(By.id("submit")).click();
        }
    }

    @Then("^the messages should be successfully sent$")
    public void the_messages_should_be_successfully_sent() {
        final String notificationText = driver.findElement(By.id("notif")).getText();
        Assert.assertTrue(notificationText.equals("Message sent successfully."));
    }

    @When("^I try to send another message$")
    public void i_try_to_send_another_message() {
        Assume.assumeTrue(driver.getCurrentUrl().endsWith(baseUrl + "/sendmessage"));

        driver.findElement(By.id("destination")).click();
        driver.findElement(By.id("destination")).sendKeys(AGENT_ID);
        driver.findElement(By.id("messageBody")).click();
        driver.findElement(By.id("messageBody")).sendKeys("Another message");
        driver.findElement(By.id("submit")).click();
    }

    @Then("^the system will inform me that I have exceeded my quota$")
    public void the_system_will_inform_me_that_I_have_exceeded_my_quota() {
        final String notificationText = driver.findElement(By.id("notif")).getText();

        Assert.assertTrue(notificationText.equals("You were logged out due to an exceeded quota."));
    }

    @When("^I attempt to send the message (.*) to another agent$")
    public void iAttemptToSendTheMessageMessageToAnotherAgent(String message) {
        final ChromeDriver driver2 = new ChromeDriver();
        registerAgent(driver2, OTHER_AGENT_ID); // register the recipient
        driver2.quit();

        Assume.assumeTrue(driver.getCurrentUrl().endsWith(baseUrl + "/loggedin"));
        gotoSendMessagePage(driver);
        Assume.assumeTrue(driver.getCurrentUrl().endsWith(baseUrl + "/sendmessage"));

        driver.findElement(By.id("destination")).click();
        driver.findElement(By.id("destination")).sendKeys(OTHER_AGENT_ID);
        driver.findElement(By.id("messageBody")).click();
        driver.findElement(By.id("messageBody")).sendKeys(message);
        driver.findElement(By.id("submit")).click();
    }

    @Then("^the other agent should receive the message (.*)$")
    public void theOtherAgentShouldReceiveTheMessageNewMessage(String message) {
        final WebDriver driver2 = new ChromeDriver();
        registerAgent(driver2, OTHER_AGENT_ID);
        Assume.assumeTrue(driver2.getCurrentUrl().endsWith(baseUrl + "/login"));

        final String loginKey = driver2.findElement(By.id("lKey")).getText();
        loginAgent(driver2, loginKey);
        Assume.assumeTrue(driver2.getCurrentUrl().endsWith(baseUrl + "/loggedin"));

        // From here on, only assertions since they form part of what is tested

        gotoReadMessagePage(driver2);
        Assume.assumeTrue(driver2.getCurrentUrl().endsWith(baseUrl + "/readmessage"));

        Assert.assertEquals(AGENT_ID, driver2.findElement(By.id("from")).getText());
        Assert.assertEquals(OTHER_AGENT_ID, driver2.findElement(By.id("to")).getText());
        Assert.assertEquals(message, driver2.findElement(By.id("message")).getText());
        driver2.quit();
    }

    @When("^I click on “Log out”$")
    public void i_click_on_Log_out() {
        Assume.assumeTrue(driver.getCurrentUrl().endsWith(baseUrl + "/loggedin"));

        driver.findElement(By.id("logout")).click();
    }

    @Then("^I should be logged out$")
    public void i_should_be_logged_out() {
        Assert.assertTrue(driver.getCurrentUrl().endsWith(baseUrl + "/register"));
    }
}
