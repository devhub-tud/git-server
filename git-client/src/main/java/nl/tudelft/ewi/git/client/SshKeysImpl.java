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
public class SshKeysImpl extends Backend implements SshKeys {

	private final String path;

	SshKeysImpl(String host, UserModel user) {
		super(host);
		this.path = user.getPath();
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
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
