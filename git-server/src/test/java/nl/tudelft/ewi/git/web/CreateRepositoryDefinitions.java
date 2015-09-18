package nl.tudelft.ewi.git.web;

import com.google.common.collect.Maps;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;
import nl.tudelft.ewi.git.web.api.RepositoriesApiImpl;
import nl.tudelft.ewi.gitolite.config.Config;
import nl.tudelft.ewi.gitolite.git.GitManager;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.parser.rules.AccessRule;
import nl.tudelft.ewi.gitolite.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.permission.Permission;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.inject.Named;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@ScenarioScoped
@SuppressWarnings("unused")
public class CreateRepositoryDefinitions {

	@Inject @MockedSingleton private GitManager gitManager;
	@Inject @MockedSingleton private Config gitoliteConfig;
	@Inject @Named("repositories.folder") private File repositoriesFolder;
	@Inject private RepositoriesApiImpl repositoriesApi;

	private CreateRepositoryModel createRepositoryModel;
	private DetailedRepositoryModel detailedRepositoryModel;

	@Before
	public void setUp() throws IOException {
		createRepositoryModel = new CreateRepositoryModel();
	}

	@Given("^the Git server is ready$")
	public void theGitServerIsReady() throws Throwable {
		// noop
	}

	@Given("^the template repository \"(.*?)\"$")
	public void theTemplateRepository(String templateRepository) throws Throwable {
		createRepositoryModel.setTemplateRepository(templateRepository);
	}

	@Given("^the following permissions:$")
	public void theFollowingPermissions(final Map<String, Level> permissions) throws Throwable {
		createRepositoryModel.setPermissions(permissions);
	}

	@When("^I create repository \"(.*?)\"$")
	public void iCreateRepository(String name) throws Throwable {
		createRepositoryModel.setName(name);
		prepareBareRepository(name);
		detailedRepositoryModel = repositoriesApi.createRepository(createRepositoryModel);
	}

	private void prepareBareRepository(String name) throws GitAPIException {
		Git.init()
			.setBare(true)
			.setDirectory(new File(repositoriesFolder, name + ".git"))
			.call();
	}

	@Then("^the template repository is cloned$")
	public void theTemplateRepositoryIsCloned() throws Throwable {
		assertNotNull("Master branch is not initialized", detailedRepositoryModel.getBranch("master"));
	}

	@Then("^the template is pushed to the provisioned repository$")
	public void theTemplateIsPushedToTheProvisionedRepository() throws Throwable {
		File folder = new File(repositoriesFolder, createRepositoryModel.getName() + ".git");
		assertTrue("Git folder does not exist at " + folder, folder.exists());
	}

	@Then("^the permissions look like this:$")
	public void thePermissionsLookLikeThis(final Map<String, Level> permissions) throws Throwable {
		Map<Permission, AccessRule> accessRuleMap = permissionsToAccessRules(permissions);

		RepositoryRule repositoryRule = RepositoryRule.builder()
			.identifiable(new Identifier(createRepositoryModel.getName()))
			.rules(accessRuleMap.values())
			.build();

		verify(gitoliteConfig).addRepositoryRule(repositoryRule);
		verify(gitManager).commitChanges();
		verify(gitManager).push();
	}

	protected Map<Permission, AccessRule> permissionsToAccessRules(Map<String, Level> permissions) {
		Map<Permission, AccessRule> accessRuleMap = Maps.newHashMap();
		permissions.forEach((username, level) -> {
			Permission permission = Permission.valueOf(level.getLevel());
			AccessRule accessRule = accessRuleMap.get(permission);
			Identifier identifier = Identifier.valueOf(username);
			if(accessRule == null) {
				accessRule = new AccessRule(permission, identifier);
				accessRuleMap.put(permission, accessRule);
			}
			else {
				accessRule.getMembers().add(identifier);
			}
		});
		return accessRuleMap;
	}

}
