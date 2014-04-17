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
public class Repositories extends Backend {

	private static final String BASE_PATH = "/api/repositories";

	Repositories(String host) {
		super(host);
	}

	/**
	 * @return All currently active {@link RepositoryModel} objects on the git-server.
	 */
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

	/**
	 * This method retrieves the specified {@link RepositoryModel} from the git-server.
	 * 
	 * @param model
	 *            The {@link RepositoryModel} to retrieve from the git-server.
	 * @return The retrieved {@link RepositoryModel} object.
	 */
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

	/**
	 * This mehtod retrieves the specified {@link RepositoryModel} from the git-server.
	 * 
	 * @param name
	 *            The name of the {@link RepositoryModel} to retrieve from the git-server.
	 * @return The retrieved {@link RepositoryModel} object.
	 */
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

	/**
	 * This method creates a new {@link RepositoryModel} on the git-server.
	 * 
	 * @param newRepository
	 *            The new {@link RepositoryModel} to provision on the git-server.
	 * @return The created {@link RepositoryModel} on the git-server.
	 */
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

	/**
	 * This method deletes the specified {@link RepositoryModel} from the git-server.
	 * 
	 * @param repository
	 *            The {@link RepositoryModel} to remove from the git-server.
	 */
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

	/**
	 * This method lists all commits in the {@link RepositoryModel} on the git-server.
	 * 
	 * @param repository
	 *            The {@link RepositoryModel} to list all commits for.
	 * @return A {@link List} of all {@link CommitModel} object for the specified repository.
	 */
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

	/**
	 * This method retrieves a specific commit in the {@link RepositoryModel} on the git-server.
	 * 
	 * @param repository
	 *            The {@link RepositoryModel} to list all commits for.
	 * @param commitId
	 *            The commit to retrieve.
	 * @return A {@link CommitModel} object from the specified repository.
	 */
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

	/**
	 * This method lists all diffs for the two specified commit IDs.
	 * 
	 * @param repository
	 *            The {@link RepositoryModel} to fetch the diffs for.
	 * @param oldCommitId
	 *            The first commit ID.
	 * @param newCommitId
	 *            The second commit ID.
	 * @return A {@link List} of all {@link DiffModel} objects.
	 */
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

	/**
	 * THis method lists all entries on the specified path of the specified repository at the
	 * specified commit ID.
	 * 
	 * @param repository
	 *            The {@link RepositoryModel} to inspect.
	 * @param commitId
	 *            The commit ID to inspect.
	 * @param path
	 *            The path to list all files and folders of.
	 * @return A {@link List} with files and directory names.
	 */
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

	/**
	 * This method retrieves the contents of a file at the specified commit ID of the repository.
	 * 
	 * @param repository
	 *            The {@link RepositoryModel} to inspect.
	 * @param commitId
	 *            The commit ID to inspect.
	 * @param path
	 *            The path of the file to inspect.
	 * @return The contents of the specified file.
	 */
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
