package nl.tudelft.ewi.git.client;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.tudelft.ewi.git.models.SshKeyModel;
import nl.tudelft.ewi.git.models.UserModel;

public class SshKeys extends Backend {

	private final String path;
	
	SshKeys(String host, String name) {
		super(host);
		this.path = "/api/users/" + name;
	}
	
	SshKeys(String host, UserModel user) {
		super(host);
		this.path = user.getPath();
	}

	public List<SshKeyModel> retrieveAll() {
		return perform(new Request<List<SshKeyModel>>() {
			@Override
			public List<SshKeyModel> perform(Client client) {
				return client.target(createUrl(path + "/keys"))
						.request(MediaType.APPLICATION_JSON)
						.get(new GenericType<List<SshKeyModel>>() {});
			}
		});
	}
	
	public SshKeyModel retrieve(final String keyName) {
		return perform(new Request<SshKeyModel>() {
			@Override
			public SshKeyModel perform(Client client) {
				return client.target(createUrl(path + "/keys/" + keyName))
						.request(MediaType.APPLICATION_JSON)
						.get(SshKeyModel.class);
			}
		});
	}
	
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
