package nl.tudelft.ewi.git.client;

import java.util.Collection;

import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;

/**
 * This class allows you to query and manipulate group members of a specific {@link GroupModel}.
 */
public interface GroupMembers {

	/**
	 * @return All currently registered {@link GroupModel} objects on the git-server.
	 */
	Collection<IdentifiableModel> listAll();
	
	/**
	 * This method registers a new member for the specified {@link GroupModel}.
	 * 
	 * @param identifiable
	 *            The new member to add to the {@link GroupModel}.
	 * @return A {@link Collection} of {@link IdentifiableModel}s representing all current group
	 *         members.
	 */
	Collection<IdentifiableModel> addMember(IdentifiableModel identifiable);
	
	/**
	 * This method removes an existing group member from the specified {@link GroupModel}.
	 * 
	 * @param identifiable
	 *            The member to remove from the {@link GroupModel}.
	 */
	void removeMember(IdentifiableModel identifiable);
	
}
