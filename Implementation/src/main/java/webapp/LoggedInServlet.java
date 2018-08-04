package webapp;

import system.MessagingSystem;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/loggedin")
class LoggedInServlet extends HttpServlet {

    private final MessagingSystem messagingSystem;

    LoggedInServlet(MessagingSystem messagingSystem) {
        this.messagingSystem = messagingSystem;
    }

    @Override
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

            response.getWriter().println("" +
                    "<h1>Agent " + id + "'s Mailbox</h1>" +
                    "<hr>" +
                    Utils.getHrefButton("/readmessage", "consumeMessage", "Get Next Message") + "<br>" +
                    Utils.getHrefButton("/sendmessage", "sendMessage", "Send a Message") + "<br>" +
                    "<hr>" +
                    Utils.getHrefButton("/logout", "logout", "Logout")
            );
        }
    }
}