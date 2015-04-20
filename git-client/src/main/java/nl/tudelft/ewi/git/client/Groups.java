package nl.tudelft.ewi.git.client;

import java.util.List;

import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;

/**
 * This class allows you to query and manipulate {@link GroupModel} objects on the git-server.
 */
public interface Groups {
	
	/**
	 * @return all {@link GroupModel} objects currently registered with the git-server.
	 */
	public List<IdentifiableModel> retrieveAll() throws GitClientException;
	
	/**
	 * This method retrieves the specified {@link GroupModel} from the git-server.
	 * 
	 * @param groupName
	 *            The name of the {@link GroupModel}.
	 * @return The retrieved {@link GroupModel} object.
	 */
	GroupModel retrieve(String groupName) throws GitClientException;
	
	/**
	 * This method retrieves the specified {@link GroupModel} from the git-server.
	 * 
	 * @param model
	 *            The {@link IdentifiableModel} to retrieve.
	 * @return The retrieved {@link GroupModel}.
	 */
	GroupModel retrieve(IdentifiableModel model) throws GitClientException;
	
	/**
	 * This method creates a new {@link GroupModel} on the git-server.
	 * 
	 * @param newGroup
	 *            The new {@link GroupModel} to construct on the git-server.
	 * @return The created {@link GroupModel} object.
	 */
	GroupModel create(GroupModel newGroup) throws GitClientException;
	
	/**
	 * This method ensures that a specified {@link GroupModel} exists on the server.
	 * 
	 * @param model
	 *            The {@link GroupModel} to create if it does not yet exist.
	 * @return The created or fetched {@link GroupModel} object.
	 */
	GroupModel ensureExists(GroupModel model) throws GitClientException;
	
	/**
	 * This method deletes a {@link GroupModel} from the git-server.
	 * 
	 * @param group
	 *            The {@link IdentifiableModel} to remove from the git-server.
	 */
	void delete(IdentifiableModel group) throws GitClientException;
	
	/**
	 * This method returns a {@link GroupMembers} object which allows you to query and manipulate
	 * members of the specified {@link GroupModel} object.
	 * 
	 * @param group
	 *            The {@link GroupModel} of which you wish to query or manipulate group members.
	 * @return The constructed {@link GroupMembers} object.
	 */
	GroupMembers groupMembers(GroupModel group) throws GitClientException;
	
}
