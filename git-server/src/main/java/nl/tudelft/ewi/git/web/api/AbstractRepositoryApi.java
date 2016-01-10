package nl.tudelft.ewi.git.web.api;

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

	protected AbstractRepositoryApi(ManagedConfig managedConfig, Transformers transformers, Repository repository) {
		this.managedConfig = managedConfig;
		this.transformers = transformers;
		this.repository = repository;
	}

	protected  AbstractRepositoryApi(ManagedConfig managedConfig, Transformers transformers, RepositoriesManager repositoriesManager, String repositoryName) {
		this.managedConfig = managedConfig;
		this.transformers = transformers;
		try {
			this.repository = repositoriesManager.getRepository(getRepositoryURI(repositoryName));
		} catch (RepositoryNotFoundException e) {
			throw new NotFoundException(e);
		}
	}

	protected URI getRepositoryURI(String repositoryName) {
		return URI.create(repositoryName + ".git/");
	}

	protected String getRepositoryName() {
		String repoName = repository.getURI().toString();
		return repoName.substring(0, repoName.lastIndexOf('/'));
	}

}
