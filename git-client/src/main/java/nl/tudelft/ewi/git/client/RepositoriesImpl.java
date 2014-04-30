package nl.tudelft.ewi.git.client;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.RepositoryModel;

/**
 * This class allows you query and manipulate repositories on the git-server.
 */
public class RepositoriesImpl extends Backend implements Repositories {

	private static final String BASE_PATH = "/api/repositories";

	RepositoriesImpl(String host) {
		super(host);
	}

	@Override
	public List<RepositoryModel> retrieveAll() {
		return perform(new Request<List<RepositoryModel>>() {
			@Override
			public List<RepositoryModel> perform(Client client) {
				return client.target(createUrl(BASE_PATH))
					.request(MediaType.APPLICATION_JSON)
					.get(new GenericType<List<RepositoryModel>>() {
					});
			}
		});
	}

	@Override
	public DetailedRepositoryModel retrieve(final RepositoryModel model) {
		return perform(new Request<DetailedRepositoryModel>() {
			@Override
			public DetailedRepositoryModel perform(Client client) {
				return client.target(createUrl(model.getPath()))
					.request(MediaType.APPLICATION_JSON)
					.get(DetailedRepositoryModel.class);
			}
		});
	}

	@Override
	public DetailedRepositoryModel retrieve(final String name) {
		return perform(new Request<DetailedRepositoryModel>() {
			@Override
			public DetailedRepositoryModel perform(Client client) {
				return client.target(createUrl(BASE_PATH + "/" + encode(name)))
					.request(MediaType.APPLICATION_JSON)
					.get(DetailedRepositoryModel.class);
			}
		});
	}
	
	@Override
	public DetailedRepositoryModel create(final CreateRepositoryModel newRepository) {
		return perform(new Request<DetailedRepositoryModel>() {
			@Override
			public DetailedRepositoryModel perform(Client client) {
				return client.target(createUrl(BASE_PATH))
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.json(newRepository), DetailedRepositoryModel.class);
			}
		});
	}

	@Override
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

	@Override
	public List<CommitModel> listCommits(final RepositoryModel repository) {
		return perform(new Request<List<CommitModel>>() {
			@Override
			public List<CommitModel> perform(Client client) {
				return client.target(createUrl(repository.getPath() + "/commits"))
					.request(MediaType.APPLICATION_JSON)
					.get(new GenericType<List<CommitModel>>() {
					});
			}
		});
	}

	@Override
	public CommitModel retrieveCommit(final RepositoryModel repository, final String commitId) {
		return perform(new Request<CommitModel>() {
			@Override
			public CommitModel perform(Client client) {
				return client.target(createUrl(repository.getPath() + "/commits/" + commitId))
					.request(MediaType.APPLICATION_JSON)
					.get(CommitModel.class);
			}
		});
	}

	@Override
	public List<DiffModel> listDiffs(final RepositoryModel repository, final String oldCommitId,
			final String newCommitId) {

		return perform(new Request<List<DiffModel>>() {
			@Override
			public List<DiffModel> perform(Client client) {
				String path = repository.getPath() + "/diff/" + encode(oldCommitId) + "/" + encode(newCommitId);
				return client.target(createUrl(path))
					.request(MediaType.APPLICATION_JSON)
					.get(new GenericType<List<DiffModel>>() {
					});
			}
		});
	}
	
	@Override
	public List<String> listDirectoryEntries(final RepositoryModel repository, final String commitId, final String path) {
		return perform(new Request<List<String>>() {
			@Override
			public List<String> perform(Client client) {
				return client.target(createUrl(repository.getPath() + "/tree/" + encode(commitId) + "/" + encode(path)))
					.request(MediaType.APPLICATION_JSON)
					.get(new GenericType<List<String>>() {
					});
			}
		});
	}

	@Override
	public String showFile(final RepositoryModel repository, final String commitId, final String path) {
		return perform(new Request<String>() {
			@Override
			public String perform(Client client) {
				return client.target(createUrl(repository.getPath() + "/tree/" + encode(commitId) + "/" + encode(path)))
					.request(MediaType.APPLICATION_JSON)
					.get(new GenericType<String>() {
					});
			}
		});
	}

}
