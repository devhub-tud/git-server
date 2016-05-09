package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.backend.RepositoryFacadeFactory;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;
import nl.tudelft.ewi.gitolite.repositories.Repository;
import nl.tudelft.ewi.gitolite.repositories.RepositoryNotFoundException;

import javax.ws.rs.NotFoundException;
import java.net.URI;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public abstract class AbstractRepositoryApi {

	protected final ManagedConfig managedConfig;
	protected final Transformers transformers;
	protected final Repository repository;
	protected final RepositoryFacadeFactory repositoryFacadeFactory;


	protected AbstractRepositoryApi(ManagedConfig managedConfig, Transformers transformers, Repository repository, RepositoryFacadeFactory repositoryFacadeFactory) {
		this.managedConfig = managedConfig;
		this.transformers = transformers;
		this.repository = repository;
		this.repositoryFacadeFactory = repositoryFacadeFactory;
	}

	protected  AbstractRepositoryApi(ManagedConfig managedConfig, Transformers transformers, RepositoriesManager repositoriesManager, RepositoryFacadeFactory repositoryFacadeFactory, String repositoryName) {
		this.managedConfig = managedConfig;
		this.transformers = transformers;
		this.repositoryFacadeFactory = repositoryFacadeFactory;
		try {
			this.repository = repositoriesManager.getRepository(getRepositoryURI(repositoryName));
		} catch (RepositoryNotFoundException e) {
			throw new NotFoundException(e);
		}
	}

	protected URI getRepositoryURI(String repositoryName) {
		return URI.create(repositoryName + ".git/");
	}

}
