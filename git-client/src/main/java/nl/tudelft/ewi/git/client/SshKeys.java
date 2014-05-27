package nl.tudelft.ewi.git.client;

import java.util.List;

import nl.tudelft.ewi.git.models.SshKeyModel;

/**
 * This class allows you to query and manipulate SSH keys on the git-server.
 */
public interface SshKeys {

	/**
	 * @return All SSH keys of the specified user.
	 */
	List<SshKeyModel> retrieveAll();
	
	/**
	 * Retrieves a specific SSH key.
	 * 
	 * @param keyName
	 *            The name of the SSH key.
	 * @return The retrieved {@link SshKeyModel} representing the SSH key.
	 */
	SshKeyModel retrieve(String keyName);
	
	/**
	 * This method registers a new SSH key with the git-server.
	 * 
	 * @param sshKey
	 *            The {@link SshKeyModel} to register.
	 * @return The created {@link SshKeyModel} from the git-server.
	 */
	SshKeyModel registerSshKey(SshKeyModel sshKey);
	
	/**
	 * This method removes an existing SSH key from the git-server.
	 * 
	 * @param sshKey
	 *            The {@link SshKeyModel} describing the SSH key to remove.
	 */
	void deleteSshKey(SshKeyModel sshKey);
	
}
