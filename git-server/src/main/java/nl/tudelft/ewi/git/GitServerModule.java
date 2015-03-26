package nl.tudelft.ewi.git;

import java.io.File;
import java.lang.annotation.Annotation;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;
import nl.minicom.gitolite.manager.models.ConfigManager;
import nl.tudelft.ewi.git.inspector.Inspector;

import org.jboss.resteasy.plugins.guice.ext.JaxrsModule;
import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;
import org.reflections.Reflections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;

@Slf4j
class GitServerModule extends AbstractModule {

	private final ConfigManager configManager;
	private final Config config;

	/**
	 * Constructs a new {@link GitServerModule} object which specifies how to configure the {@link GitServer}.
	 * 
	 * @param configManager
	 *        The {@link ConfigManager} to use when interacting with the Gitolite-admin repository.
	 * @param config the config
	 */
	public GitServerModule(ConfigManager configManager, Config config) {
		this.configManager = configManager;
		this.config = config;
	}

	@Override
	protected void configure() {
		install(new RequestScopeModule());
		install(new JaxrsModule());
		requireBinding(ObjectMapper.class);

		findResourcesWith(Path.class);
		findResourcesWith(Provider.class);

		bind(ConfigManager.class).toInstance(configManager);
		bind(Inspector.class).toInstance(new Inspector(config));
	}

	private void findResourcesWith(Class<? extends Annotation> ann) {
		Reflections reflections = new Reflections(getClass().getPackage().getName());
		for (Class<?> clasz : reflections.getTypesAnnotatedWith(ann)) {
			log.info("Registering resource {}", clasz);
			bind(clasz);
		}
	}

}
