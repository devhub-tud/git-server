package nl.tudelft.ewi.git.client;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
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

	SshKeysImpl(Client client, String host, UserModel user) {
		super(client, host);
		this.path = user.getPath();
	}

	@Override
	public List<SshKeyModel> retrieveAll() throws GitClientException {
		return perform(new Request<List<SshKeyModel>>() {
			@Override
			public List<SshKeyModel> perform(WebTarget target) {
				return target.path(path).path("keys")
						.request(MediaType.APPLICATION_JSON)
						.get(new GenericType<List<SshKeyModel>>() {
						});
			}
		});
	}

	@Override
	public SshKeyModel retrieve(final String keyName) throws GitClientException {
		return perform(new Request<SshKeyModel>() {
			@Override
			public SshKeyModel perform(WebTarget target) {
				return target.path(path).path("keys").path(encode(keyName))
						.request(MediaType.APPLICATION_JSON)
						.get(SshKeyModel.class);
			}
		});
	}

	@Override
	public SshKeyModel registerSshKey(final SshKeyModel sshKey) throws GitClientException {
		return perform(new Request<SshKeyModel>() {
			@Override
			public SshKeyModel perform(WebTarget target) {
				return target.path(path).path("keys")
						.request(MediaType.APPLICATION_JSON)
						.post(Entity.json(sshKey), SshKeyModel.class);
			}
		});
	}

	@Override
	public void deleteSshKey(final SshKeyModel sshKey) throws GitClientException {
		perform(new Request<Response>() {
			@Override
			public Response perform(WebTarget target) {
				return target.path(sshKey.getPath())
						.request()
						.delete(Response.class);
			}
		});
	}

}
