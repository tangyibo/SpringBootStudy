package cn.com.ruijie.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * 基于jetty的server服务
 * @author Administrator
 */
public class HttpConfigServer {

    public static void main(String[] args) {
        int port =6543;
        Server server = new Server(port);
        server.addConnector(new SelectChannelConnector());
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new ServiceHandler()), "/api/*");
        server.setHandler(context);
        
        try {
            server.start();
            System.out.println("Start server on port:"+port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
