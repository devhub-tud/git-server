package nl.tudelft.ewi.git.client;

import java.util.Collection;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;

/**
 * This class allows you to query and manipulate group members of a specific {@link GroupModel}.
 */
public class GroupMembers extends Backend {

	private final GroupModel group;

	GroupMembers(String host, GroupModel group) {
		super(host);
		this.group = group;
	}

	/**
	 * @return All currently registered {@link GroupModel} objects on the git-server.
	 */
	public Collection<IdentifiableModel> listAll() {
		return perform(new Request<Collection<IdentifiableModel>>() {
			@Override
			public Collection<IdentifiableModel> perform(Client client) {
				return client.target(createUrl(group.getPath() + "/members"))
						.request(MediaType.APPLICATION_JSON)
						.get(new GenericType<Collection<IdentifiableModel>>() {
						});
			}
		});
	}

	/**
	 * This method registers a new member for the specified {@link GroupModel}.
	 * 
	 * @param identifiable
	 *            The new member to add to the {@link GroupModel}.
	 * @return A {@link Collection} of {@link IdentifiableModel}s representing all current group
	 *         members.
	 */
	public Collection<IdentifiableModel> addMember(final IdentifiableModel identifiable) {
		return perform(new Request<Collection<IdentifiableModel>>() {
			@Override
			public Collection<IdentifiableModel> perform(Client client) {
				return client.target(createUrl(group.getPath() + "/members"))
						.request(MediaType.APPLICATION_JSON)
						.post(Entity.json(identifiable), new GenericType<Collection<IdentifiableModel>>() {
						});
			}
		});
	}

	/**
	 * This method removes an existing group member from the specified {@link GroupModel}.
	 * 
	 * @param identifiable
	 *            The member to remove from the {@link GroupModel}.
	 */
	public void removeMember(final IdentifiableModel identifiable) {
		perform(new Request<Response>() {
			@Override
			public Response perform(Client client) {
				return client.target(createUrl(group.getPath() + "/members/" + encode(identifiable.getName())))
						.request()
						.delete(Response.class);
			}
		});
	}

}
