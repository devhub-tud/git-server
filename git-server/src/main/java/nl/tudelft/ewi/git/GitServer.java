package nl.tudelft.ewi.git;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;
import nl.minicom.gitolite.manager.exceptions.GitException;
import nl.minicom.gitolite.manager.exceptions.ServiceUnavailable;
import nl.minicom.gitolite.manager.models.ConfigManager;

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

		Config config = new Config();
		config.reload();
		
		GitServer server = new GitServer(config);
		server.start();
	}

	private final Server server;

	/**
	 * Creates a new instance of the {@link GitServer} class but does not start it. 
	 * 
	 * @param config
	 *        The {@link Config} object which has user-specified settings.
	 */
	public GitServer(Config config) {
		ContextHandlerCollection handlers = new ContextHandlerCollection();
		handlers.addContext("/", "/").setHandler(new GitServerHandler(config));

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

		private GitServerHandler(final Config config) {
			addEventListener(new GuiceResteasyBootstrapServletContextListener() {
				@Override
				protected List<Module> getModules(ServletContext context) {
					// TODO: Fix this...
					SshSessionFactory.setInstance(new JschConfigSessionFactory() {
						@Override
						protected void configure(Host hc, Session session) {
							session.setConfig("StrictHostKeyChecking", "no");
						}
					});
					
					ConfigManager configManager = ConfigManager.create(config.getGitoliteRepoUrl());
					try {
						configManager.get();
					}
					catch (IOException | ServiceUnavailable | GitException e) {
						log.warn("Could not connect to the gitolite instance: " + e.getMessage(), e);
					}

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
