package nl.tudelft.ewi.git.inspector;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import nl.minicom.gitolite.manager.models.Repository;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class Inspector {
	
	private static final String BRANCH_PREFIX = "refs/heads/";

	private final File repositoriesDirectory;
	
	public Inspector(File repositoriesDirectory) {
		this.repositoriesDirectory = repositoriesDirectory;
	}
	
	public Collection<Branch> listBranches(Repository repository) throws IOException {
		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);
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

	public Collection<Tag> listTags(Repository repository) throws IOException {
		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);
		List<RevTag> results = git.tagList().call();
		return Collections2.transform(results, new Function<RevTag, Tag>() {
			@Override
			public Tag apply(RevTag input) {
				Tag tag = new Tag();
				tag.setName(input.getTagName());
				tag.setCommit(input.getName());
				return tag;
			}
		});
	}
	
	public Collection<Commit> listCommits(Repository repository) throws IOException, NoHeadException, JGitInternalException {
		return listCommits(repository, Integer.MAX_VALUE);
	}
	
	public Collection<Commit> listCommits(Repository repository, int limit) throws IOException, NoHeadException, JGitInternalException {
		File repositoryDirectory = new File(repositoriesDirectory, repository.getName());
		Git git = Git.open(repositoryDirectory);
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
	
}
