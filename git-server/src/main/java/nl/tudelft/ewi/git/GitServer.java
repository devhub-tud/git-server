package nl.tudelft.ewi.git;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContext;

import com.google.inject.util.Modules;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.jcraft.jsch.Session;

/**
 * This class is the main class of the Git Server project. It contains a main method allowing you to start a Git Server.
 * 
 * @author michael
 */
@Slf4j
public class GitServer {

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

		// TODO: Fix this...
		SshSessionFactory.setInstance(new JschConfigSessionFactory() {
			@Override
			protected void configure(Host hc, Session session) {
				session.setConfig("StrictHostKeyChecking", "no");
			}
		});

		Config config = new Config();
		config.reload();
		
		GitServer server = new GitServer(config);
		server.start();
		server.join();
	}

	private final Server server;
	private final AtomicReference<Injector> injectorAtomicReference = new AtomicReference<>();

	/**
	 * Creates a new instance of the {@link GitServer} class but does not start it.
	 *
	 * @param config
	 *        The {@link Config} object which has user-specified settings.
	 * @param overrides
	 *        The module overrides.
	 * @throws IOException If an I/O error occurs.
	 * @throws InterruptedException If the current thread is interrupted.
	 */
	public GitServer(final Config config, final Module... overrides) throws IOException, InterruptedException {
		ContextHandlerCollection handlers = new ContextHandlerCollection();
		handlers.addContext("/", "/").setHandler(new GitServerHandler(config, overrides));

		server = new Server(config.getHttpPort());
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
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});

	}

	public void join() throws InterruptedException {
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

	private class GitServerHandler extends ServletContextHandler {

		private GitServerHandler(final Config config, final Module... overrides) {
			addEventListener(new GuiceResteasyBootstrapServletContextListener() {
				@Override
				protected List<Module> getModules(ServletContext context) {
					return ImmutableList.<Module> of(Modules.override(new GitServerModule(config)).with(overrides));
				}

				@Override
				protected void withInjector(Injector injector) {
					injectorAtomicReference.set(injector);
					// No interaction with injector required. Skipping...
				}
			});

			addServlet(HttpServletDispatcher.class, "/");
		}
	}

	public Injector getInjector() {
		return injectorAtomicReference.get();
	}

}
