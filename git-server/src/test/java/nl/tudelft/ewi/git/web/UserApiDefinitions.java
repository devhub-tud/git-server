package nl.tudelft.ewi.git.web;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.models.SshKeyModel;
import nl.tudelft.ewi.git.models.UserModel;
import nl.tudelft.ewi.git.web.api.KeysApi;
import nl.tudelft.ewi.git.web.api.UserApi;
import nl.tudelft.ewi.git.web.api.UsersApi;
import nl.tudelft.ewi.gitolite.git.GitManager;
import nl.tudelft.ewi.gitolite.keystore.KeyHolder;
import nl.tudelft.ewi.gitolite.keystore.KeyStore;
import nl.tudelft.ewi.gitolite.keystore.PersistedKey;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;

import javax.inject.Inject;

import java.util.Collection;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@ScenarioScoped
@SuppressWarnings("unused")
public class UserApiDefinitions {

	@Inject @MockedSingleton private GitManager gitManager;
	@Inject @MockedSingleton private KeyStore keyStore;
	@Inject private UsersApi usersApi;

	private UserModel userModel;
	private SshKeyModel sshKeyModel;

	@Before
	public void removeOldKeys() {
		keyStore.getUsers().stream()
			.map(keyStore::getKeys).flatMap(Collection::stream)
			.forEach(unsafe(PersistedKey::delete));
	}

	@Before
	public void resetMockCounters() {
		reset(gitManager, keyStore);
	}

	public interface ThrowingConsumer<T> {
		void accept(T elem) throws Exception;
	}

	public static <T> Consumer<T> unsafe(ThrowingConsumer<T> consumer) {
		return (a) -> {
			try { consumer.accept(a); }
			catch (RuntimeException e) { throw e; }
			catch (Exception e) { throw new RuntimeException(e); }
		};
	}

	@Given("^a user with username \"([^\"]*)\"$")
	public void aUserWithUsername(String name) throws Throwable {
		userModel = new UserModel();
		userModel.setName(name);
	}

	UserApi getUserApi() {
		return usersApi.getUser(userModel.getName());
	}

	KeysApi getKeysApi() {
		return getUserApi().keys();
	}

	@When("^the users adds an SSH key with name \"([^\"]*)\" and contents:$")
	public void theUsersAddsAnSSHKeyWithNameAndContents(String name, String contents) throws Throwable {
		sshKeyModel = new SshKeyModel();
		sshKeyModel.setName(name);
		sshKeyModel.setContents(contents);
		getKeysApi().addNewKey(sshKeyModel);
	}

	@Then("^the key is added to the keystore$")
	public void theKeyIsAddedToTheKeystore() throws Throwable {
		verify(gitManager).commitChanges();
		verify(gitManager).push();
		verify(keyStore).put(new KeyHolder(userModel.getName(), sshKeyModel.getName(), sshKeyModel.getContents()));
	}

	@Given("^the user has an SSH key in the key store$")
	public void theUserHasAnSSHKeyInTheKeyStore() throws Throwable {
		String contents = IOUtils.toString(UserApiDefinitions.class.getResource("/test_rsa.pub"));
		KeyHolder keyHolder = new KeyHolder(userModel.getName(), contents);
		keyStore.put(keyHolder);
		assertThat(getKeysApi().listSshKeys(), Matchers.not(Matchers.empty()));
		resetMockCounters();
	}

	@When("^the key is removed$")
	public void theKeyIsRemoved() throws Throwable {
		KeysApi keysApi = getKeysApi();
		keysApi.deleteSshKey("");
	}

	@Then("^the key is removed from the keystore$")
	public void theKeyIsRemovedFromTheKeystore() throws Throwable {
		assertThat(getKeysApi().listSshKeys(), Matchers.empty());
	}

}
