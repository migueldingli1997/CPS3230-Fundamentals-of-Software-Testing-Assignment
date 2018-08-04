package webapp;

import system.MessagingSystem;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/logout")
class LogoutServlet extends HttpServlet {

    private final MessagingSystem messagingSystem;

    LogoutServlet(final MessagingSystem messagingSystem) {
        this.messagingSystem = messagingSystem;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        final Cookie idCookie = Utils.findCookie(request.getCookies(), CookieNames.AGENT_ID.name());
        final Cookie sKeyCookie = Utils.findCookie(request.getCookies(), CookieNames.SESSION_KEY.name());

        if (idCookie != null && sKeyCookie != null) {
            messagingSystem.logout(idCookie.getValue());
            Utils.deleteCookie(idCookie, response);
            Utils.deleteCookie(sKeyCookie, response);
        }

        response.addCookie(new Cookie(CookieNames.LOGGED_OUT_STATUS.name(), "Successfully_logged_out."));
        response.sendRedirect("/register");
    }
}
