package nl.tudelft.ewi.git.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import nl.tudelft.ewi.git.models.IdentifiableModel;

/**
 * The {@code GroupMembersMock} mocks a {@link GroupMembers} class
 * @author Jan-Willem
 *
 */
public class GroupMembersMock implements GroupMembers {
	
	private final List<IdentifiableModel> members = new ArrayList<>();

	@Override
	public Collection<IdentifiableModel> listAll() {
		return Lists.newArrayList(members);
	}

	@Override
	public Collection<IdentifiableModel> addMember(IdentifiableModel identifiable) {
		members.add(identifiable);
		return listAll();
	}

	@Override
	public void removeMember(IdentifiableModel identifiable) {
		members.remove(identifiable);
	}
	
}
