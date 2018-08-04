package webapp;

import system.Agent;
import system.MessagingSystem;
import system.Supervisor;
import system.SupervisorImpl;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/register")
class RegisterServlet extends HttpServlet {

    private final MessagingSystem messagingSystem;

    RegisterServlet(final MessagingSystem messagingSystem) {
        this.messagingSystem = messagingSystem;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String loggedOutStatusText = "";
        final Cookie loggedOutCookie = Utils.findCookie(request.getCookies(), CookieNames.LOGGED_OUT_STATUS.name());
        if (loggedOutCookie != null) {
            loggedOutStatusText = loggedOutCookie.getValue().replaceAll("_", " ");
            Utils.deleteCookie(loggedOutCookie, response);
        }

        response.setContentType("text/html");
        response.getWriter().println("" +
                "<h1>Register Screen</h1>" +
                "<hr>" +
                "<p class=\"notification\">" + Utils.getSpan("notif", loggedOutStatusText) + "</p>" +
                Utils.getPostForm("registerForm", "/register") +
                Utils.getInputField("idInput", "Agent ID", true) + "<br>" +
                Utils.getSubmitButton("submit", "Register")
        );
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        final String id = request.getParameter("idInput");

        final Supervisor supervisor = new SupervisorImpl(messagingSystem);
        final Agent agent = new Agent(id, supervisor, messagingSystem);

        if (agent.register()) {
            response.addCookie(new Cookie(CookieNames.AGENT_ID.name(), id));
            response.addCookie(new Cookie(CookieNames.LOGIN_KEY.name(), agent.getLoginKey()));
            response.sendRedirect("/login");
        } else {
            response.addCookie(new Cookie(CookieNames.LOGGED_OUT_STATUS.name(), "Access_denied_by_your_supervisor."));
            response.sendRedirect("/register");
        }
    }
}