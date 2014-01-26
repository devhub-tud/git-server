package nl.tudelft.ewi.git;

import java.util.List;

import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;

@Slf4j
public class GitServer {
	
	public static void main(String[] args) throws Exception {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
			
		GitServer server = new GitServer(8080);
		server.startServer();
	}

	private final Server server;

	public GitServer(int port) {
		ContextHandlerCollection handlers = new ContextHandlerCollection();
		handlers.addContext("/", "/").setHandler(new GitServerHandler());
		
		server = new Server(port);
		server.setHandler(handlers);
	}
	
	public void startServer() throws Exception {
		server.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					stopServer();
				}
				catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
		
		server.join();
	}
	
	public void stopServer() throws Exception {
		server.stop();
	}
	
	private static class GitServerHandler extends ServletContextHandler {
		
		public GitServerHandler() {
			addEventListener(new GuiceResteasyBootstrapServletContextListener() {
				@Override
				protected List<Module> getModules(ServletContext context) {
					return ImmutableList.<Module>of(new GitServerModule());
				}
				
				@Override
				protected void withInjector(Injector injector) {
				}
			});
			
			addServlet(HttpServletDispatcher.class, "/");
		}
	}
	
}
