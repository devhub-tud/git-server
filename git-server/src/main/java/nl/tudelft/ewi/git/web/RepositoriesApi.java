package nl.tudelft.ewi.git.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nl.minicom.gitolite.manager.exceptions.GitException;
import nl.minicom.gitolite.manager.exceptions.ModificationException;
import nl.minicom.gitolite.manager.exceptions.ServiceUnavailable;
import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.ConfigManager;
import nl.minicom.gitolite.manager.models.Group;
import nl.minicom.gitolite.manager.models.Permission;
import nl.minicom.gitolite.manager.models.Repository;
import nl.minicom.gitolite.manager.models.User;
import nl.tudelft.ewi.git.inspector.Inspector;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.models.Transformers;
import nl.tudelft.ewi.git.web.security.RequireAuthentication;

import org.jboss.resteasy.plugins.guice.RequestScoped;
import org.jboss.resteasy.plugins.validation.hibernate.ValidateRequest;

import com.google.common.collect.Collections2;

/**
 * This class is a RESTEasy resource which provides an interface to users over HTTP to retrieve,
 * list, create, and remove repositories in the Gitolite configuration.
 * 
 * @author michael
 */
@Path("api/repositories")
@RequestScoped
@ValidateRequest
@RequireAuthentication
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RepositoriesApi extends BaseApi {

	private final ConfigManager manager;
	private final Inspector inspector;

	@Inject
	RepositoriesApi(ConfigManager manager, Inspector inspector) {
		this.manager = manager;
		this.inspector = inspector;
	}

	/**
	 * This will list all repositories currently in the Gitolite configuration.
	 * 
	 * @return A {@link Collection} of {@link RepositoryModel}s, each representing a repository in
	 *         the Gitolite configuration.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@GET
	public Collection<RepositoryModel> listAllRepositories() throws IOException, ServiceUnavailable, GitException {
		Config config = manager.get();
		return Collections2.transform(config.getRepositories(), Transformers.repositories());
	}

	/**
	 * This will retrieve a representation of a specific repository in the Gitolite configuration.
	 * 
	 * @param repoId
	 *            The <code>name</code> of the repository to retrieve.
	 * @return A {@link DetailedRepositoryModel} representation of the specified repository.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@GET
	@Path("{repoId}")
	public DetailedRepositoryModel showRepository(@PathParam("repoId") String repoId) throws IOException,
			ServiceUnavailable, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		return Transformers.detailedRepositories(inspector).apply(repository);
	}

	/**
	 * This creates a new repository in the Gitolite configuration and returns a representation of
	 * it.
	 * 
	 * @param model
	 *            A {@link RepositoryModel} describing the properties of the repository.
	 * @return A {@link DetailedRepositoryModel} representing the final properties of the created
	 *         repository.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws ModificationException
	 *             If the modification conflicted with another request.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@POST
	public DetailedRepositoryModel createRepository(@Valid RepositoryModel model) throws IOException,
			ServiceUnavailable,
			ModificationException, GitException {

		Config config = manager.get();
		Repository repository = config.createRepository(model.getName());

		for (Entry<String, String> permission : model.getPermissions().entrySet()) {
			String identifiable = permission.getKey();
			Permission level = Permission.getByLevel(permission.getValue());
			if (identifiable.startsWith("@")) {
				Group group = fetchGroup(config, identifiable);
				repository.setPermission(group, level);
			}
			else {
				User user = fetchUser(config, identifiable);
				repository.setPermission(user, level);
			}
		}

		repository.setPermission(fetchUser(config, "git"), Permission.ALL);

		manager.apply(config);
		return Transformers.detailedRepositories(inspector).apply(repository);
	}

	/**
	 * This updates an existing repository in the Gitolite configuration and returns a
	 * representation of it.
	 * 
	 * @param model
	 *            A {@link RepositoryModel} describing the properties of the repository.
	 * @return A {@link DetailedRepositoryModel} representing the final properties of the updated
	 *         repository.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws ModificationException
	 *             If the modification conflicted with another request.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@PUT
	@Path("{repoId}")
	public DetailedRepositoryModel updateRepository(@PathParam("repoId") String repoId, @Valid RepositoryModel model)
			throws IOException, ServiceUnavailable,
			ModificationException, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));

		for (Entry<String, String> permission : model.getPermissions().entrySet()) {
			String identifiable = permission.getKey();
			Permission level = Permission.getByLevel(permission.getValue());
			if (identifiable.startsWith("@")) {
				Group group = fetchGroup(config, identifiable);
				repository.setPermission(group, level);
			}
			else {
				User user = fetchUser(config, identifiable);
				repository.setPermission(user, level);
			}
		}

		repository.setPermission(fetchUser(config, "git"), Permission.ALL);

		manager.apply(config);
		return Transformers.detailedRepositories(inspector).apply(repository);
	}

	/**
	 * This removes an existing repository from the Gitolite configuration.
	 * 
	 * @param repoId
	 *            The <code>name</code> of the repository to remove.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws ModificationException
	 *             If the modification conflicted with another request.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@DELETE
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("{repoId}")
	public void deleteRepository(@PathParam("repoId") String repoId) throws IOException, ServiceUnavailable,
			ModificationException, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		config.removeRepository(repository);
		manager.apply(config);
	}

	/**
	 * This lists all the commits of a specific repository in the Gitolite configuration.
	 * 
	 * @param repoId
	 *            The <code>name</code> of the repository to list all commits of.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@GET
	@Path("{repoId}/commits")
	public List<CommitModel> listCommits(@PathParam("repoId") String repoId) throws IOException, ServiceUnavailable,
			GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		return inspector.listCommits(repository);
	}

	/**
	 * This lists all the diffs of a specific repository between two specified commit IDs in the
	 * Gitolite configuration.
	 * 
	 * @param repoId
	 *            The <code>name</code> of the repository to list all diffs for.
	 * @param oldId
	 *            The base commit ID of the repository to compare all the changes with.
	 * @param newId
	 *            The reference commit ID of the repository to compare with the base commit ID.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@GET
	@Path("{repoId}/diff/{oldId}/{newId}")
	public Collection<DiffModel> calculateDiff(@PathParam("repoId") String repoId, @PathParam("oldId") String oldId,
			@PathParam("newId") String newId) throws IOException, ServiceUnavailable, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		return inspector.calculateDiff(repository, decode(oldId), decode(newId));
	}

	/**
	 * This lists all the files and folders of a specific repository at a specific commit IDs in the
	 * Gitolite configuration.
	 * 
	 * @param repoId
	 *            The <code>name</code> of the repository to list all files and folders for.
	 * @param commitId
	 *            The commit ID of the repository to list all the files and folders for.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@GET
	@Path("{repoId}/tree/{commitId}")
	public Collection<String> showTree(@PathParam("repoId") String repoId, @PathParam("commitId") String commitId)
			throws IOException, ServiceUnavailable, GitException {

		return showTree(repoId, commitId, "");
	}

	/**
	 * This lists all the files and folders of a specific repository at a specific commit ID in the
	 * Gitolite configuration.
	 * 
	 * @param repoId
	 *            The <code>name</code> of the repository to list all files and folders for.
	 * @param commitId
	 *            The commit ID of the repository to list all the files and folders for.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@GET
	@Path("{repoId}/tree/{commitId}/{path}")
	public Collection<String> showTree(@PathParam("repoId") String repoId, @PathParam("commitId") String commitId,
			@PathParam("path") String path) throws IOException, ServiceUnavailable, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		Collection<String> entries = inspector.showTree(repository, decode(commitId), decode(path));
		if (entries == null) {
			throw new NotFoundException();
		}
		return entries;
	}

	/**
	 * This retrieves the content of a specific file of a specific repository at a specific commit
	 * ID in the Gitolite configuration.
	 * 
	 * @param repoId
	 *            The <code>name</code> of the repository to retrieve the file from.
	 * @param commitId
	 *            The commit ID of the repository to retrieve the file from.
	 * @param path
	 *            The path of the file.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{repoId}/file/{commitId}/{path}")
	public InputStream showFile(@PathParam("repoId") String repoId, @PathParam("commitId") String commitId,
			@PathParam("path") String path) throws IOException, ServiceUnavailable, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		InputStream stream = inspector.showFile(repository, decode(commitId), decode(path));
		if (stream == null) {
			throw new NotFoundException();
		}
		return stream;
	}

}
