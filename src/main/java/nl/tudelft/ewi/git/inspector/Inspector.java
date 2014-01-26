package nl.tudelft.ewi.git.inspector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import nl.minicom.gitolite.manager.exceptions.GitException;
import nl.minicom.gitolite.manager.models.Repository;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

@Slf4j
public class Inspector {
	
	private static final String BRANCH_PREFIX = "refs/heads/";

	private final File repositoriesDirectory;
	
	public Inspector(File repositoriesDirectory) {
		this.repositoriesDirectory = repositoriesDirectory;
	}
	
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
	
	public Collection<Commit> listCommits(Repository repository) throws IOException, GitException {
		return listCommits(repository, Integer.MAX_VALUE);
	}
	
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
	
	public Collection<Diff> calculateDiff(Repository repository, String leftCommitId, String rightCommitId) throws IOException, GitException {
		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);

		final org.eclipse.jgit.lib.Repository repo = git.getRepository();
		repo.getConfig().setString("diff", null, "algorithm", "histogram");
		
		try {
			AbstractTreeIterator oldTreeIter = createTreeParser(git, leftCommitId);
			AbstractTreeIterator newTreeIter = createTreeParser(git, rightCommitId);
			
			List<DiffEntry> diffs = git.diff()
			        .setNewTree(newTreeIter)
			        .setOldTree(oldTreeIter)
			        .call();
			
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
					diff.setRaw(contents);
					return diff;
				}
			});
		}
		catch (GitAPIException e) {
			throw new GitException(e);
		}
	}
	
	private static AbstractTreeIterator createTreeParser(Git git, String ref) throws IOException, GitAPIException {
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
