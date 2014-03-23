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

public class Users extends Backend {
	
	Users(String host) {
		super(host);
	}
	
	public List<IdentifiableModel> retrieveAll() {
		return perform(new Request<List<IdentifiableModel>>() {
			@Override
			public List<IdentifiableModel> perform(Client client) {
				return client.target(createUrl("/api/users"))
						.request(MediaType.APPLICATION_JSON)
						.get(new GenericType<List<IdentifiableModel>>() {});
			}
		});
	}
	
	public UserModel retrieve(final String name) {
		return perform(new Request<UserModel>() {
			@Override
			public UserModel perform(Client client) {
				return client.target(createUrl("/api/users/" + name))
						.request(MediaType.APPLICATION_JSON)
						.get(UserModel.class);
			}
		});
	}
	
	public UserModel create(final UserModel newValues) {
		return perform(new Request<UserModel>() {
			@Override
			public UserModel perform(Client client) {
				return client.target(createUrl("/api/users"))
						.request(MediaType.APPLICATION_JSON)
						.post(Entity.json(newValues), UserModel.class);
			}
		});
	}
	
	public UserModel ensureExists(final UserModel model) {
		try {
			return retrieve(model.getName());
		}
		catch (NotFoundException e) {
			return create(model);
		}
	}
	
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
	
	public SshKeys sshKeys(UserModel user) {
		return new SshKeys(getHost(), user);
	}
	
}
