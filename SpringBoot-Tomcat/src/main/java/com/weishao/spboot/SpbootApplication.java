package com.weishao.spboot;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import com.weishao.spboot.servlet.IndexServlet;

/**
 * SpringBoot的底层实际上也是封装了这些子模块实现
 * 的，Controller的本质上还是一个Servlet，Spring框架
 * 为我们提供了更加灵活简单的配置和使用方式。
 * 
 * 参考地址：https://www.jianshu.com/p/b0c9f02cba8f
 * 
 * @author tang
 *
 */
public class SpbootApplication {

    private static int PORT = 8080;
    private static String CONTEXT_PATH="/tomcat";
    private static String SERVLET_NAME = "IndexServlet";
	
	public static void main(String[] args) {
        //创建tomcat服务器
        Tomcat tomcatServer = new Tomcat();
        //设置端口号
        tomcatServer.setPort(PORT);
        //是否自动部署
        tomcatServer.getHost().setAutoDeploy(false);

        //创建上下文
        StandardContext standardContext = new StandardContext();
        standardContext.setPath(CONTEXT_PATH);
        standardContext.addLifecycleListener(new Tomcat.FixContextListener());

        //将上下文加入到tomcat中去
        tomcatServer.getHost().addChild(standardContext);

        //创建Servlet
        tomcatServer.addServlet(CONTEXT_PATH,SERVLET_NAME,new IndexServlet());
        // servlet url映射
        standardContext.addServletMappingDecoded("/index", SERVLET_NAME);
        try {
			tomcatServer.start();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}

        System.out.println("tomcat服务器启动成功..");
        
        //异步接收请求
        tomcatServer.getServer().await();
	}

}
