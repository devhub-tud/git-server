package nl.tudelft.ewi.git.web;

import java.io.File;
import java.io.IOException;
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
import javax.ws.rs.core.Response;

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
import nl.tudelft.ewi.git.models.*;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;
import nl.tudelft.ewi.git.web.security.RequireAuthentication;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.lib.ObjectLoader;
import org.jboss.resteasy.annotations.cache.Cache;
import org.jboss.resteasy.plugins.guice.RequestScoped;

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
		
		File repositoriesDirectory = configuration.getRepositoriesDirectory();
		File mirrorsDirectory = configuration.getMirrorsDirectory();
		FileUtils.deleteDirectory(new File(repositoriesDirectory, decode(repoId).concat(".git")));
		FileUtils.deleteDirectory(new File(mirrorsDirectory, decode(repoId)));
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
	public BranchModel retrieveBranch(@PathParam("repoId") String repoId,
			@PathParam("branchName") String branchName)
			throws IOException, ServiceUnavailable, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		return inspector.getBranch(repository, branchName);
	}

	@POST
	@Path("{repoId}/branch/{branchName}/merge")
	public MergeResponse mergeBranch(@PathParam("repoId") String repoId,
									 @PathParam("branchName") String branchName,
									 @QueryParam("message") String message,
									 @QueryParam("name") String name,
									 @QueryParam("email") String email)
			throws IOException, ServiceUnavailable, GitException, GitAPIException  {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));

		Person person = new Person();
		person.setEmail(email);
		person.setName(name);

		return inspector.merge(repository, branchName, person, message);
	}

	@POST
	@Path("{repoId}/tag")
	public TagModel addTag(@PathParam("repoId") String repoId,
						   @Valid TagModel tagModel)
			throws IOException, ServiceUnavailable, GitException, GitAPIException  {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		return inspector.tag(repository, tagModel);
	}

	@GET
	@Path("{repoId}/branch/{branchName}/commits")
	public CommitSubList retrieveCommitsInBracn(@PathParam("repoId") String repoId,
												@PathParam("branchName") String branchName,
												@QueryParam("skip") @DefaultValue("0") int skip,
												@QueryParam("limit") @DefaultValue("25") int limit)
			throws IOException, ServiceUnavailable, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		BranchModel branch = inspector.getBranch(repository, branchName);
		int size = inspector.sizeOfBranch(repository, branch);
		List<CommitModel> commits = inspector.listCommitsInBranch(repository, branch, skip, limit);

		CommitSubList result = new CommitSubList();
		result.setSkip(skip);
		result.setLimit(limit);
		result.setTotal(size);
		result.setCommits(commits);
		return result;
	}

	@GET
	@Path("{repoId}/branch/{branchName}/merge-base")
	public CommitModel mergeBase(@PathParam("repoId") String repoId,
								 @PathParam("branchName") String branchName)
			throws ServiceUnavailable, GitException, IOException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));

		BranchModel master = inspector.getBranch(repository, "master");
		BranchModel branch = inspector.getBranch(repository, branchName);
		CommitModel masterCommit = master.getCommit();
		CommitModel branchCommit = branch.getCommit();

		return inspector.mergeBase(repository, masterCommit.getCommit(), branchCommit.getCommit());
	}

	@DELETE
	@Path("{repoId}/branch/{branchName}")
	public void deleteBranch(@PathParam("repoId") String repoId,
									@PathParam("branchName") String branchName)
			throws ServiceUnavailable, GitException, IOException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		inspector.deleteBranch(repository, branchName);
	}

	@GET
	@Path("{repoId}/branch/{branchName}/diff-blame")
	public DiffBlameModel branchDiffBlame(@PathParam("repoId") String repoId,
										  @PathParam("branchName") String branchName,
										  @DefaultValue("3") @QueryParam("context") int context)
			throws ServiceUnavailable, GitException, IOException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));

		BranchModel master = inspector.getBranch(repository, "master");
		BranchModel branch = inspector.getBranch(repository, branchName);

		CommitModel masterCommit = master.getCommit();
		CommitModel branchCommit = branch.getCommit();
		CommitModel mergeBase = inspector.mergeBase(repository, masterCommit.getCommit(), branchCommit.getCommit());

		DiffModel diffs = inspector.calculateDiff(repository, mergeBase.getCommit(), branchCommit.getCommit(), context);
		return Transformers.DiffBlameModelTransformer(inspector, repository).apply(diffs);
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
	 * Fetch a DiffModel between two commits
	 * @param repoId Repository
	 * @param commitId New commit id
	 * @param context amount of context lines (default: 3)
	 * @return The DiffBlameModel
	 * @throws IOException If an I/O Exception occurs
	 * @throws ServiceUnavailable If the service is unavailable
	 * @throws GitException If an GitException occurs
	 */
	@GET
	@Path("{repoId}/commits/{commitId}/diff")
	public DiffModel retrieveCommitDiff(@PathParam("repoId") String repoId,
										@PathParam("commitId") String commitId,
										@DefaultValue("3") @QueryParam("context") int context)
			throws IOException, ServiceUnavailable, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));

		CommitModel commit = inspector.retrieveCommit(repository, commitId);
		String[] parents = commit.getParents();
		String parentId = parents.length > 0 ? inspector.retrieveCommit(repository, parents[0]).getCommit() : null;

		return inspector.calculateDiff(repository, parentId, commit.getCommit(), context);
	}

	/**
	 * Fetch a DiffModel between two commits
	 * @param repoId Repository
	 * @param commitId New commit id
	 * @param oldCommitId Old commit id
	 * @param context amount of context lines (default: 3)
	 * @return The DiffBlameModel
	 * @throws IOException If an I/O Exception occurs
	 * @throws ServiceUnavailable If the service is unavailable
	 * @throws GitException If an GitException occurs
	 */
	@GET
	@Path("{repoId}/commits/{commitId}/diff/{oldCommitId}")
	public DiffModel retrieveCommitDiffCmp(@PathParam("repoId") String repoId,
										   @PathParam("commitId") String commitId,
										   @PathParam("oldCommitId") String oldCommitId,
										   @DefaultValue("3") @QueryParam("context") int context)
			throws IOException, ServiceUnavailable, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		return inspector.calculateDiff(repository, oldCommitId, commitId, context);
	}

	/**
	 * Fetch a DiffBlameModel between two commits
	 * @param repoId Repository
	 * @param commitId New commit id
	 * @param context amount of context lines (default: 3)
	 * @return The DiffBlameModel
	 * @throws IOException If an I/O Exception occurs
	 * @throws ServiceUnavailable If the service is unavailable
	 * @throws GitException If an GitException occurs
	 */
	@GET
	@Path("{repoId}/commits/{commitId}/diff-blame")
	public DiffBlameModel retrieveCommitDiffBlame(@PathParam("repoId") String repoId,
												  @PathParam("commitId") String commitId,
												  @DefaultValue("3") @QueryParam("context") int context)
			throws IOException, ServiceUnavailable, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));

		CommitModel commit = inspector.retrieveCommit(repository, commitId);
		String[] parents = commit.getParents();
		String parentId = parents.length > 0 ? inspector.retrieveCommit(repository, parents[0]).getCommit() : null;

		DiffModel diffs = inspector.calculateDiff(repository, parentId, commit.getCommit(), context);
		return Transformers.DiffBlameModelTransformer(inspector, repository).apply(diffs);
	}

	/**
	 * Fetch a DiffBlameModel between two commits
	 * @param repoId Repository
	 * @param commitId New commit id
	 * @param oldCommitId Old commit id
	 * @param context amount of context lines (default: 3)
	 * @return The DiffBlameModel
	 * @throws IOException If an I/O Exception occurs
	 * @throws ServiceUnavailable If the service is unavailable
	 * @throws GitException If an GitException occurs
	 */
	@GET
	@Path("{repoId}/commits/{commitId}/diff-blame/{oldCommitId}")
	public DiffBlameModel retrieveCommitDiffBlameCmp(@PathParam("repoId") String repoId,
													 @PathParam("commitId") String commitId,
													 @PathParam("oldCommitId") String oldCommitId,
													 @DefaultValue("3") @QueryParam("context") int context)
			throws IOException, ServiceUnavailable, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		DiffModel diffs = inspector.calculateDiff(repository, oldCommitId, commitId, context);
		return Transformers.DiffBlameModelTransformer(inspector, repository).apply(diffs);
	}
	
	/**
	 * Perform a git blame
	 * @param repoId
	 *            The <code>name</code> of the repository to list all diffs for.
	 * @param commitId
	 *            The base commit ID of the repository to compare all the
	 *            changes with.
	 * @param filePath
	 *            The path of the file.
	 * @return {@link BlameModel}
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@GET
	@Path("{repoId}/commits/{commitId}/blame/{filePath}")
	public BlameModel blame(@PathParam("repoId") String repoId,
							@PathParam("commitId") String commitId,
							@PathParam("filePath") String filePath)
			throws IOException, GitException, ServiceUnavailable {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		return inspector.blame(repository, commitId, filePath);
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
			throw new NotFoundException(String.format("%s not found in %s at %s", path, repoId, commitId));
		}
		return entries;
	}

	private static final int MAX_AGE = 31556926;
	
	/**
	 * This retrieves the content of a specific file of a specific repository at
	 * a specific commit ID in the Gitolite configuration.
	 * 
	 * @param repoId
	 *            The <code>name</code> of the repository to retrieve the file
	 *            from.
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
	 * @throws NotFoundException
	 *             In case the file could not be found in the commit
	 */
	@GET
	@Cache(maxAge=MAX_AGE)
	@Produces(MediaType.WILDCARD)
	@Path("{repoId}/file/{commitId}/{path}")
	public Response showFile(@PathParam("repoId") String repoId, @PathParam("commitId") String commitId,
			@PathParam("path") String path) throws IOException, ServiceUnavailable, GitException {

		Config config = manager.get();
		Repository repository = fetchRepository(config, decode(repoId));
		ObjectLoader loader = inspector.showFile(repository, decode(commitId),
				decode(path));
		String fileName = path.substring(path.lastIndexOf('/') + 1);

		return Response.ok(loader, of(loader))
				.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
				.build();
	}

	private static MediaType of(final ObjectLoader objectLoader) {
		try {
			if (!objectLoader.isLarge()
					&& !RawText.isBinary(objectLoader.getCachedBytes())) {
				return MediaType.TEXT_PLAIN_TYPE;
			}
		} catch (LargeObjectException e) {
		}
		return MediaType.APPLICATION_OCTET_STREAM_TYPE;
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

}
