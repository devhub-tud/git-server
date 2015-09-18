package nl.tudelft.ewi.git.web;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.git.web.api.GroupApi;
import nl.tudelft.ewi.git.web.api.GroupsApi;
import nl.tudelft.ewi.git.web.api.di.GroupApiFactory;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.config.Config;
import nl.tudelft.ewi.gitolite.git.GitManager;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.parser.rules.GroupRule;
import org.hamcrest.Matchers;

import javax.inject.Inject;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@ScenarioScoped
@SuppressWarnings("unused")
public class GroupApiDefinitions {

	@Inject @MockedSingleton private GitManager gitManager;
	@Inject @MockedSingleton private Config gitoliteConfig;
	@Inject private ManagedConfig managedConfig;
	@Inject private GroupsApi groupsApi;

	private GroupModel groupModel;

	@Before
	public void cleanUpOldConfig() {
		gitoliteConfig.clear();
	}

	@Given("^the group name \"([^\"]*)\"$")
	public void theGroupName(String name) throws Throwable {
		groupModel = new GroupModel();
		groupModel.setName(name);
	}

	@Given("^the following group members:$")
	public void theFollowingGroupMembers(List<IdentifiableModel> members) throws Throwable {
		groupModel.setMembers(members);
	}

	@When("^the group is created$")
	public void theGroupIsCreated() throws Throwable {
		groupsApi.create(groupModel);
	}

	@Then("^the group should be added to the Gitolite configuration$")
	public void theGroupShouldBeAddedToTheGitoliteConfiguration() throws Throwable {
		verify(gitoliteConfig).addGroup(getGroupRule());
	}

	protected GroupRule getGroupRule() {
		List<Identifier> members = groupModel.getMembers().stream()
			.map(IdentifiableModel::getName).map(Identifier::valueOf)
			.collect(Collectors.toList());
		return new GroupRule(groupModel.getName(), null, members, Collections.emptyList());
	}

	@Then("^the configuration should be pushed to the remote$")
	public void theConfigurationShouldBePushedToTheRemote() throws Throwable {
		verify(gitManager).commitChanges();
		verify(gitManager).push();
	}

	@Given("^the group exists in the configuration$")
	public void theGroupExistsInTheConfiguration() throws Throwable {
		managedConfig.writeConfig(config -> config.addGroup(getGroupRule()));
		reset(gitManager, gitoliteConfig);
	}

	@When("^\"([^\"]*)\" is added to the group$")
	public void isAddedToTheGroup(IdentifiableModel user) throws Throwable {
		getGroupApi().addNewMember(user);
		updateGroupModel();
	}

	protected void updateGroupModel() {
		groupModel = getGroupApi().getGroup();
	}

	@Then("^the group should contain:$")
	public void theGroupShouldContain(List<IdentifiableModel> users) throws Throwable {
		assertThat(groupModel.getMembers(), Matchers.contains(users.toArray()));
	}

	@When("^\"([^\"]*)\" is removed from the group$")
	public void isRemovedFromTheGroup(IdentifiableModel user) throws Throwable {
		getGroupApi().removeMember(user);
		updateGroupModel();
	}

	protected GroupApi getGroupApi() {
		return groupsApi.getGroup(groupModel.getName());
	}

	@When("^the group is deleted$")
	public void theGroupIsDeleted() throws Throwable {
		getGroupApi().deleteGroup();

	}

	@Then("^the configuration should be empty$")
	public void theConfigurationShouldBeEmpty() throws Throwable {
		assertThat(gitoliteConfig.getGroupRules(), Matchers.empty());
	}

}
