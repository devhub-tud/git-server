package nl.tudelft.ewi.git.models;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.minicom.gitolite.manager.exceptions.GitException;
import nl.minicom.gitolite.manager.models.ConfigManager;
import nl.minicom.gitolite.manager.models.Group;
import nl.minicom.gitolite.manager.models.Identifiable;
import nl.minicom.gitolite.manager.models.Permission;
import nl.minicom.gitolite.manager.models.Repository;
import nl.minicom.gitolite.manager.models.User;
import nl.tudelft.ewi.git.Config;
import nl.tudelft.ewi.git.inspector.DiffContextFormatter;
import nl.tudelft.ewi.git.inspector.Inspector;
import nl.tudelft.ewi.git.models.BlameModel.BlameBlock;
import nl.tudelft.ewi.git.models.DiffModel.Type;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This class provides several transformation {@link Function}s which allow you to transform the data objects from the
 * Gitolite {@link ConfigManager} to a certain model object which can be returned through the REST API.
 * 
 * @author michael
 */
@Slf4j
public class Transformers {

	private static final String REF_MASTER = "master";
	
	/**
	 * @return A {@link Function} which can transform {@link Group} objects into {@link GroupModel} objects.
	 */
	public static Function<Group, GroupModel> groups() {
		return new Function<Group, GroupModel>() {
			@Override
			public GroupModel apply(Group input) {
				List<IdentifiableModel> members = Lists.newArrayList();

				ImmutableSet<Group> memberGroups = input.getGroups();
				for (Group group : memberGroups) {
					members.add(groups().apply(group));
				}

				ImmutableSet<User> memberUsers = input.getUsers();
				for (User user : memberUsers) {
					members.add(identifiables().apply(user));
				}

				GroupModel model = new GroupModel();
				model.setName(input.getName());
				model.setMembers(members);
				model.setPath("/api/groups/" + encode(input.getName()));
				return model;
			}
		};
	}

	/**
	 * @return A {@link Function} which can transform {@link Identifiable} objects into {@link IdentifiableModel}
	 *         objects.
	 */
	public static Function<Identifiable, IdentifiableModel> identifiables() {
		return new Function<Identifiable, IdentifiableModel>() {
			@Override
			public IdentifiableModel apply(Identifiable input) {
				IdentifiableModel model = new IdentifiableModel();
				model.setName(input.getName());

				if (input instanceof Group) {
					model.setPath("/api/groups/" + encode(input.getName()));
				}
				else {
					model.setPath("/api/users/" + encode(input.getName()));
				}

				return model;
			}
		};
	}

	/**
	 * @return A {@link Function} which can transform {@link Identifiable} objects into {@link GroupModel} or
	 *         {@link UserModel} objects.
	 */
	public static Function<Identifiable, IdentifiableModel> detailedIdentifiables() {
		return new Function<Identifiable, IdentifiableModel>() {
			@Override
			public IdentifiableModel apply(Identifiable input) {
				if (input instanceof Group) {
					return groups().apply((Group) input);
				}
				else if (input instanceof User) {
					return identifiables().apply((User) input);
				}
				else {
					return null;
				}
			}
		};
	}

	/**
	 * @return A {@link Function} which can transform {@link Repository} objects into {@link RepositoryModel} objects.
	 */
	public static Function<Repository, RepositoryModel> repositories(final Config config) {
		return new Function<Repository, RepositoryModel>() {
			@Override
			public RepositoryModel apply(Repository input) {
				Map<String, Level> permissions = Maps.newHashMap();
				for (Entry<Permission, Identifiable> entry : input.getPermissions().entries()) {
					permissions.put(entry.getValue().getName(), Level.getLevel(entry.getKey().getLevel()));
				}

				RepositoryModel model = new RepositoryModel();
				model.setName(input.getName());
				model.setPermissions(permissions);
				model.setPath("/api/repositories/" + encode(input.getName()));
				model.setUrl(config.getGitoliteBaseUrl() + input.getName() + ".git");
				return model;
			}
		};
	}

	/**
	 * @return A {@link Function} which can transform {@link Repository} objects into {@link DetailedRepositoryModel} objects.
	 */
	public static Function<Repository, DetailedRepositoryModel> detailedRepositories(final Config config, final Inspector inspector) {
		return new Function<Repository, DetailedRepositoryModel>() {
			@Override
			public DetailedRepositoryModel apply(Repository input) {
				Map<String, Level> permissions = Maps.newHashMap();
				for (Entry<Permission, Identifiable> entry : input.getPermissions().entries()) {
					permissions.put(entry.getValue().getName(), Level.getLevel(entry.getKey().getLevel()));
				}

				DetailedRepositoryModel model = new DetailedRepositoryModel();
				model.setName(input.getName());
				model.setPermissions(permissions);
				model.setPath("/api/repositories/" + encode(input.getName()));
				model.setUrl(config.getGitoliteBaseUrl() + input.getName() + ".git");

				try {
					model.setBranches(inspector.listBranches(input));
				}
				catch (IOException | GitException e) {
					log.warn(e.getMessage(), e);
					model.setBranches(Collections.<BranchModel> emptyList());
				}

				try {
					model.setTags(inspector.listTags(input));
				}
				catch (IOException | GitException e) {
					log.warn(e.getMessage(), e);
					model.setTags(Collections.<TagModel> emptyList());
				}

				return model;
			}
		};
	}

	/**
	 * @return A {@link Function} which can transform {@link Map} objects into {@link SshKeyModel} objects.
	 */
	public static Function<Entry<String, String>, SshKeyModel> sshKeys(final User owner) {
		return new Function<Entry<String, String>, SshKeyModel>() {
			@Override
			public SshKeyModel apply(Entry<String, String> input) {
				String keyId = input.getKey();
				if (keyId.isEmpty()) {
					keyId = "default";
				}
				
				SshKeyModel model = new SshKeyModel();
				model.setContents(input.getValue());
				model.setName(keyId);
				model.setPath("/api/users/" + encode(owner.getName()) + "/keys/" + encode(keyId));
				return model;
			}
		};
	}

	/**
	 * @return A {@link Function} which can transform {@link User} objects into {@link UserModel} objects.
	 */
	public static Function<User, UserModel> users() {
		return new Function<User, UserModel>() {
			@Override
			public UserModel apply(User input) {
				UserModel model = new UserModel();
				model.setName(input.getName());
				model.setKeys(Collections2.transform(input.getKeys().entrySet(), sshKeys(input)));
				model.setPath("/api/users/" + encode(input.getName()));
				return model;
			}
		};
	}

	/**
	 * @param repository {@link RepositoryModel} in which the branch resists
	 * @return A {@link Function} which can transform {@link Ref} objects into {@link BranchModel} objects.
	 */
	public static Function<Ref, BranchModel> branchModel(final Repository repository, final org.eclipse.jgit.lib.Repository repo) {
		return new Function<Ref, BranchModel>() {
			@Override
			public BranchModel apply(final Ref input) {

				BranchModel branch = new BranchModel();
				String name = input.getName();
				branch.setName(name);
				branch.setPath("/api/repositories/"
						+ encode(repository.getName()) + "/branch/"
						+ encode(name));

				try {
					int ahead = 0, behind = 0;
					RevWalk walk = new RevWalk(repo);
					RevCommit localCommit = walk.parseCommit(input.getObjectId());
					branch.setCommit(commitModel(repository).apply(localCommit));

					Ref master = repo.getRef(REF_MASTER);
					if(!input.equals(master)){
						RevCommit trackingCommit = walk.parseCommit(master.getObjectId());

						walk.setRevFilter(RevFilter.MERGE_BASE);
						walk.markStart(localCommit);
						walk.markStart(trackingCommit);
						RevCommit mergeBase = walk.next();

						walk.reset();
						walk.setRevFilter(RevFilter.ALL);
						ahead = RevWalkUtils.count(walk, localCommit, mergeBase);
						behind = RevWalkUtils.count(walk, trackingCommit, mergeBase);
					}

					branch.setAhead(ahead);
					branch.setBehind(behind);
				}
				catch (IOException e) {
					log.warn("Failed to calculate branch tracking status", e);
				}

				return branch;
			}
		};
	}

	/**
	 * @param repository {@link RepositoryModel} in which the commit resists
	 * @return A {@link Function} which can transform {@link RevCommit} objects into {@link CommitModel} objects.
	 */
	public static Function<RevCommit, CommitModel> commitModel(final Repository repository) {
		return new Function<RevCommit, CommitModel>() {

			@Override
			public CommitModel apply(RevCommit revCommit) {
				PersonIdent committerIdent = revCommit.getCommitterIdent();
				ObjectId revCommitId = revCommit.getId();

				RevCommit[] parents = revCommit.getParents();
				String[] parentIds = new String[parents.length];
				for (int i = 0; i < parents.length; i++) {
					ObjectId parentId = parents[i].getId();
					parentIds[i] = parentId.getName();
				}

				CommitModel commit = new CommitModel();
				commit.setCommit(revCommitId.getName());
				commit.setParents(parentIds);
				commit.setTime(revCommit.getCommitTime());
				commit.setAuthor(committerIdent.getName(), committerIdent.getEmailAddress());
				commit.setMessage(revCommit.getShortMessage());
				commit.setPath("/api/repositories/"
						+ encode(repository.getName()) + "/commits/"
						+ encode(revCommitId.getName()));
				return commit;
			}
		};
	}

	/**
	 * @param repository {@link RepositoryModel} in which the commit resists
	 * @return A {@link Function} which can transform {@link RevCommit} objects into {@link DetailedCommitModel} objects.
	 */
	public static Function<RevCommit, DetailedCommitModel> detailedCommitModel(final Repository repository) {
		return new Function<RevCommit, DetailedCommitModel>() {

			@Override
			public DetailedCommitModel apply(RevCommit revCommit) {
				PersonIdent committerIdent = revCommit.getCommitterIdent();
				ObjectId revCommitId = revCommit.getId();

				RevCommit[] parents = revCommit.getParents();
				String[] parentIds = new String[parents.length];
				for (int i = 0; i < parents.length; i++) {
					ObjectId parentId = parents[i].getId();
					parentIds[i] = parentId.getName();
				}

				DetailedCommitModel commit = new DetailedCommitModel();
				commit.setCommit(revCommitId.getName());
				commit.setParents(parentIds);
				commit.setTime(revCommit.getCommitTime());
				commit.setAuthor(committerIdent.getName(), committerIdent.getEmailAddress());
				commit.setMessage(revCommit.getShortMessage());
				commit.setFullMessage(revCommit.getFullMessage());
				commit.setPath("/api/repositories/"
						+ encode(repository.getName()) + "/commits/"
						+ encode(revCommitId.getName()));
				return commit;
			}
		};
	}
	
	/**
	 * @param repo {@link org.eclipse.jgit.lib.Repository} for this calculation
	 * @return a {@link Function} that transforms a {@link DiffEntry} to a {@link DiffModel}
	 */
	public static Function<DiffEntry, DiffModel> diffEntry(final org.eclipse.jgit.lib.Repository repo) {
		return new Function<DiffEntry, DiffModel>() {
			public DiffModel apply(DiffEntry input) {
				DiffModel diff = new DiffModel();
				diff.setType(convertChangeType(input.getChangeType()));
				diff.setOldPath(input.getOldPath());
				diff.setNewPath(input.getNewPath());
				
				DiffContextFormatter formatter = new DiffContextFormatter(diff, repo);
				
				try {
					formatter.format(input);
				}
				catch (IOException e) {
					log.warn(e.getMessage(), e);
				}
				
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
		};
	}
	
	/**
	 * {@link Function} to transform a {@link BlameResult} into a {@link BlameModel}
	 * @param repo
	 * @param commitId
	 * @param path
	 * @return {@link BlameModel}
	 */
	public static Function<BlameResult, BlameModel> blameModel(
			final org.eclipse.jgit.lib.Repository repo, final String commitId,
			final String path) {
		return new Function<BlameResult, BlameModel>() {
			@Override
			public BlameModel apply(BlameResult input) {
				final BlameModel model = new BlameModel();
				model.setCommitId(commitId);
				model.setPath(path);
				final List<BlameBlock> blames = Lists.<BlameBlock> newArrayList();
				
				BlameBlock block = null;
				
				for(int i = 0, length = input.getResultContents().size(); i < length; i++) {
					String commitId = input.getSourceCommit(i).getName();
					if(block == null || (!block.getFromCommitId().equals(commitId))) {
						// First block or from another commit
						int destinationFrom = block == null ? 0 : block.getDestinationTo();
                        int sourceFrom = block == null ? 0 : block.getSourceTo();
                        // Replace previous block with a new one
						block = new BlameBlock();
						block.setFromCommitId(commitId);
						block.setDestinationFrom(destinationFrom);
						block.setSourceFrom(sourceFrom);
                        block.setFromFilePath(input.getSourcePath(i));
						block.setLength(1);
						blames.add(block);
					}
					else {
						// Another line in current block
						block.incrementLength();
					}
				}
				
				model.setBlames(blames);
				return model;
			}
		};
	}

	@SneakyThrows
	private static String encode(String value) {
		return URLEncoder.encode(value, "UTF-8");
	}

}
