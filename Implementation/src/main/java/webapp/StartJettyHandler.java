package webapp;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import system.MessagingSystem;

public class StartJettyHandler {

    public static final int PORT_NUMBER = 8080;

    public static void main(String[] args) {

        final MessagingSystem messagingSystem = new MessagingSystem();
        final Runnable runnable = () -> {
            final Server server = new Server(PORT_NUMBER);
            try {
                server.getConnectors()[0].getConnectionFactory(HttpConnectionFactory.class);

                ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                context.setContextPath("/");
                server.setHandler(context);

                context.addServlet(new ServletHolder(new DefaultServlet()), "/");
                context.addServlet(new ServletHolder(new RegisterServlet(messagingSystem)), "/register/*");
                context.addServlet(new ServletHolder(new LoginServlet(messagingSystem)), "/login/*");
                context.addServlet(new ServletHolder(new LoggedInServlet(messagingSystem)), "/loggedin/*");
                context.addServlet(new ServletHolder(new SendMessageServlet(messagingSystem)), "/sendmessage/*");
                context.addServlet(new ServletHolder(new ReadMessageServlet(messagingSystem)), "/readmessage/*");
                context.addServlet(new ServletHolder(new LogoutServlet(messagingSystem)), "/logout/*");

                server.start();
                server.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        new Thread(runnable).start();
    }
}