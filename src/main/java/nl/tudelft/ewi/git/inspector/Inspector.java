package nl.tudelft.ewi.git.inspector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import nl.minicom.gitolite.manager.exceptions.GitException;
import nl.minicom.gitolite.manager.models.Repository;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * This class allows its users to inspect the contents of Git repositories. You can use this class to list commits,
 * branches and tags. In addition it can create diffs between commits, list files and folders for a certain commit and
 * inspect the contents of a specified file at a specific commit ID.
 * 
 * @author michael
 */
@Slf4j
public class Inspector {

	private static final String BRANCH_PREFIX = "refs/heads/";

	private final File repositoriesDirectory;

	/**
	 * Creates a new {@link Inspector} object.
	 * 
	 * @param repositoriesDirectory
	 *        The directory where all Git repositories are mirrored to (non-bare repositories).
	 */
	public Inspector(File repositoriesDirectory) {
		this.repositoriesDirectory = repositoriesDirectory;
	}

	/**
	 * This method lists all the current branches of a specific {@link Repository} object.
	 * 
	 * @param repository
	 *        The {@link Repository} to list all the current branches of.
	 * @return A {@link Collections} of {@link Branch} objects, each representing one branch in the specified Git
	 *         repository.
	 * @throws IOException
	 *         In case the Git repository could not be accessed.
	 * @throws GitException
	 *         In case the Git repository could not be interacted with.
	 */
	public Collection<Branch> listBranches(Repository repository) throws IOException, GitException {
		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);

		try {
			List<Ref> results = git.branchList().call();
			return Collections2.transform(results, new Function<Ref, Branch>() {
				@Override
				public Branch apply(Ref input) {
					String name = input.getName();

					Branch branch = new Branch();
					branch.setCommit(input.getObjectId().getName());
					if (name.startsWith(BRANCH_PREFIX)) {
						branch.setName(name.substring(BRANCH_PREFIX.length()));
					}
					else {
						branch.setName(name);
					}

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
	 *        The {@link Repository} to list all the current tags of.
	 * @return A {@link Collections} of {@link Tag} objects, each representing one tag in the specified Git repository.
	 * @throws IOException
	 *         In case the Git repository could not be accessed.
	 * @throws GitException
	 *         In case the Git repository could not be interacted with.
	 */
	public Collection<Tag> listTags(Repository repository) throws IOException, GitException {
		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);

		try {
			List<Ref> results = git.tagList().call();
			return Collections2.transform(results, new Function<Ref, Tag>() {
				@Override
				public Tag apply(Ref input) {
					Tag tag = new Tag();
					tag.setName(input.getName());
					tag.setCommit(input.getObjectId().getName());
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
	 *        The {@link Repository} to list all the current commits of.
	 * @return A {@link Collections} of {@link Commit} objects, each representing one commit in the specified Git
	 *         repository. The {@link Commit} objects are ordered from newest to oldest.
	 * @throws IOException
	 *         In case the Git repository could not be accessed.
	 * @throws GitException
	 *         In case the Git repository could not be interacted with.
	 */
	public Collection<Commit> listCommits(Repository repository) throws IOException, GitException {
		return listCommits(repository, Integer.MAX_VALUE);
	}

	/**
	 * This method lists a limited amount of commits of a specific {@link Repository} object.
	 * 
	 * @param repository
	 *        The {@link Repository} to list a limited amount of commits of.
	 * @param limit
	 *        The maximum amount of {@link Commit} objects to return.
	 * @return A {@link Collections} of {@link Commit} objects, each representing one commit in the specified Git
	 *         repository. The {@link Commit} objects are ordered from newest to oldest. At most "limit" number of
	 *         {@link Commit} objects will be returned.
	 * @throws IOException
	 *         In case the Git repository could not be accessed.
	 * @throws GitException
	 *         In case the Git repository could not be interacted with.
	 */
	public Collection<Commit> listCommits(Repository repository, int limit) throws IOException, GitException {
		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);

		try {
			Iterable<RevCommit> results = git.log().setMaxCount(limit).call();

			List<RevCommit> commits = Lists.newArrayList(results);
			return Collections2.transform(commits, new Function<RevCommit, Commit>() {
				@Override
				public Commit apply(RevCommit input) {
					RevCommit[] parents = input.getParents();
					String[] parentIds = new String[parents.length];
					for (int i = 0; i < parents.length; i++) {
						parentIds[i] = parents[i].getId().getName();
					}

					PersonIdent committerIdent = input.getCommitterIdent();

					Commit commit = new Commit();
					commit.setCommit(input.getId().getName());
					commit.setParents(parentIds);
					commit.setTime(input.getCommitTime());
					commit.setAuthor(committerIdent.getName(), committerIdent.getEmailAddress());
					commit.setMessage(input.getShortMessage());
					return commit;
				}
			});
		}
		catch (GitAPIException e) {
			throw new GitException(e);
		}
	}

	/**
	 * This method lists a set of diffs of files of a specific {@link Repository} object between two specific commit
	 * IDs.
	 * 
	 * @param repository
	 *        The {@link Repository} to list all the current commits of.
	 * @param leftCommitId
	 *        The first commit ID to base the diff on.
	 * @param rightCommitId
	 *        The second commit ID to base the diff on.
	 * @return A {@link Collections} of {@link Diff} objects, each representing the changes in one file in the specified
	 *         Git repository.
	 * @throws IOException
	 *         In case the Git repository could not be accessed.
	 * @throws GitException
	 *         In case the Git repository could not be interacted with.
	 */
	public Collection<Diff> calculateDiff(Repository repository, String leftCommitId, String rightCommitId)
			throws IOException, GitException {

		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);

		final org.eclipse.jgit.lib.Repository repo = git.getRepository();
		repo.getConfig().setString("diff", null, "algorithm", "histogram");

		try {
			AbstractTreeIterator oldTreeIter = createTreeParser(git, leftCommitId);
			AbstractTreeIterator newTreeIter = createTreeParser(git, rightCommitId);

			List<DiffEntry> diffs = git.diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();

			return Collections2.transform(diffs, new Function<DiffEntry, Diff>() {
				public Diff apply(DiffEntry input) {
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

					Diff diff = new Diff();
					diff.setChangeType(input.getChangeType());
					diff.setOldPath(input.getOldPath());
					diff.setNewPath(input.getNewPath());
					diff.setRaw(contents.split("\\r?\\n"));
					return diff;
				}
			});
		}
		catch (GitAPIException e) {
			throw new GitException(e);
		}
	}

	/**
	 * This method lists files and folders in a specified path at a specific commit of the repository.
	 * 
	 * @param repository
	 *        The {@link Repository} to list all the current commits of.
	 * @param commitId
	 *        The commit ID of the state of the repository.
	 * @param path
	 *        The path to inspect at the specified commit ID.
	 * @return A {@link Collections} of {@link String} representations.
	 * @throws IOException
	 *         In case the Git repository could not be accessed.
	 * @throws GitException
	 *         In case the Git repository could not be interacted with.
	 */
	public Collection<String> showTree(Repository repository, String commitId, String path) throws IOException,
			GitException {
		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);
		return showTree(git.getRepository(), commitId, path);
	}

	/**
	 * This method returns an {@link InputStream} which will output the contents of the specified file.
	 * 
	 * @param repository
	 *        The {@link Repository} to list all the current commits of.
	 * @param commitId
	 *        The commit ID of the state of the repository.
	 * @param path
	 *        The path of the file to inspect at the specified commit ID.
	 * @return An {@link InputStream} which outputs the contents of the file. Or NULL if no file is present.
	 * @throws IOException
	 *         In case the Git repository could not be accessed.
	 * @throws GitException
	 *         In case the Git repository could not be interacted with.
	 */
	public InputStream showFile(Repository repository, String commitId, String path) throws IOException, GitException {
		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);
		return showFile(git.getRepository(), commitId, path);
	}

	private Collection<String> showTree(org.eclipse.jgit.lib.Repository repo, String commitId, String path)
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

		List<String> handles = Lists.newArrayList();
		while (walker.next()) {
			String entryPath = walker.getPathString();
			if (!entryPath.startsWith(path)) {
				continue;
			}

			String entry = entryPath.substring(path.length());
			if (entry.contains("/")) {
				entry = entry.substring(0, entry.indexOf('/') + 1);
			}

			handles.add(entry);
		}

		if (handles.isEmpty()) {
			return null;
		}

		Collections.sort(handles, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (o1.endsWith("/") && o2.endsWith("/")) {
					return o1.compareTo(o2);
				}
				else if (!o1.endsWith("/") && !o2.endsWith("/")) {
					return o1.compareTo(o2);
				}
				else if (o1.endsWith("/")) {
					return -1;
				}
				return 1;
			}
		});

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
		RevTree tree = walk.parseTree(commit.getTree().getId());

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
