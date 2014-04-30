package nl.tudelft.ewi.git.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;

import com.google.common.collect.Lists;

import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;

/**
 * The {@code GroupsMock} mocks a {@link Groups} class
 * @author Jan-Willem
 *
 */
public class GroupsMock implements Groups {
	
	private final Map<String, GroupModel> groups = new HashMap<>();
	private final Map<GroupModel, GroupMembers> groupMembers = new HashMap<>();

	@Override
	public List<IdentifiableModel> retrieveAll() {
		return Lists.<IdentifiableModel> newArrayList(groups.values());
	}

	@Override
	public GroupModel retrieve(String groupName) {
		GroupModel groupModel = groups.get(groupName);
		if(groupModel == null) {
			throw new NotFoundException("No group found for name: " + groupName);
		}
		return groupModel;
	}

	@Override
	public GroupModel retrieve(IdentifiableModel model) {
		return retrieve(model.getName());
	}

	@Override
	public GroupModel create(GroupModel newGroup) {
		groups.put(newGroup.getName(), newGroup);
		return newGroup;
	}

	@Override
	public GroupModel ensureExists(GroupModel model) {
		try {
			return retrieve(model.getName());
		}
		catch (NotFoundException e) {
			return create(model);
		}
	}

	@Override
	public void delete(IdentifiableModel group) {
		// Ensure that the group exists, or throw an exception
		retrieve(group);
		groups.remove(group.getName());
		groupMembers.remove(group.getName());
	}

	@Override
	public GroupMembers groupMembers(GroupModel group) {
		// Ensure that the group exists, or throw an exception
		group = retrieve(group);
		GroupMembers members = groupMembers.get(group);
		if(members == null) {
			// Create new GroupMembersMock and put it in to the map
			members = new GroupMembersMock();
			groupMembers.put(group, members);
		}
		return members;
	}

}
