package nl.tudelft.ewi.git.web.models;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;
import nl.minicom.gitolite.manager.models.Group;
import nl.minicom.gitolite.manager.models.Identifiable;
import nl.minicom.gitolite.manager.models.Permission;
import nl.minicom.gitolite.manager.models.Repository;
import nl.minicom.gitolite.manager.models.User;
import nl.tudelft.ewi.git.inspector.Branch;
import nl.tudelft.ewi.git.inspector.Commit;
import nl.tudelft.ewi.git.inspector.Inspector;
import nl.tudelft.ewi.git.inspector.Tag;

import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoHeadException;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Slf4j
public class Transformers {
	
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
				model.setPath("/api/groups/" + input.getName());
				return model;
			}
		};
	}
	
	public static Function<Identifiable, IdentifiableModel> identifiables() {
		return new Function<Identifiable, IdentifiableModel>() {
			@Override
			public IdentifiableModel apply(Identifiable input) {
				IdentifiableModel model = new IdentifiableModel();
				model.setName(input.getName());
				
				if (input instanceof Group) {
					model.setPath("/api/groups/" + input.getName());
				}
				else {
					model.setPath("/api/users/" + input.getName());
				}
				
				return model;
			}
		};
	}
	
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
	
	public static Function<Repository, RepositoryModel> repositories() {
		return new Function<Repository, RepositoryModel>() {
			@Override
			public RepositoryModel apply(Repository input) {
				Map<String, String> permissions = Maps.newHashMap();
				for (Entry<Permission, Identifiable> entry : input.getPermissions().entries()) {
					permissions.put(entry.getValue().getName(), entry.getKey().getLevel());
				}
				
				RepositoryModel model = new RepositoryModel();
				model.setName(input.getName());
				model.setPermissions(permissions);
				model.setPath("/api/repositories/" + input.getName());
				model.setUrl("ssh://git@localhost:2222/" + input.getName() + ".git");
				return model;
			}
		};
	}

	public static Function<Repository, DetailedRepositoryModel> detailedRepositories(final Inspector inspector) {
		return new Function<Repository, DetailedRepositoryModel>() {
			@Override
			public DetailedRepositoryModel apply(Repository input) {
				Map<String, String> permissions = Maps.newHashMap();
				for (Entry<Permission, Identifiable> entry : input.getPermissions().entries()) {
					permissions.put(entry.getValue().getName(), entry.getKey().getLevel());
				}
				
				DetailedRepositoryModel model = new DetailedRepositoryModel();
				model.setName(input.getName());
				model.setPermissions(permissions);
				model.setPath("/api/repositories/" + input.getName());
				model.setUrl("ssh://git@localhost:2222/" + input.getName() + ".git");

				try {
					model.setBranches(inspector.listBranches(input));
				}
				catch (IOException e) {
					log.error(e.getMessage(), e);
					model.setBranches(Collections.<Branch>emptyList());
				}
				
				try {
					model.setTags(inspector.listTags(input));
				}
				catch (IOException e) {
					log.error(e.getMessage(), e);
					model.setTags(Collections.<Tag>emptyList());
				}
				
				try {
					model.setCommits(inspector.listCommits(input));
				}
				catch (IOException | NoHeadException | JGitInternalException e) {
					log.error(e.getMessage(), e);
					model.setCommits(Collections.<Commit>emptyList());
				}
				
				return model;
			}
		};
	}
	
	public static Function<Entry<String, String>, SshKeyModel> sshKeys(final User owner) {
		return new Function<Entry<String, String>, SshKeyModel>() {
			@Override
			public SshKeyModel apply(Entry<String, String> input) {
				String fullKeyName = owner.getName() + ".pub";
				if (!Strings.isNullOrEmpty(input.getKey())) {
					fullKeyName = input.getKey() + "@" + fullKeyName;
				}
				
				SshKeyModel model = new SshKeyModel();
				model.setContents(input.getValue());
				model.setName(fullKeyName);
				model.setPath("/api/users/" + owner.getName() + "/keys/" + fullKeyName);
				return model;
			}
		};
	}

	public static Function<User, UserModel> users() {
		return new Function<User, UserModel>() {
			@Override
			public UserModel apply(User input) {
				UserModel model = new UserModel();
				model.setName(input.getName());
				model.setKeys(Collections2.transform(input.getKeys().entrySet(), sshKeys(input)));
				model.setPath("/api/users/" + input.getName());
				return model;
			}
		};
	}
	
}
