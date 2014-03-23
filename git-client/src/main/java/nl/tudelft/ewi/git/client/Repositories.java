package nl.tudelft.ewi.git.client;

import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.SneakyThrows;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel;

public class Repositories extends Backend {
	
	Repositories(String host) {
		super(host);
	}
	
	public List<RepositoryModel> retrieveAll() {
		return perform(new Request<List<RepositoryModel>>() {
			@Override
			public List<RepositoryModel> perform(Client client) {
				return client.target(createUrl("/api/repositories"))
						.request(MediaType.APPLICATION_JSON)
						.get(new GenericType<List<RepositoryModel>>() {});
			}
		});
	}
	
	public DetailedRepositoryModel retrieve(final RepositoryModel model) {
		return retrieve(model.getName());
	}
	
	public DetailedRepositoryModel retrieve(final String name) {
		return perform(new Request<DetailedRepositoryModel>() {
			@Override
			@SneakyThrows
			public DetailedRepositoryModel perform(Client client) {
				return client.target(createUrl("/api/repositories/" + URLEncoder.encode(name, "UTF-8")))
						.request(MediaType.APPLICATION_JSON)
						.get(DetailedRepositoryModel.class);
			}
		});
	}
	
	public DetailedRepositoryModel create(final RepositoryModel newValues) {
		return perform(new Request<DetailedRepositoryModel>() {
			@Override
			public DetailedRepositoryModel perform(Client client) {
				return client.target(createUrl("/api/repositories"))
						.request(MediaType.APPLICATION_JSON)
						.post(Entity.json(newValues), DetailedRepositoryModel.class);
			}
		});
	}
	
	public void delete(final RepositoryModel repository) {
		perform(new Request<Response>() {
			@Override
			public Response perform(Client client) {
				return client.target(createUrl(repository.getPath()))
						.request()
						.delete(Response.class);
			}
		});
	}
	
}
