package nl.tudelft.ewi.git.web.api;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import nl.tudelft.ewi.git.Config;
import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;
import nl.tudelft.ewi.git.models.SshKeyModel;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.keystore.Key;
import nl.tudelft.ewi.gitolite.objects.Identifiable;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.parser.rules.AccessRule;
import nl.tudelft.ewi.gitolite.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.parser.rules.InlineUserGroup;
import nl.tudelft.ewi.gitolite.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.repositories.Repository;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class Transformers {

	private final Config config;
	private final ManagedConfig managedConfig;

	@Inject
	public Transformers(Config config, ManagedConfig managedConfig) {
		this.config = config;
		this.managedConfig = managedConfig;
	}

	public static IdentifiableModel transformIdentifiable(Identifiable groupRule) {
		IdentifiableModel identifiableModel = new IdentifiableModel();
		identifiableModel.setName(groupRule.getPattern());
		return identifiableModel;
	}

	public static GroupModel transformGroupRuleToGroupModel(GroupRule groupRule) {
		GroupModel identifiableModel = new GroupModel();
		identifiableModel.setName(groupRule.getPattern());
		identifiableModel.setMembers(
			groupRule.getOwnMembersStream()
				.map(Transformers::transformIdentifiable)
				.collect(Collectors.toList())
		);
		return identifiableModel;
	}

	@SneakyThrows
	public static SshKeyModel transformSshKey(Key key) {
		SshKeyModel sshKeyModel = new SshKeyModel();
		sshKeyModel.setName(key.getName());
		sshKeyModel.setContents(key.getContents());
		return sshKeyModel;
	}

	public RepositoryModel transformRepository(Repository repository) {
		return setBaseAttributes(repository, new RepositoryModel());
	}

	public <T extends RepositoryModel> T setBaseAttributes(Repository repository, T repositoryModel) {
		String name = repository.getURI().toString();
		name = name.substring(0, name.lastIndexOf(".git/"));
		repositoryModel.setName(name);
		repositoryModel.setUrl(config.getGitoliteBaseUrl() + name);

		Identifier identifier = new Identifier(name);
		Map<String, Level>  permissions = managedConfig.readConfigWithReturn(config ->
			transformRepositoryRule(config.getRepositoryRule(identifier)));
		repositoryModel.setPermissions(permissions);

		return repositoryModel;
	}

	// WILL BE REPLACED VERY SOON, JUST A VIEW TO LEGACY ACCESSES
	@Deprecated
	public static Map<String, Level> transformRepositoryRule(Collection<? extends RepositoryRule> repositoryRules) {
		ImmutableMap.Builder<String, Level> builder = ImmutableMap.builder();

		for(RepositoryRule repositoryRule : repositoryRules) {
			for(AccessRule accessRule : repositoryRule.getRules()) {
				// TODO Skip defaults as they are not supported by the git server yet.
				if(!accessRule.getAdjustedRefex().equals(AccessRule.DEFAULT_REFEX)) continue;
				Level permission = Level.getLevel(accessRule.getPermission().valueOf());

				InlineUserGroup inlineUserGroup = accessRule.getMembers();
				Stream.concat(
					inlineUserGroup.getOwnGroupsStream(),
					inlineUserGroup.getOwnMembersStream()
				).map(Identifiable::getPattern).sequential()
				.forEach(member -> builder.put(member, permission));
			}
		}

		return builder.build();
	}

}
