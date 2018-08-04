package webapp;

import system.Message;
import system.MessagingSystem;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/readmessage")
class ReadMessageServlet extends HttpServlet {

    private final MessagingSystem messagingSystem;

    ReadMessageServlet(MessagingSystem messagingSystem) {
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
            final String id = idCookie.getValue();
            final String sessionKey = skCookie.getValue();

            if (messagingSystem.agentHasMessages(id)) {
                final Message message = messagingSystem.getNextMessage(sessionKey, id);
                response.getWriter().println("" +
                        "<h1>Latest Message</h1>" +
                        "<hr>" +
                        "<p id=\"messageContainer\">" +
                        "    <b>From</b>: Agent " + Utils.getSpan("from", message.getSourceAgentId()) + "<br>" +
                        "    <b>To</b>: Agent " + Utils.getSpan("to", message.getTargetAgentId()) + "<br>" +
                        "    <b>Timestamp</b>: " + Utils.getSpan("timestamp", "" + message.getTimestamp()) + "<br>" +
                        "    <b>Message</b>: " + Utils.getSpan("message", message.getMessage()) + "<br>" +
                        "</p>" +
                        Utils.getHrefButton("/readmessage", "consume", "Consume another message") + "<br>" +
                        Utils.getHrefButton("/loggedin", "backToMailbox", "Go back")
                );
            } else {
                response.getWriter().println("" +
                        "<h1>Latest Message</h1>" +
                        "<hr>" +
                        "<p id=\"messageContainer\">You have no new messages.</p>" +
                        Utils.getHrefButton("/readmessage", "consume", "Try again") + "<br>" +
                        Utils.getHrefButton("/loggedin", "backToMailbox", "Go back")
                );
            }
        }
    }
}
