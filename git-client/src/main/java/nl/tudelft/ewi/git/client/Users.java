package nl.tudelft.ewi.git.client;

import java.util.List;

import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.git.models.UserModel;

/**
 * This class allows you to query and manipulate users on the git-server.
 */
public interface Users {
	
	/**
	 * @return All {@link UserModel} objects currently registered on the git-server.
	 */
	List<IdentifiableModel> retrieveAll() throws GitClientException;
	
	/**
	 * This method retrieves a specific {@link UserModel} from the git-server.
	 * 
	 * @param userName
	 *            The name of the {@link UserModel} to retrieve.
	 * @return The retrieved {@link UserModel} object.
	 */
	UserModel retrieve(String userName) throws GitClientException;
	
	/**
	 * This method retrieves a specific {@link UserModel} from the git-server.
	 * 
	 * @param model
	 *            The {@link UserModel} to retrieve from the git-server.
	 * @return The retrieved {@link UserModel} object.
	 */
	UserModel retrieve(UserModel model) throws GitClientException;
	
	/**
	 * This method creates a new {@link UserModel} object on the git-server.
	 * 
	 * @param newUser
	 *            The {@link UserModel} object to create on the git-server.
	 * @return The created {@link UserModel} object.
	 */
	UserModel create(UserModel newUser) throws GitClientException;
	
	/**
	 * This method ensures that a specific {@link UserModel} exists on the git-server.
	 * 
	 * @param name
	 *            The name of the user to ensure exists on the git-server.
	 * @return The created or retrieved {@link UserModel}.
	 */
	UserModel ensureExists(String name) throws GitClientException;
	
	/**
	 * This method ensures that a specific {@link UserModel} exists on the git-server.
	 * 
	 * @param model
	 *            The {@link UserModel} to ensure that it exists.
	 * @return The created or fetched {@link UserModel} on the git-server.
	 */
	UserModel ensureExists(UserModel model) throws GitClientException;
	
	/**
	 * This method deletes an existing {@link UserModel} from the git-server.
	 * 
	 * @param user
	 *            The {@link UserModel} to delete.
	 */
	void delete(IdentifiableModel user) throws GitClientException;
	
	/**
	 * Creates a new returns a {@link SshKeys} object allowing you to query and manipulate the SSH
	 * keys of the specified {@link UserModel} object.
	 * 
	 * @param user
	 *            The {@link UserModel} to query or manipulate the SSH keys of.
	 * @return The constructed {@link SshKeys} object.
	 */
	SshKeys sshKeys(UserModel user) throws GitClientException;
	
}
