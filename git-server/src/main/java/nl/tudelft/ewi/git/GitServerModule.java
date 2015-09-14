package nl.tudelft.ewi.git;
import java.lang.annotation.Annotation;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import lombok.extern.slf4j.Slf4j;

import nl.tudelft.ewi.git.web.api.BaseApi;
import nl.tudelft.ewi.git.web.api.BaseApiImpl;
import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.git.web.api.di.BranchApiFactory;
import nl.tudelft.ewi.git.web.api.BranchApiImpl;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.di.CommitApiFactory;
import nl.tudelft.ewi.git.web.api.CommitApiImpl;
import nl.tudelft.ewi.git.web.api.di.Factory;
import nl.tudelft.ewi.git.web.api.GroupApi;
import nl.tudelft.ewi.git.web.api.di.GroupApiFactory;
import nl.tudelft.ewi.git.web.api.GroupApiImpl;
import nl.tudelft.ewi.git.web.api.GroupsApi;
import nl.tudelft.ewi.git.web.api.GroupsApiImpl;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApiImpl;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import nl.tudelft.ewi.git.web.api.di.RepositoryApiFactory;
import nl.tudelft.ewi.git.web.api.RepositoryApiImpl;
import nl.tudelft.ewi.git.web.api.UsersApi;
import nl.tudelft.ewi.git.web.api.UsersApiImpl;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;
import org.jboss.resteasy.plugins.guice.ext.JaxrsModule;
import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;
import org.reflections.Reflections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;

@Slf4j
class GitServerModule extends AbstractModule {

	private final ManagedConfig managedConfig;
	private final RepositoriesManager repositoriesManager;
	private final Config config;

	/**
	 * Constructs a new {@link GitServerModule} object which specifies how to configure the {@link GitServer}.
	 * 
	 * @param managedConfig
	 *        The {@link ManagedConfig} to use when interacting with the Gitolite-admin repository.
	 * @param config the config
	 */
	public GitServerModule(ManagedConfig managedConfig, RepositoriesManager repositoriesManager, Config config) {
		this.managedConfig = managedConfig;
		this.repositoriesManager = repositoriesManager;
		this.config = config;
	}

	@Override
	protected void configure() {
		install(new RequestScopeModule());
		install(new JaxrsModule());
		requireBinding(ObjectMapper.class);

		findResourcesWith(Path.class);
		findResourcesWith(Provider.class);

		bind(BaseApi.class).to(BaseApiImpl.class);
		bind(UsersApi.class).to(UsersApiImpl.class);
		bind(GroupsApi.class).to(GroupsApiImpl.class);
		bind(RepositoriesApi.class).to(RepositoriesApiImpl.class);

		bindSubResourceFactory(GroupApi.class, GroupApiImpl.class, GroupApiFactory.class);
		bindSubResourceFactory(CommitApi.class, CommitApiImpl.class, CommitApiFactory.class);
		bindSubResourceFactory(BranchApi.class, BranchApiImpl.class, BranchApiFactory.class);
		bindSubResourceFactory(RepositoryApi.class, RepositoryApiImpl.class, RepositoryApiFactory.class);

		bind(ManagedConfig.class).toInstance(managedConfig);
		bind(RepositoriesManager.class).toInstance(repositoriesManager);
	}

	protected <T> void bindSubResourceFactory(Class<T> iface, Class<? extends T> implementation, Class<? extends Factory<T>> factory) {
		install(new FactoryModuleBuilder()
			.implement(iface, implementation)
			.build(factory));
	}

	private void findResourcesWith(Class<? extends Annotation> ann) {
		Reflections reflections = new Reflections(getClass().getPackage().getName());
		for (Class<?> clasz : reflections.getTypesAnnotatedWith(ann)) {
			if(clasz.isInterface()) continue;
			log.info("Registering resource {}", clasz);
			bind(clasz);
		}
	}

}
