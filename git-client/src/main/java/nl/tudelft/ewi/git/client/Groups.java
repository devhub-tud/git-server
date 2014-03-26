package nl.tudelft.ewi.git.client;

import java.util.List;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;

/**
 * This class allows you to query and manipulate {@link GroupModel} objects on the git-server.
 */
public class Groups extends Backend {

	private static final String BASE_PATH = "/api/groups";

	Groups(String host) {
		super(host);
	}

	/**
	 * @return all {@link GroupModel} objects currently registered with the git-server.
	 */
	public List<IdentifiableModel> retrieveAll() {
		return perform(new Request<List<IdentifiableModel>>() {
			@Override
			public List<IdentifiableModel> perform(Client client) {
				return client.target(createUrl(BASE_PATH))
						.request(MediaType.APPLICATION_JSON)
						.get(new GenericType<List<IdentifiableModel>>() {
						});
			}
		});
	}

	/**
	 * This method retrieves the specified {@link GroupModel} from the git-server.
	 * 
	 * @param groupName
	 *            The name of the {@link GroupModel}.
	 * @return The retrieved {@link GroupModel} object.
	 */
	public GroupModel retrieve(final String groupName) {
		return perform(new Request<GroupModel>() {
			@Override
			public GroupModel perform(Client client) {
				return client.target(createUrl(BASE_PATH + "/" + encode(groupName)))
						.request(MediaType.APPLICATION_JSON)
						.get(GroupModel.class);
			}
		});
	}

	/**
	 * This method retrieves the specified {@link GroupModel} from the git-server.
	 * 
	 * @param model
	 *            The {@link IdentifiableModel} to retrieve.
	 * @return The retrieved {@link GroupModel}.
	 */
	public GroupModel retrieve(final IdentifiableModel model) {
		return perform(new Request<GroupModel>() {
			@Override
			public GroupModel perform(Client client) {
				return client.target(createUrl(model.getPath()))
						.request(MediaType.APPLICATION_JSON)
						.get(GroupModel.class);
			}
		});
	}

	/**
	 * This method creates a new {@link GroupModel} on the git-server.
	 * 
	 * @param newGroup
	 *            The new {@link GroupModel} to construct on the git-server.
	 * @return The created {@link GroupModel} object.
	 */
	public GroupModel create(final GroupModel newGroup) {
		return perform(new Request<GroupModel>() {
			@Override
			public GroupModel perform(Client client) {
				return client.target(createUrl(BASE_PATH))
						.request(MediaType.APPLICATION_JSON)
						.post(Entity.json(newGroup), GroupModel.class);
			}
		});
	}

	/**
	 * This method ensures that a specified {@link GroupModel} exists on the server.
	 * 
	 * @param model
	 *            The {@link GroupModel} to create if it does not yet exist.
	 * @return The created or fetched {@link GroupModel} object.
	 */
	public GroupModel ensureExists(final GroupModel model) {
		try {
			return retrieve(model.getName());
		}
		catch (NotFoundException e) {
			return create(model);
		}
	}

	/**
	 * This method deletes a {@link GroupModel} from the git-server.
	 * 
	 * @param group
	 *            The {@link IdentifiableModel} to remove from the git-server.
	 */
	public void delete(final IdentifiableModel group) {
		perform(new Request<Response>() {
			@Override
			public Response perform(Client client) {
				return client.target(createUrl(group.getPath()))
						.request()
						.delete(Response.class);
			}
		});
	}

	/**
	 * This method returns a {@link GroupMembers} object which allows you to query and manipulate
	 * members of the specified {@link GroupModel} object.
	 * 
	 * @param group
	 *            The {@link GroupModel} of which you wish to query or manipulate group members.
	 * @return The constructed {@link GroupMembers} object.
	 */
	public GroupMembers groupMembers(GroupModel group) {
		return new GroupMembers(getHost(), group);
	}

}
