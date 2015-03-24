package nl.tudelft.ewi.git.web;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;
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
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.DetailedBranchModel;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;
import nl.tudelft.ewi.git.models.Transformers;
import nl.tudelft.ewi.git.web.security.RequireAuthentication;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jboss.resteasy.plugins.guice.RequestScoped;
import org.jboss.resteasy.plugins.validation.hibernate.ValidateRequest;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.io.Files;

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
@Slf4j
public class RepositoriesApi extends BaseApi {

	private final ConfigManager manager;
	private final Inspector inspector;
	private final nl.tudelft.ewi.git.Config configuration;

	@Inject
	RepositoriesApi(nl.tudelft.ewi.git.Config configuration, ConfigManager manager, Inspector inspector) {
		this.configuration = configuration;
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
		return Collections2.transform(config.getRepositories(), Transformers.repositories(configuration));
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
		return Transformers.detailedRepositories(configuration, inspector)
			.apply(repository);
	}

	/**
	 * This creates a new repository in the Gitolite configuration and returns a representation of
	 * it.
	 * 
	 * @param model
	 *            A {@link CreateRepositoryModel} describing the properties of the repository.
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
	public DetailedRepositoryModel createRepository(@Valid CreateRepositoryModel model) throws IOException,
			ServiceUnavailable, ModificationException, GitException {

		Config config = manager.get();
		Repository repository = config.createRepository(model.getName());

		for (Entry<String, Level> permission : model.getPermissions()
			.entrySet()) {
			String identifiable = permission.getKey();
			Permission level = transformLevel(permission.getValue());
			if (identifiable.startsWith("@")) {
				Group group = fetchGroup(config, identifiable);
				repository.setPermission(group, level);
			}
			else {
				User user = fetchUser(config, identifiable);
				repository.setPermission(user, level);
			}
		}

		repository.setPermission(fetchUser(config, configuration.getGitoliteAdmin()), Permission.ALL);

		manager.apply(config);

		if (!Strings.isNullOrEmpty(model.getTemplateRepository())) {
			try {
				DetailedRepositoryModel provisionedRepository = showRepository(model.getName());
				pullCommitsFromRemoteRepository(model.getTemplateRepository(), provisionedRepository.getUrl());
			}
			catch (GitException e) {
				log.error(e.getMessage(), e);
				
				config = manager.get();
				config.removeRepository(config.getRepository(model.getName()));
				manager.apply(config);
			}
		}

		return Transformers.detailedRepositories(configuration, inspector)
			.apply(repository);
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
			throws IOException, ServiceUnavailable, ModificationException, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));

		for (Entry<String, Level> permission : model.getPermissions()
			.entrySet()) {
			String identifiable = permission.getKey();
			Permission level = transformLevel(permission.getValue());
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
		return Transformers.detailedRepositories(configuration, inspector)
			.apply(repository);
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
		
		if (!Strings.isNullOrEmpty(configuration.getRepositoriesDirectory())) {
			File topDirectory = new File(configuration.getRepositoriesDirectory());
			delete(topDirectory, new File(topDirectory, decode(repoId)));
		}
		
		if (!Strings.isNullOrEmpty(configuration.getMirrorsDirectory())) {
			File topDirectory = new File(configuration.getMirrorsDirectory());
			delete(topDirectory, new File(topDirectory, decode(repoId)));
		}
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
	
	@GET
	@Path("{repoId}/branch/{branchName}")
	public DetailedBranchModel retrieveBranch(@PathParam("repoId") String repoId,
			@PathParam("branchName") String branchName,
			@QueryParam("skip") @DefaultValue("0") int skip,
			@QueryParam("limit") @DefaultValue("50") int limit)
			throws IOException, ServiceUnavailable, GitException {
		

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		
		DetailedBranchModel branch = DetailedBranchModel
				.from(inspector.getBranch(repository, branchName));
		List<CommitModel> commits = inspector.listCommitsInBranch(repository, branch);
		
		int size = commits.size();
	
		if(skip < 0 || limit < 0 || skip > size) {
			throw new IllegalArgumentException();
		}
		
		branch.setCommits(commits.subList(skip, Math.min(size, skip + limit)));
		branch.setAmountOfCommits(size);
		return branch;
	}

	/**
	 * This retrieves a specific commit of a specific repository in the Gitolite configuration.
	 * 
	 * @param repoId
	 *            The <code>name</code> of the repository to retrieve the commit from.
	 * @param commitId
	 *            The <code>commit</code> to retrieve.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@GET
	@Path("{repoId}/commits/{commitId}")
	public DetailedCommitModel retrieveCommit(@PathParam("repoId") String repoId, @PathParam("commitId") String commitId)
			throws IOException, ServiceUnavailable, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		return inspector.retrieveCommit(repository, commitId);
	}
	
	/**
	 * This lists all the diffs of a specific repository between two specified commit IDs in the
	 * Gitolite configuration.
	 * 
	 * @param repoId
	 *            The <code>name</code> of the repository to list all diffs for.
	 * @param commitId
	 *            The commit ID of the repository to fetch the diff for.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@GET
	@Path("{repoId}/diff/{commitId}")
	public Collection<DiffModel> calculateDiff(@PathParam("repoId") String repoId, 
			@PathParam("commitId") String commitId) throws IOException, ServiceUnavailable, GitException {
		
		return calculateDiff(repoId, commitId, null);
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
		
		if (Strings.isNullOrEmpty(newId)) {
			CommitModel commit = retrieveCommit(repoId, oldId);
			if(commit.getParents().length == 0) {
				return inspector.calculateDiff(repository, decode(oldId));
			} else {
				// Set the newId to the oldId, and append a ^ to the old id to get its parent
				newId = oldId;
				oldId += "^";
			}
		}
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
	public Map<String, EntryType> showTree(@PathParam("repoId") String repoId, @PathParam("commitId") String commitId)
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
	public Map<String, EntryType> showTree(@PathParam("repoId") String repoId, @PathParam("commitId") String commitId,
			@PathParam("path") String path) throws IOException, ServiceUnavailable, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		Map<String, EntryType> entries = inspector.showTree(repository, decode(commitId), decode(path));
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
	@Produces(MediaType.MULTIPART_FORM_DATA)
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

	private void pullCommitsFromRemoteRepository(String templateUrl, String repoUrl) throws GitException {
		File dir = Files.createTempDir();

		try {
			Git repo = Git.cloneRepository()
				.setDirectory(dir)
				.setURI(templateUrl)
				.setCloneAllBranches(true)
				.setCloneSubmodules(true)
				.call();

			repo.push()
				.setRemote(repoUrl)
				.setPushAll()
				.setPushTags()
				.call();
		}
		catch (GitAPIException e) {
			log.warn(e.getMessage(), e);
			throw new GitException(e);
		}
		finally {
			dir.delete();
		}
	}

	private Permission transformLevel(Level level) {
		Preconditions.checkNotNull(level);

		switch (level) {
			case ADMIN:
				return Permission.ALL;
			case READ_ONLY:
				return Permission.READ_ONLY;
			case READ_WRITE:
				return Permission.READ_WRITE;
			default:
				throw new IllegalArgumentException("Level: " + level + " is not supported!");
		}
	}

	private void delete(File topDirectory, File handle) {
		try {
			FileUtils.deleteDirectory(handle);
			
			File parentFile = handle;
			while (!((parentFile = parentFile.getParentFile()).equals(topDirectory))) {
				File[] listFiles = parentFile.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return true;
					}
				});
				
				if (listFiles.length == 0) {
					FileUtils.deleteDirectory(parentFile);
				}
				else {
					break;
				}
			}
		}
		catch (Throwable e) {
			log.warn(e.getMessage(), e);
		}
	}

}
