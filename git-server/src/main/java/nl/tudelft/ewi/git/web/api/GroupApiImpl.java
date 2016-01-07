package nl.tudelft.ewi.git.web.api;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.parser.rules.GroupRule;

import javax.validation.Valid;
import javax.ws.rs.NotFoundException;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class GroupApiImpl implements GroupApi {

	private final ManagedConfig managedConfig;
	private final String groupName;

	@Inject
	public GroupApiImpl(ManagedConfig managedConfig, @Assisted String groupName) {
		this.managedConfig = managedConfig;
		this.groupName = groupName;
	}

	protected GroupRule getGroupRule() {
		try {
			return managedConfig.readConfigWithReturn(config ->
				config.getGroup(groupName));
		}
		catch (NoSuchElementException e) {
			throw new NotFoundException("Group rule for " + groupName + " could not be found: " + e.getMessage(), e);
		}
	}

	@Override
	public GroupModel getGroup() {
		return managedConfig.readConfigWithReturn(config ->
			Transformers.transformGroupRuleToGroupModel(getGroupRule()));
	}

	@Override
	public void deleteGroup() {
		managedConfig.writeConfig(config ->
			config.deleteGroup(config.getGroup(groupName)));
	}

	@Override
	public Collection<IdentifiableModel> listMembers() {
		return getGroup().getMembers();
	}

	@Override
	public Collection<IdentifiableModel> addNewMember(@Valid IdentifiableModel model) {
		return managedConfig.writeConfigWithReturn(config -> {
			GroupRule groupRule = getGroupRule();
			String name = model.getName();
			if (name.startsWith("@")) {
				GroupRule ref = config.getGroup(name);
				groupRule.add(ref);
			} else {
				groupRule.add(Identifier.valueOf(name));
			}

			config.addGroup(groupRule);
			return Transformers.transformGroupRuleToGroupModel(groupRule);
		}).getMembers();
	}

	@Override
	public void removeMember(@Valid IdentifiableModel model) {
		Identifier identifier = Identifier.valueOf(model.getName());
		managedConfig.writeConfig(config ->
			getGroupRule().remove(identifier));
	}

}
