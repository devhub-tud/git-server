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
public class Users extends Backend {

	private static final String BASE_PATH = "/api/users";

	Users(String host) {
		super(host);
	}

	/**
	 * @return All {@link UserModel} objects currently registered on the git-server.
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
	 * This method retrieves a specific {@link UserModel} from the git-server.
	 * 
	 * @param userName
	 *            The name of the {@link UserModel} to retrieve.
	 * @return The retrieved {@link UserModel} object.
	 */
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

	/**
	 * This method retrieves a specific {@link UserModel} from the git-server.
	 * 
	 * @param model
	 *            The {@link UserModel} to retrieve from the git-server.
	 * @return The retrieved {@link UserModel} object.
	 */
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

	/**
	 * This method creates a new {@link UserModel} object on the git-server.
	 * 
	 * @param newUser
	 *            The {@link UserModel} object to create on the git-server.
	 * @return The created {@link UserModel} object.
	 */
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

	/**
	 * This method ensures that a specific {@link UserModel} exists on the git-server.
	 * 
	 * @param name
	 *            The name of the user to ensure exists on the git-server.
	 * @return The created or retrieved {@link UserModel}.
	 */
	public UserModel ensureExists(String name) {
		UserModel model = new UserModel();
		model.setName(name);
		return ensureExists(model);
	}

	/**
	 * This method ensures that a specific {@link UserModel} exists on the git-server.
	 * 
	 * @param model
	 *            The {@link UserModel} to ensure that it exists.
	 * @return The created or fetched {@link UserModel} on the git-server.
	 */
	public UserModel ensureExists(final UserModel model) {
		try {
			return retrieve(model.getName());
		}
		catch (NotFoundException e) {
			return create(model);
		}
	}

	/**
	 * This method deletes an existing {@link UserModel} from the git-server.
	 * 
	 * @param user
	 *            The {@link UserModel} to delete.
	 */
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

	/**
	 * Creates a new returns a {@link SshKeys} object allowing you to query and manipulate the SSH
	 * keys of the specified {@link UserModel} object.
	 * 
	 * @param user
	 *            The {@link UserModel} to query or manipulate the SSH keys of.
	 * @return The constructed {@link SshKeys} object.
	 */
	public SshKeys sshKeys(UserModel user) {
		return new SshKeys(getHost(), user);
	}

}
