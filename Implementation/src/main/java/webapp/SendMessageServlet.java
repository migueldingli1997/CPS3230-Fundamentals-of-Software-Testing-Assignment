package webapp;

import system.MessagingSystem;
import system.StatusCodes;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet implementation class FirstServlet
 */
@WebServlet("/sendmessage")
class SendMessageServlet extends HttpServlet {

    private final MessagingSystem messagingSystem;

    SendMessageServlet(MessagingSystem messagingSystem) {
        this.messagingSystem = messagingSystem;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/html");

        final Cookie idCookie = Utils.findCookie(request.getCookies(), CookieNames.AGENT_ID.name());
        final Cookie skCookie = Utils.findCookie(request.getCookies(), CookieNames.SESSION_KEY.name());

        if (idCookie == null || skCookie == null) {
            response.sendRedirect("/register");
        } else if (!messagingSystem.agentLoggedIn(idCookie.getValue())) {
            Utils.deleteCookie(idCookie, response);
            Utils.deleteCookie(skCookie, response);
            response.addCookie(new Cookie(CookieNames.LOGGED_OUT_STATUS.name(), "You_were_logged_out_of_the_system."));
            response.sendRedirect("/register");
        } else {
            String sendingMessageStatusText = "";
            final Cookie statusCookie = Utils.findCookie(request.getCookies(), CookieNames.MESSAGE_SENDING_STATUS.name());
            if (statusCookie != null) {

                // Get cookie value and delete the cookie
                final String statusValue = statusCookie.getValue();
                Utils.deleteCookie(statusCookie, response);

                // Set the status message
                if (statusValue.equals(StatusCodes.OK.name())) {
                    sendingMessageStatusText = "Message sent successfully.";
                } else if (statusValue.equals(StatusCodes.TARGET_AGENT_DOES_NOT_EXIST.name())) {
                    sendingMessageStatusText = "Message not sent since the target agent does not exist.";
                } else if (statusValue.equals(StatusCodes.MESSAGE_LENGTH_EXCEEDED.name())) {
                    sendingMessageStatusText = "Message not sent since it is longer than 140 characters.";
                } else if (statusValue.equals(StatusCodes.TARGET_AGENT_QUOTA_EXCEEDED.name())) {
                    sendingMessageStatusText = "Message not sent since target agent's quota exceeded.";
                } else {
                    /*In the case of SESSION_KEY_UNRECOGNIZED, SOURCE_AGENT_NOT_LOGGED_IN, and
                    SESSION_KEY_INVALID_LENGTH, the user should have been logged out.*/
                    System.err.println("Unexpected statusCookie \"" + statusValue + "\" in SendMailServlet.");
                }
            }

            response.getWriter().println("" +
                    "<h1>Send a Message</h1>" +
                    "<hr>" +
                    "<div id=\"composeFormBlock\" class=\"compose\">" +
                    "    <p class=\"notification\">" + Utils.getSpan("notif", sendingMessageStatusText) + "</p>" +
                    "    " + Utils.getPostForm("composeForm", "/sendmessage") +
                    "    " + Utils.getInputField("destination", "To Agent ID:", true) + "<br>" +
                    "    " + Utils.getTextArea("messageBody", "Message Body (140 characters)", 2, 70) + "<br>" +
                    "    " + Utils.getSubmitButton("submit", "Send message") +
                    "</div>" +
                    Utils.getHrefButton("/loggedin", "backToMailbox", "Go back")
            );
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        final Cookie idCookie = Utils.findCookie(request.getCookies(), CookieNames.AGENT_ID.name());
        final Cookie skCookie = Utils.findCookie(request.getCookies(), CookieNames.SESSION_KEY.name());

        if (idCookie == null || skCookie == null) {
            response.sendRedirect("/register");
        } else if (!messagingSystem.agentLoggedIn(idCookie.getValue())) {
            Utils.deleteCookie(idCookie, response);
            Utils.deleteCookie(skCookie, response);
            response.addCookie(new Cookie(CookieNames.LOGGED_OUT_STATUS.name(), "You_were_logged_out_of_the_system."));
            response.sendRedirect("/register");
        } else {
            final String id = idCookie.getValue();
            final String sessionKey = skCookie.getValue();

            final String destination = request.getParameter("destination");
            final String message = request.getParameter("messageBody");

            final StatusCodes status = messagingSystem.sendMessage(sessionKey, id, destination, message);

            switch (status) {
                case SOURCE_AGENT_DOES_NOT_EXIST:
                case SOURCE_AGENT_NOT_LOGGED_IN:
                case SESSION_KEY_UNRECOGNIZED:
                case FAILED_TO_ADD_TO_MAILBOX:
                    response.addCookie(new Cookie(CookieNames.LOGGED_OUT_STATUS.name(),
                            "You_were_logged_out_due_to_an_error_in_the_system."));
                    Utils.deleteCookie(idCookie, response);
                    Utils.deleteCookie(skCookie, response);
                    break;

                case SOURCE_AGENT_QUOTA_EXCEEDED:
                case BOTH_AGENT_QUOTAS_EXCEEDED:
                    response.addCookie(new Cookie(CookieNames.LOGGED_OUT_STATUS.name(),
                            "You_were_logged_out_of_the_system."));
                    Utils.deleteCookie(idCookie, response);
                    Utils.deleteCookie(skCookie, response);
                    break;

                default:
                    response.addCookie(new Cookie(CookieNames.MESSAGE_SENDING_STATUS.name(), status.name()));
            }
            response.sendRedirect("/sendmessage");
        }
    }

}
