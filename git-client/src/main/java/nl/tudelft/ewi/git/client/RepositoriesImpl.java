package nl.tudelft.ewi.git.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.File;
import java.util.List;
import java.util.Map;

import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.DetailedBranchModel;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffResponse;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.models.RepositoryModel;

/**
 * This class allows you query and manipulate repositories on the git-server.
 */
public class RepositoriesImpl extends Backend implements Repositories {

	private static final String BASE_PATH = "/api/repositories";

	RepositoriesImpl(Client client, String host) {
		super(client, host);
	}

	@Override
	public List<RepositoryModel> retrieveAll() {
		return perform(new Request<List<RepositoryModel>>() {
			@Override
			public List<RepositoryModel> perform(WebTarget target) {
				return target.path(BASE_PATH)
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
			public DetailedRepositoryModel perform(WebTarget target) {
				return target.path(model.getPath())
					.request(MediaType.APPLICATION_JSON)
					.get(DetailedRepositoryModel.class);
			}
		});
	}

	@Override
	public DetailedRepositoryModel retrieve(final String name) {
		return perform(new Request<DetailedRepositoryModel>() {
			@Override
			public DetailedRepositoryModel perform(WebTarget target) {
				return target.path(BASE_PATH).path(encode(name))
					.request(MediaType.APPLICATION_JSON)
					.get(DetailedRepositoryModel.class);
			}
		});
	}
	
	@Override
	public DetailedRepositoryModel create(final CreateRepositoryModel newRepository) {
		return perform(new Request<DetailedRepositoryModel>() {
			@Override
			public DetailedRepositoryModel perform(WebTarget target) {
				return target.path(BASE_PATH)
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.json(newRepository), DetailedRepositoryModel.class);
			}
		});
	}

	@Override
	public void delete(final RepositoryModel repository) {
		perform(new Request<Response>() {
			@Override
			public Response perform(WebTarget target) {
				return target.path(repository.getPath())
					.request()
					.delete(Response.class);
			}
		});
	}

	@Override
	public List<CommitModel> listCommits(final RepositoryModel repository) {
		return perform(new Request<List<CommitModel>>() {
			@Override
			public List<CommitModel> perform(WebTarget target) {
				return target.path(repository.getPath()).path("commits")
					.request(MediaType.APPLICATION_JSON)
					.get(new GenericType<List<CommitModel>>() {
					});
			}
		});
	}
	
	@Override
	public DetailedBranchModel retrieveBranch(final RepositoryModel repository,
			final String branchName) {
		return retrieveBranch(repository, branchName, 0, Integer.MAX_VALUE);
	}
	
	@Override
	public DetailedBranchModel retrieveBranch(final RepositoryModel repository,
			final String branchName, final int skip, final int limit) {
		return perform(new Request<DetailedBranchModel>() {
			@Override
			public DetailedBranchModel perform(WebTarget target) {
				return target.path(repository.getPath()).path("branch").path(encode(branchName))
					.queryParam("skip", skip)
					.queryParam("limit", limit)
					.request(MediaType.APPLICATION_JSON)
					.get(new GenericType<DetailedBranchModel>() {
					});
			}
		});
	}

	@Override
	public DetailedCommitModel retrieveCommit(final RepositoryModel repository, final String commitId) {
		return perform(new Request<DetailedCommitModel>() {
			@Override
			public DetailedCommitModel perform(WebTarget target) {
				return target.path(repository.getPath()).path("commits").path(commitId)
					.request(MediaType.APPLICATION_JSON)
					.get(DetailedCommitModel.class);
			}
		});
	}

	@Override
	public DiffResponse listDiffs(RepositoryModel repository, String newCommitId) {
		return listDiffs(repository, null, newCommitId);
	}

	@Override
	public DiffResponse listDiffs(final RepositoryModel repository, final String oldCommitId,
			final String newCommitId) {

		return perform(new Request<DiffResponse>() {
			@Override
			public DiffResponse perform(WebTarget target) {
				target = target.path(repository.getPath()).path("diff");

				if(oldCommitId != null) {
					target = target.path(encode(oldCommitId));
				}

				return target.path(encode(newCommitId))
					.request(MediaType.APPLICATION_JSON)
					.get(DiffResponse.class);
			}
		});
	}
	
	@Override
	public DiffResponse listDiffs(final RepositoryModel repository, final String oldCommitId,
			final String newCommitId, final int context) {

		return perform(new Request<DiffResponse>() {
			@Override
			public DiffResponse perform(WebTarget target) {
				target = target.path(repository.getPath()).path("diff");

				if(oldCommitId != null) {
					target = target.path(encode(oldCommitId));
				}

				return target.path(encode(newCommitId))
					.queryParam("contextLines", context)
					.request(MediaType.APPLICATION_JSON)
					.get(DiffResponse.class);
			}
		});
	}
	
	@Override
	public Map<String, EntryType> listDirectoryEntries(final RepositoryModel repository, final String commitId, final String path) {
		return perform(new Request<Map<String, EntryType>>() {
			@Override
			public Map<String, EntryType> perform(WebTarget target) {
				return target.path(repository.getPath()).path("tree").path(encode(commitId)).path(encode(path))
					.request(MediaType.APPLICATION_JSON)
					.get(new GenericType<Map<String, EntryType>>() {
					});
			}
		});
	}

	@Override
	public String showFile(final RepositoryModel repository, final String commitId, final String path) {
		return perform(new Request<String>() {
			@Override
			public String perform(WebTarget target) {
				return target.path(repository.getPath()).path("file").path(encode(commitId)).path(encode(path))
					.request(MediaType.MULTIPART_FORM_DATA)
					.get(String.class);
			}
		});
	}
	
	@Override
	public File showBinFile(final RepositoryModel repository, final String commitId, final String path) {
		return perform(new Request<File>() {
			@Override
			public File perform(WebTarget target) {
				return target
					.path(repository.getPath())
					.path("file")
					.path(encode(commitId))
					.path(encode(path))
					.request(MediaType.WILDCARD_TYPE)
					.get(File.class);
			}
		});
	}

}
