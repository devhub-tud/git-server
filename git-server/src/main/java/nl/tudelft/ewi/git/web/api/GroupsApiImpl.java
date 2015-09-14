package nl.tudelft.ewi.git.web.api;

import com.google.inject.Inject;
import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.git.web.api.di.GroupApiFactory;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.config.Config;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.parser.rules.GroupRule.GroupRuleBuilder;

import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.PathParam;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class GroupsApiImpl implements GroupsApi {

	private final ManagedConfig managedConfig;
	private final Provider<GroupApiFactory> groupApiFactoryProvider;

	@Inject
	public GroupsApiImpl(ManagedConfig managedConfig, Provider<GroupApiFactory> groupApiFactoryProvider) {
		this.managedConfig = managedConfig;
		this.groupApiFactoryProvider = groupApiFactoryProvider;
	}

	@Override
	public List<IdentifiableModel> listAllGroups() {
		return managedConfig.readConfigWithReturn(config ->
				config.getGroupRules().stream()
					.map(Transformers::transformIdentifiable)
					.collect(Collectors.toList())
		);
	}

	@Override
	public GroupModel create(@Valid GroupModel groupModel) {
		return managedConfig.writeConfigWithReturn(config -> {
			GroupRule.GroupRuleBuilder builder = GroupRule.builder()
				.pattern(groupModel.getName());
			GroupRule rule = builder.build();
			addMembers(config, groupModel, builder);
			config.addGroup(rule);
			return Transformers.transformGroupRuleToGroupModel(rule);
		});
	}

	protected static void addMembers(Config config, GroupModel groupModel, GroupRuleBuilder builder) {
		for(IdentifiableModel member : groupModel.getMembers()) {
			String name = member.getName();
			if(name.startsWith("@")) {
				GroupRule ref = config.getGroup(name);
				builder.group(ref);
			}
			else {
				builder.member(new Identifier(name));
			}
		}
	}

	@Override
	public GroupApi getGroup(@NotNull @PathParam("groupName") String groupName) {
		return groupApiFactoryProvider.get().create(groupName);
	}

}
