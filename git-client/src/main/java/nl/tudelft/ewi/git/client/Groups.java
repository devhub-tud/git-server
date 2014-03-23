package nl.tudelft.ewi.git.client;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;

public class Groups extends Backend {
	
	Groups(String host) {
		super(host);
	}
	
	public List<IdentifiableModel> retrieveAll() {
		return perform(new Request<List<IdentifiableModel>>() {
			@Override
			public List<IdentifiableModel> perform(Client client) {
				return client.target(createUrl("/api/groups"))
						.request(MediaType.APPLICATION_JSON)
						.get(new GenericType<List<IdentifiableModel>>() {});
			}
		});
	}
	
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
	
	public GroupModel create(final GroupModel newValues) {
		return perform(new Request<GroupModel>() {
			@Override
			public GroupModel perform(Client client) {
				return client.target(createUrl("/api/groups"))
						.request(MediaType.APPLICATION_JSON)
						.post(Entity.json(newValues), GroupModel.class);
			}
		});
	}
	
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
	
}
