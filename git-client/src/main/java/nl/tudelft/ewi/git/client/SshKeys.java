package nl.tudelft.ewi.git.client;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.tudelft.ewi.git.models.SshKeyModel;
import nl.tudelft.ewi.git.models.UserModel;

/**
 * This class allows you to query and manipulate SSH keys on the git-server.
 */
public class SshKeys extends Backend {

	private final String path;

	SshKeys(String host, UserModel user) {
		super(host);
		this.path = user.getPath();
	}

	/**
	 * @return All SSH keys of the specified user.
	 */
	public List<SshKeyModel> retrieveAll() {
		return perform(new Request<List<SshKeyModel>>() {
			@Override
			public List<SshKeyModel> perform(Client client) {
				return client.target(createUrl(path + "/keys"))
						.request(MediaType.APPLICATION_JSON)
						.get(new GenericType<List<SshKeyModel>>() {
						});
			}
		});
	}

	/**
	 * Retrieves a specific SSH key.
	 * 
	 * @param keyName
	 *            The name of the SSH key.
	 * @return The retrieved {@link SshKeyModel} representing the SSH key.
	 */
	public SshKeyModel retrieve(final String keyName) {
		return perform(new Request<SshKeyModel>() {
			@Override
			public SshKeyModel perform(Client client) {
				return client.target(createUrl(path + "/keys/" + encode(keyName)))
						.request(MediaType.APPLICATION_JSON)
						.get(SshKeyModel.class);
			}
		});
	}

	/**
	 * This method registers a new SSH key with the git-server.
	 * 
	 * @param sshKey
	 *            The {@link SshKeyModel} to register.
	 * @return The created {@link SshKeyModel} from the git-server.
	 */
	public SshKeyModel registerSshKey(final SshKeyModel sshKey) {
		return perform(new Request<SshKeyModel>() {
			@Override
			public SshKeyModel perform(Client client) {
				return client.target(createUrl(path + "/keys"))
						.request(MediaType.APPLICATION_JSON)
						.post(Entity.json(sshKey), SshKeyModel.class);
			}
		});
	}

	/**
	 * This method removes an existing SSH key from the git-server.
	 * 
	 * @param sshKey
	 *            The {@link SshKeyModel} describing the SSH key to remove.
	 */
	public void deleteSshKey(final SshKeyModel sshKey) {
		perform(new Request<Response>() {
			@Override
			public Response perform(Client client) {
				return client.target(createUrl(sshKey.getPath()))
						.request()
						.delete(Response.class);
			}
		});
	}

}
