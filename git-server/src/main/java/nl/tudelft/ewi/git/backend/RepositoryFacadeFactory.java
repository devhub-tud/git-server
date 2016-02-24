package nl.tudelft.ewi.git.backend;

import nl.tudelft.ewi.gitolite.repositories.Repository;

import java.io.IOException;

/**
 * Used to instantiate a {@link RepositoryFacade}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface RepositoryFacadeFactory {

	/**
	 * Instantiate a {@link RepositoryFacade}.
	 * @param repository Repository for which to instantiate a facade.
	 * @return The instance.
	 */
	RepositoryFacade create(Repository repository) throws IOException;

}
