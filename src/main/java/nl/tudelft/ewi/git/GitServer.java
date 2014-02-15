package nl.tudelft.ewi.git;

import java.io.File;
import java.util.List;

import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;
import nl.minicom.gitolite.manager.models.ConfigManager;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * This class is the main class of the Git Server project. It contains a main method allowing you to start a Git Server.
 * 
 * @author michael
 */
@Slf4j
public class GitServer {

	private static final int DEFAULT_HTTP_PORT = 8080;
	private static final String HTTP_PORT_PREFIX = "--httpPort=";

	/**
	 * Starts a new {@link GitServer} instance on a specified port. You can specify a HTTP port by providing an argument
	 * of the form <code>--httpPort=xxxx</code> where <code>xxxx</code> is a port number. If no such argument is
	 * specified the HTTP port defaults to 8080.
	 * 
	 * @param args
	 *        The arguments to influence the start-up phase of the {@link GitServer} instance.
	 * @throws Exception
	 *         In case the {@link GitServer} instance could not be started.
	 */
	public static void main(String[] args) throws Exception {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		GitServer server = new GitServer(getHttpPort(args));
		server.start();
	}

	private static int getHttpPort(String[] args) {
		for (String arg : args) {
			if (arg.startsWith(HTTP_PORT_PREFIX)) {
				return Integer.parseInt(arg.substring(HTTP_PORT_PREFIX.length()));
			}
		}
		return DEFAULT_HTTP_PORT;
	}

	private final Server server;

	/**
	 * Creates a new instance of the {@link GitServer} class but does not start it. You can specify a HTTP port on which
	 * to listen for REST API calls.
	 * 
	 * @param port
	 *        The HTTP port on which to listen for REST API calls.
	 */
	public GitServer(int port) {
		ContextHandlerCollection handlers = new ContextHandlerCollection();
		handlers.addContext("/", "/").setHandler(new GitServerHandler());

		server = new Server(port);
		server.setHandler(handlers);
	}

	/**
	 * This method starts the server.
	 * 
	 * @throws Exception
	 *         In case the server fails to start.
	 */
	public void start() throws Exception {
		server.start();

		final GitServer self = this;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					self.stop();
				}
				catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});

		server.join();
	}

	/**
	 * This method stops the server.
	 * 
	 * @throws Exception
	 *         In case the server fails to stop.
	 */
	public void stop() throws Exception {
		server.stop();
	}

	private static class GitServerHandler extends ServletContextHandler {

		private GitServerHandler() {
			addEventListener(new GuiceResteasyBootstrapServletContextListener() {
				@Override
				protected List<Module> getModules(ServletContext context) {
					ConfigManager configManager = ConfigManager.create("ssh://git@localhost:2222/gitolite-admin.git");
					File mirrorsDirectory = new File("mirrors");

					return ImmutableList.<Module> of(new GitServerModule(configManager, mirrorsDirectory));
				}

				@Override
				protected void withInjector(Injector injector) {
					// No interaction with injector required. Skipping...
				}
			});

			addServlet(HttpServletDispatcher.class, "/");
		}
	}

}
