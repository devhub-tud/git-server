package nl.tudelft.ewi.git.client;

import java.util.List;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.git.models.UserModel;

/**
 * This class allows you to query and manipulate users on the git-server.
 */
public class UsersImpl extends Backend implements Users {

	private static final String BASE_PATH = "/api/users";

	UsersImpl(Client client, String host) {
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
	public UserModel retrieve(final String userName) throws GitClientException {
		return perform(new Request<UserModel>() {
			@Override
			public UserModel perform(WebTarget target) {
				return target.path(BASE_PATH).path(encode(userName))
						.request(MediaType.APPLICATION_JSON)
						.get(UserModel.class);
			}
		});
	}

	@Override
	public UserModel retrieve(final UserModel model) throws GitClientException {
		return perform(new Request<UserModel>() {
			@Override
			public UserModel perform(WebTarget target) {
				return target.path(model.getPath())
						.request(MediaType.APPLICATION_JSON)
						.get(UserModel.class);
			}
		});
	}
	
	@Override
	public UserModel create(final UserModel newUser) throws GitClientException {
		return perform(new Request<UserModel>() {
			@Override
			public UserModel perform(WebTarget target) {
				return target.path(BASE_PATH)
						.request(MediaType.APPLICATION_JSON)
						.post(Entity.json(newUser), UserModel.class);
			}
		});
	}

	@Override
	public UserModel ensureExists(String name) throws GitClientException {
		UserModel model = new UserModel();
		model.setName(name);
		return ensureExists(model);
	}

	@Override
	public UserModel ensureExists(final UserModel model) throws GitClientException {
		try {
			return retrieve(model.getName());
		}
		catch (NotFoundException e) {
			return create(model);
		}
	}

	@Override
	public void delete(final IdentifiableModel user) throws GitClientException {
		perform(new Request<Response>() {
			@Override
			public Response perform(WebTarget target) {
				return target.path(user.getPath())
						.request()
						.delete(Response.class);
			}
		});
	}

	@Override
	public SshKeys sshKeys(UserModel user) {
		return new SshKeysImpl(client, getHost(), user);
	}

}
