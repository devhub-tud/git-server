package nl.tudelft.ewi.git.client;

import java.util.List;

import nl.tudelft.ewi.git.models.*;

/**
 * This class allows you query and manipulate repositories on the git-server.
 */
public interface Repositories {
	
	/**
	 * @return All currently active {@link Repository} objects on the git-server.
	 */
	List<RepositoryModel> retrieveAll() throws GitClientException;

	/**
	 * This method retrieves the specified {@link Repository} from the git-server.
	 * 
	 * @param model
	 *            The {@link Repository} to retrieve from the git-server.
	 * @return The retrieved {@link Repository} object.
	 */
	Repository retrieve(RepositoryModel model) throws GitClientException;

	/**
	 * This mehtod retrieves the specified {@link Repository} from the git-server.
	 * 
	 * @param name
	 *            The name of the {@link Repository} to retrieve from the git-server.
	 * @return The retrieved {@link Repository} object.
	 */
	Repository retrieve(String name) throws GitClientException;

	/**
	 * This method creates a new {@link Repository} on the git-server.
	 * 
	 * @param newRepository
	 *            The new {@link Repository} to provision on the git-server.
	 * @return The created {@link Repository} on the git-server.
	 */
	Repository create(CreateRepositoryModel newRepository) throws GitClientException;


}
