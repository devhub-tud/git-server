package nl.tudelft.ewi.git.inspector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;

import lombok.extern.slf4j.Slf4j;
import nl.minicom.gitolite.manager.exceptions.GitException;
import nl.minicom.gitolite.manager.models.Repository;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DetailedBranchModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.DiffModel.Type;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.models.TagModel;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.ObjectStream;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.DepthWalk.Commit;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This class allows its users to inspect the contents of Git repositories. You can use this class
 * to list commits, branches and tags. In addition it can create diffs between commits, list files
 * and folders for a certain commit and inspect the contents of a specified file at a specific
 * commit ID.
 * 
 * @author michael
 */
@Slf4j
public class Inspector {

	private static EntryType of(org.eclipse.jgit.lib.Repository repo, TreeWalk walker, String entry) throws IOException {
		if (entry.endsWith("/")) {
			return EntryType.FOLDER;
		}

		ObjectId objectId = walker.getObjectId(0);
		ObjectLoader loader = repo.open(objectId);
		try (ObjectStream stream = loader.openStream()) {
			if (RawText.isBinary(stream)) {
				return EntryType.BINARY;
			}
			return EntryType.TEXT;
		}
	}

	private final File repositoriesDirectory;

	/**
	 * Creates a new {@link Inspector} object.
	 * 
	 * @param repositoriesDirectory
	 *            The directory where all Git repositories are mirrored to (non-bare repositories).
	 */
	public Inspector(File repositoriesDirectory) {
		this.repositoriesDirectory = repositoriesDirectory;
	}

	/**
	 * This method lists all the current branches of a specific {@link Repository} object.
	 * 
	 * @param repository
	 *            The {@link Repository} to list all the current branches of.
	 * @return A {@link Collections} of {@link BranchModel} objects, each representing one branch in
	 *         the specified Git repository.
	 * @throws IOException
	 *             In case the Git repository could not be accessed.
	 * @throws GitException
	 *             In case the Git repository could not be interacted with.
	 */
	public Collection<BranchModel> listBranches(Repository repository) throws IOException, GitException {
		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);

		try {
			List<Ref> results = git.branchList()
				.setListMode(ListMode.REMOTE)
				.call();

			return Collections2.transform(Collections2.filter(results, new Predicate<Ref>() {

				@Override
				public boolean apply(Ref input) {
					// Ignore the HEAD of the GitServer clone
					return !input.getName().equals("refs/remotes/origin/HEAD");
				}
				
			}), new Function<Ref, BranchModel>() {
				@Override
				public BranchModel apply(Ref input) {
					String name = input.getName();
					ObjectId objectId = input.getObjectId();

					BranchModel branch = new BranchModel();
					branch.setCommit(objectId.getName());
					branch.setName(name);
					return branch;
				}
			});
		}
		catch (GitAPIException e) {
			throw new GitException(e);
		}
	}

	/**
	 * This method lists all the current tags of a specific {@link Repository} object.
	 * 
	 * @param repository
	 *            The {@link Repository} to list all the current tags of.
	 * @return A {@link Collections} of {@link TagModel} objects, each representing one tag in the
	 *         specified Git repository.
	 * @throws IOException
	 *             In case the Git repository could not be accessed.
	 * @throws GitException
	 *             In case the Git repository could not be interacted with.
	 */
	public Collection<TagModel> listTags(Repository repository) throws IOException, GitException {
		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);

		try {
			List<Ref> results = git.tagList()
				.call();

			return Collections2.transform(results, new Function<Ref, TagModel>() {
				@Override
				public TagModel apply(Ref input) {
					TagModel tag = new TagModel();
					ObjectId objectId = input.getPeeledObjectId();
					if (objectId == null) {
						objectId = input.getObjectId();
					}

					tag.setName(input.getName());
					tag.setCommit(objectId.getName());
					return tag;
				}
			});
		}
		catch (GitAPIException e) {
			throw new GitException(e);
		}
	}

	/**
	 * This method lists all commits of a specific {@link Repository} object.
	 * 
	 * @param repository
	 *            The {@link Repository} to list all the current commits of.
	 * @return A {@link Collections} of {@link CommitModel} objects, each representing one commit in
	 *         the specified Git repository. The {@link CommitModel} objects are ordered from newest
	 *         to oldest.
	 * @throws IOException
	 *             In case the Git repository could not be accessed.
	 * @throws GitException
	 *             In case the Git repository could not be interacted with.
	 */
	public List<CommitModel> listCommits(Repository repository) throws IOException, GitException {
		return listCommits(repository, Integer.MAX_VALUE);
	}

	/**
	 * This method lists a limited amount of commits of a specific {@link Repository} object.
	 * 
	 * @param repository
	 *            The {@link Repository} to list a limited amount of commits of.
	 * @param limit
	 *            The maximum amount of {@link CommitModel} objects to return.
	 * @return A {@link Collections} of {@link CommitModel} objects, each representing one commit in
	 *         the specified Git repository. The {@link CommitModel} objects are ordered from newest
	 *         to oldest. At most "limit" number of {@link CommitModel} objects will be returned.
	 * @throws IOException
	 *             In case the Git repository could not be accessed.
	 * @throws GitException
	 *             In case the Git repository could not be interacted with.
	 */
	public List<CommitModel> listCommits(Repository repository, int limit) throws IOException, GitException {
		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);

		try {
			Iterable<RevCommit> revCommits = git.log()
				.all()
				.setMaxCount(limit)
				.call();

			List<CommitModel> commits = Lists.newArrayList();
			for (RevCommit revCommit : revCommits) {
				RevCommit[] parents = revCommit.getParents();
				String[] parentIds = new String[parents.length];
				for (int i = 0; i < parents.length; i++) {
					ObjectId parentId = parents[i].getId();
					parentIds[i] = parentId.getName();
				}

				PersonIdent committerIdent = revCommit.getCommitterIdent();
				ObjectId commitId = revCommit.getId();

				CommitModel commit = new CommitModel();
				commit.setCommit(commitId.getName());
				commit.setParents(parentIds);
				commit.setTime(revCommit.getCommitTime());
				commit.setAuthor(committerIdent.getName(), committerIdent.getEmailAddress());
				commit.setMessage(revCommit.getShortMessage());
				commits.add(commit);
			}
			return commits;
		}
		catch (NoHeadException e) {
			return Lists.newArrayList();
		}
		catch (GitAPIException e) {
			throw new GitException(e);
		}
	}

	/**
	 * Request detailled information and recent commits of a branch
	 * 
	 * @param repository The {@link Repository} to list a limited amount of
	 *           commits of.
	 * @param branchName The name of the branch
	 * @return A {@link DetailedBranchModel} of the requested branch
	 * @throws IOException In case the Git repository could not be accessed.
	 * @throws GitException In case the Git repository could not be interacted
	 *            with.
	 */
	public BranchModel getBranch(Repository repository,
			String branchName) throws GitException, IOException {
		File repositoryDirectory = new File(repositoriesDirectory,
				repository.getName());
		Git git = Git.open(repositoryDirectory);

		try {
			List<Ref> results = git.branchList().setListMode(ListMode.REMOTE)
					.call();

			for (Ref ref : results) {
				if (ref.getName().contains(branchName)) {
					ObjectId objectId = ref.getObjectId();
					BranchModel branch = new BranchModel();
					branch.setCommit(objectId.getName());
					branch.setName(branchName);
					return branch;
				}
			}

			throw new NotFoundException("Branch does not exist!");
		} catch (GitAPIException e) {
			throw new GitException(e);
		}
	}

	/**
	 * This method lists a limited amount of commits of a specific
	 * {@link Repository}
	 * 
	 * @param repository The {@link Repository} to list a limited amount of
	 *           commits of.
	 * @param branchName The branch to start traversal at
	 * @param skip Amount of commits to skip
	 * @param limit The maximum amount of {@link CommitModel} objects to return.
	 * @return A {@link Collections} of {@link CommitModel} objects, each
	 *         representing one commit in the specified Git repository. The
	 *         {@link CommitModel} objects are ordered from newest to oldest. At
	 *         most "limit" number of {@link CommitModel} objects will be
	 *         returned.
	 * @throws GitException In case the Git repository could not be interacted
	 *            with.
	 * @throws IOException In case the Git repository could not be accessed.
	 */
	public List<CommitModel> listCommitsInBranch(Repository repository,
			BranchModel branch) throws GitException,
			IOException {
		File repositoryDirectory = new File(repositoriesDirectory,
				repository.getName());
		Git git = Git.open(repositoryDirectory);

		try {
			String commitId = branch.getCommit();

			Iterable<RevCommit> revCommits = git.log()
					.add(Commit.fromString(commitId))
					.call();

			return Lists.transform(Lists.newArrayList(revCommits),
					new Function<RevCommit, CommitModel>() {

						@Override
						public CommitModel apply(RevCommit revCommit) {
							RevCommit[] parents = revCommit.getParents();
							String[] parentIds = new String[parents.length];
							for (int i = 0; i < parents.length; i++) {
								ObjectId parentId = parents[i].getId();
								parentIds[i] = parentId.getName();
							}

							PersonIdent committerIdent = revCommit
									.getCommitterIdent();
							ObjectId revCommitId = revCommit.getId();

							CommitModel commit = new CommitModel();
							commit.setCommit(revCommitId.getName());
							commit.setParents(parentIds);
							commit.setTime(revCommit.getCommitTime());
							commit.setAuthor(committerIdent.getName(),
									committerIdent.getEmailAddress());
							commit.setMessage(revCommit.getShortMessage());

							return commit;
						}

					});

		} catch (GitAPIException e) {
			throw new GitException(e);
		}
	}

	/**
	 * Retrieve a commit
	 * 
	 * @param repository The {@link Repository} in which the commit can be found
	 * @param commitId The commit id of the requested commit
	 * @return {@link CommitModel} of the requested commit
	 * @throws GitException In case the Git repository could not be interacted
	 *            with.
	 * @throws IOException In case the Git repository could not be accessed.
	 */
	public CommitModel retrieveCommit(Repository repository, String commitId)
			throws GitException, IOException {
		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);

		try {
			Iterable<RevCommit> revCommits = git.log()
				.add(Commit.fromString(commitId))
				.setMaxCount(1)
				.call();

			Iterator<RevCommit> iterator = revCommits.iterator();
			RevCommit revCommit = iterator.next();
			RevCommit[] parents = revCommit.getParents();
			String[] parentIds = new String[parents.length];
			for (int i = 0; i < parents.length; i++) {
				ObjectId parentId = parents[i].getId();
				parentIds[i] = parentId.getName();
			}

			PersonIdent committerIdent = revCommit.getCommitterIdent();
			ObjectId revCommitId = revCommit.getId();

			CommitModel commit = new CommitModel();
			commit.setCommit(revCommitId.getName());
			commit.setParents(parentIds);
			commit.setTime(revCommit.getCommitTime());
			commit.setAuthor(committerIdent.getName(), committerIdent.getEmailAddress());
			commit.setMessage(revCommit.getShortMessage());

			return commit;
		}
		catch (GitAPIException e) {
			throw new GitException(e);
		}
	}

	public Collection<DiffModel> calculateDiff(Repository repository, String commitId) throws IOException, GitException {
		return calculateDiff(repository, null, commitId);
	}


	/**
	 * This method lists a set of diffs of files of a specific {@link Repository} object between two
	 * specific commit IDs.
	 * 
	 * @param repository
	 *            The {@link Repository} to list all the current commits of.
	 * @param leftCommitId
	 *            The first commit ID to base the diff on.
	 * @param rightCommitId
	 *            The second commit ID to base the diff on.
	 * @return A {@link Collections} of {@link DiffModel} objects, each representing the changes in
	 *         one file in the specified Git repository.
	 * @throws IOException
	 *             In case the Git repository could not be accessed.
	 * @throws GitException
	 *             In case the Git repository could not be interacted with.
	 */
	public Collection<DiffModel> calculateDiff(Repository repository, String leftCommitId, String rightCommitId)
			throws IOException, GitException {

		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);

		final org.eclipse.jgit.lib.Repository repo = git.getRepository();
		StoredConfig config = repo.getConfig();
		config.setString("diff", null, "algorithm", "histogram");

		try {
			AbstractTreeIterator oldTreeIter = new EmptyTreeIterator();
			if (!Strings.isNullOrEmpty(leftCommitId)) {
				oldTreeIter = createTreeParser(git, leftCommitId);
			}
			AbstractTreeIterator newTreeIter = new EmptyTreeIterator();
			if (!Strings.isNullOrEmpty(rightCommitId)) {
				newTreeIter = createTreeParser(git, rightCommitId);
			}

			List<DiffEntry> diffs = git.diff()
				.setContextLines(3)
				.setOldTree(oldTreeIter)
				.setNewTree(newTreeIter)
				.call();
			
			RenameDetector rd = new RenameDetector(repo);
			rd.addAll(diffs);
			diffs = rd.compute();

			return Collections2.transform(diffs, new Function<DiffEntry, DiffModel>() {
				public DiffModel apply(DiffEntry input) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					DiffFormatter formatter = new DiffFormatter(out);
					formatter.setRepository(repo);

					String contents = null;
					try {
						formatter.format(input);
						contents = out.toString("UTF-8");
					}
					catch (IOException e) {
						log.error(e.getMessage(), e);
					}

					DiffModel diff = new DiffModel();
					diff.setType(convertChangeType(input.getChangeType()));
					diff.setOldPath(input.getOldPath());
					diff.setNewPath(input.getNewPath());
					diff.setRaw(contents.split("\\r?\\n"));
					return diff;
				}

				private Type convertChangeType(ChangeType changeType) {
					switch (changeType) {
						case ADD:
							return Type.ADD;
						case COPY:
							return Type.COPY;
						case DELETE:
							return Type.DELETE;
						case MODIFY:
							return Type.MODIFY;
						case RENAME:
							return Type.RENAME;
						default:
							throw new IllegalArgumentException("Cannot convert change type: " + changeType);
					}
				}
			});
		}
		catch (GitAPIException e) {
			throw new GitException(e);
		}
	}

	/**
	 * This method lists files and folders in a specified path at a specific commit of the
	 * repository.
	 * 
	 * @param repository
	 *            The {@link Repository} to list all the current commits of.
	 * @param commitId
	 *            The commit ID of the state of the repository.
	 * @param path
	 *            The path to inspect at the specified commit ID.
	 * @return A {@link Collections} of {@link String} representations.
	 * @throws IOException
	 *             In case the Git repository could not be accessed.
	 * @throws GitException
	 *             In case the Git repository could not be interacted with.
	 */
	public Map<String, EntryType> showTree(Repository repository, String commitId, String path) throws IOException,
			GitException {

		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);
		return showTree(git.getRepository(), commitId, path);
	}

	/**
	 * This method returns an {@link InputStream} which will output the contents of the specified
	 * file.
	 * 
	 * @param repository
	 *            The {@link Repository} to list all the current commits of.
	 * @param commitId
	 *            The commit ID of the state of the repository.
	 * @param path
	 *            The path of the file to inspect at the specified commit ID.
	 * @return An {@link InputStream} which outputs the contents of the file. Or NULL if no file is
	 *         present.
	 * @throws IOException
	 *             In case the Git repository could not be accessed.
	 * @throws GitException
	 *             In case the Git repository could not be interacted with.
	 */
	public InputStream showFile(Repository repository, String commitId, String path) throws IOException, GitException {
		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);
		return showFile(git.getRepository(), commitId, path);
	}

	private Map<String, EntryType> showTree(org.eclipse.jgit.lib.Repository repo, String commitId, String path)
			throws GitException, IOException {

		RevWalk walk = new RevWalk(repo);
		ObjectId resolvedObjectId = repo.resolve(commitId);
		RevCommit commit = walk.parseCommit(resolvedObjectId);

		TreeWalk walker = new TreeWalk(repo);
		walker.setFilter(TreeFilter.ALL);
		walker.addTree(commit.getTree());
		walker.setRecursive(true);

		if (!path.endsWith("/") && !Strings.isNullOrEmpty(path)) {
			path += "/";
		}

		Map<String, EntryType> handles = Maps.newLinkedHashMap();
		while (walker.next()) {
			String entryPath = walker.getPathString();
			if (!entryPath.startsWith(path)) {
				continue;
			}

			String entry = entryPath.substring(path.length());
			if (entry.contains("/")) {
				entry = entry.substring(0, entry.indexOf('/') + 1);
			}
			
			handles.put(entry, of(repo, walker, entry));
		}

		if (handles.isEmpty()) {
			return null;
		}

		return handles;
	}

	private InputStream showFile(org.eclipse.jgit.lib.Repository repo, String commitId, String path)
			throws GitException, IOException {

		RevWalk walk = new RevWalk(repo);
		ObjectId resolvedObjectId = repo.resolve(commitId);
		RevCommit commit = walk.parseCommit(resolvedObjectId);

		TreeWalk walker = new TreeWalk(repo);
		walker.setFilter(TreeFilter.ALL);
		walker.addTree(commit.getTree());
		walker.setRecursive(true);

		while (walker.next()) {
			String entryPath = walker.getPathString();
			if (!entryPath.startsWith(path)) {
				continue;
			}

			ObjectId objectId = walker.getObjectId(0);
			ObjectLoader loader = repo.open(objectId);
			return loader.openStream();
		}

		return null;
	}

	private AbstractTreeIterator createTreeParser(Git git, String ref) throws IOException, GitAPIException {
		org.eclipse.jgit.lib.Repository repo = git.getRepository();

		RevWalk walk = new RevWalk(repo);
		RevCommit commit = walk.parseCommit(repo.resolve(ref));
		RevTree commitTree = commit.getTree();
		RevTree tree = walk.parseTree(commitTree.getId());

		CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
		ObjectReader oldReader = repo.newObjectReader();
		try {
			oldTreeParser.reset(oldReader, tree.getId());
		}
		finally {
			oldReader.release();
		}

		return oldTreeParser;
	}

}
