package nl.tudelft.ewi.git.client;

import java.util.List;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;

/**
 * This class allows you to query and manipulate {@link GroupModel} objects on the git-server.
 */
public class GroupsImpl extends Backend implements Groups {

	private static final String BASE_PATH = "/api/groups";

	GroupsImpl(Client client, String host) {
		super(client, host);
	}

	@Override
	public List<IdentifiableModel> retrieveAll() throws GitClientException {
		return perform(new Request<List<IdentifiableModel>>() {
			@Override
			public List<IdentifiableModel> perform(WebTarget target) {
				return target.path(BASE_PATH)
						.request(MediaType.APPLICATION_JSON)
						.get(new GenericType<List<IdentifiableModel>>() {
						});
			}
		});
	}

	@Override
	public GroupModel retrieve(final String groupName) throws GitClientException {
		return perform(new Request<GroupModel>() {
			@Override
			public GroupModel perform(WebTarget target) {
				return target.path(BASE_PATH).path(encode(groupName))
						.request(MediaType.APPLICATION_JSON)
						.get(GroupModel.class);
			}
		});
	}

	@Override
	public GroupModel retrieve(final IdentifiableModel model) throws GitClientException {
		return perform(new Request<GroupModel>() {
			@Override
			public GroupModel perform(WebTarget target) {
				return target.path(model.getPath())
						.request(MediaType.APPLICATION_JSON)
						.get(GroupModel.class);
			}
		});
	}

	@Override
	public GroupModel create(final GroupModel newGroup) throws GitClientException {
		return perform(new Request<GroupModel>() {
			@Override
			public GroupModel perform(WebTarget target) {
				return target.path(BASE_PATH)
						.request(MediaType.APPLICATION_JSON)
						.post(Entity.json(newGroup), GroupModel.class);
			}
		});
	}

	@Override
	public GroupModel ensureExists(final GroupModel model) throws GitClientException {
		try {
			return retrieve(model.getName());
		}
		catch (NotFoundException e) {
			return create(model);
		}
	}

	@Override
	public void delete(final IdentifiableModel group) throws GitClientException {
		perform(new Request<Response>() {
			@Override
			public Response perform(WebTarget target) {
				return target.path(group.getPath())
						.request()
						.delete(Response.class);
			}
		});
	}

	@Override
	public GroupMembers groupMembers(GroupModel group) {
		return new GroupMembersImpl(client, getHost(), group);
	}

}
