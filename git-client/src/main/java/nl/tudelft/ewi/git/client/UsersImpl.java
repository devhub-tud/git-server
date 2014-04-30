package nl.tudelft.ewi.git.client;

import java.util.List;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
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

	UsersImpl(String host) {
		super(host);
	}
	
	@Override
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
	
	@Override
	public UserModel retrieve(final String userName) {
		return perform(new Request<UserModel>() {
			@Override
			public UserModel perform(Client client) {
				return client.target(createUrl(BASE_PATH + "/" + encode(userName)))
						.request(MediaType.APPLICATION_JSON)
						.get(UserModel.class);
			}
		});
	}

	@Override
	public UserModel retrieve(final UserModel model) {
		return perform(new Request<UserModel>() {
			@Override
			public UserModel perform(Client client) {
				return client.target(createUrl(model.getPath()))
						.request(MediaType.APPLICATION_JSON)
						.get(UserModel.class);
			}
		});
	}
	
	@Override
	public UserModel create(final UserModel newUser) {
		return perform(new Request<UserModel>() {
			@Override
			public UserModel perform(Client client) {
				return client.target(createUrl(BASE_PATH))
						.request(MediaType.APPLICATION_JSON)
						.post(Entity.json(newUser), UserModel.class);
			}
		});
	}

	@Override
	public UserModel ensureExists(String name) {
		UserModel model = new UserModel();
		model.setName(name);
		return ensureExists(model);
	}

	@Override
	public UserModel ensureExists(final UserModel model) {
		try {
			return retrieve(model.getName());
		}
		catch (NotFoundException e) {
			return create(model);
		}
	}

	@Override
	public void delete(final IdentifiableModel user) {
		perform(new Request<Response>() {
			@Override
			public Response perform(Client client) {
				return client.target(createUrl(user.getPath()))
						.request()
						.delete(Response.class);
			}
		});
	}

	@Override
	public SshKeys sshKeys(UserModel user) {
		return new SshKeysImpl(getHost(), user);
	}

}
