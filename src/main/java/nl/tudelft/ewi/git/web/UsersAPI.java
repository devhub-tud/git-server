package nl.tudelft.ewi.git.web;

import java.io.IOException;
import java.util.Collection;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nl.minicom.gitolite.manager.exceptions.GitException;
import nl.minicom.gitolite.manager.exceptions.ModificationException;
import nl.minicom.gitolite.manager.exceptions.ServiceUnavailable;
import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.ConfigManager;
import nl.minicom.gitolite.manager.models.User;
import nl.tudelft.ewi.git.web.models.IdentifiableModel;
import nl.tudelft.ewi.git.web.models.SshKeyModel;
import nl.tudelft.ewi.git.web.models.Transformers;
import nl.tudelft.ewi.git.web.models.UserModel;
import nl.tudelft.ewi.git.web.security.RequireAuthentication;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jboss.resteasy.plugins.guice.RequestScoped;
import org.jboss.resteasy.plugins.validation.hibernate.ValidateRequest;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;

@Path("api/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

@RequestScoped
@ValidateRequest
@RequireAuthentication
public class UsersAPI {
	
	private final ConfigManager manager;

	@Inject
	public UsersAPI(ConfigManager manager) {
		this.manager = manager;
	}

	@GET
	public Collection<IdentifiableModel> listAllUsers() throws IOException, ServiceUnavailable, GitException {
		Config config = manager.get();
		return Collections2.transform(config.getUsers(), Transformers.identifiables());
	}
	
	@GET
	@Path("{userId}")
	public UserModel getUser(@PathParam("userId") String userId) throws IOException, ServiceUnavailable, GitException {
		Config config = manager.get();
		User user = fetchUser(config, userId);
		return Transformers.users().apply(user);
	}
	
	@POST
	public UserModel createNewUser(@Valid UserModel model) throws IOException, ServiceUnavailable, ModificationException, GitException {
		Config config = manager.get();
		User user = config.createUser(model.getName());
		if (model.getKeys() != null) {
			for (SshKeyModel key : model.getKeys()) {
				user.setKey(key.getName(), key.getContents());
			}
		}
		manager.apply(config);
		return Transformers.users().apply(user);
	}
	
	@DELETE
	@Path("{userId}")
	public void deleteUser(@PathParam("userId") String userId) throws IOException, ServiceUnavailable, ModificationException, GitException {
		Config config = manager.get();
		User user = fetchUser(config, userId);
		config.removeUser(user);
		manager.apply(config);
	}
	
	@GET
	@Path("{userId}/keys")
	public Collection<SshKeyModel> listSshKeys(@PathParam("userId") String userId) throws IOException, ServiceUnavailable, GitException {
		Config config = manager.get();
		User user = fetchUser(config, userId);
		return Collections2.transform(user.getKeys().entrySet(), Transformers.sshKeys(user));
	}
	
	@GET
	@Path("{userId}/keys/{keyId}")
	public SshKeyModel retrieveSshKey(@PathParam("userId") String userId, @PathParam("keyId") String keyId) throws IOException, ServiceUnavailable, GitException {
		Config config = manager.get();
		User user = fetchUser(config, userId);
		if (!keyId.endsWith(user.getName() + ".pub")) {
			throw new IllegalArgumentException("The user: " + user.getName() + " is not the owner of the key: " + keyId);
		}
		
		ImmutableMap<String, String> keys = user.getKeys();
		String keyName = stripUsername(keyId);
		String key = keys.get(keyName);
		if (key == null) {
			throw new NotFoundException("Could not find SSH key: " + keyId);
		}
		
		return Transformers.sshKeys(user).apply(ImmutablePair.of(keyName, key));
	}
	
	@POST
	@Path("{userId}/keys")
	public SshKeyModel addNewKey(@PathParam("userId") String userId, @Valid SshKeyModel sshKey) throws IOException, ServiceUnavailable, ModificationException, GitException {
		Config config = manager.get();
		User user = fetchUser(config, userId);
		if (!sshKey.getName().endsWith(user.getName() + ".pub")) {
			throw new IllegalArgumentException("The name of the key must be either of the form: <key_name>@" + user.getName() + ".pub or " + user.getName() + ".pub");
		}
		
		String keyName = stripUsername(sshKey.getName());
		if (user.getKeys().containsKey(keyName)) {
			throw new IllegalArgumentException("A key with name: \"" + sshKey.getName() + "\" already exists!");
		}
		user.setKey(keyName, sshKey.getContents());
		manager.apply(config);
		
		return Transformers.sshKeys(user).apply(ImmutablePair.of(keyName, sshKey.getContents())); 
	}
	
	@DELETE
	@Path("{userId}/keys/{keyId}")
	public void deleteSshKey(@PathParam("userId") String userId, @PathParam("keyId") String keyId) throws IOException, ServiceUnavailable, ModificationException, GitException {
		Config config = manager.get();
		User user = fetchUser(config, userId);
		if (!keyId.endsWith(user.getName() + ".pub")) {
			throw new IllegalArgumentException("The user: " + user.getName() + " is not the owner of the key: " + keyId);
		}
				
		String keyName = stripUsername(keyId);
		if (!user.getKeys().containsKey(keyName)) {
			throw new IllegalArgumentException("User: \"" + userId + "\" doesn't own any keys named: \"" + keyId + "\"!");
		}
		
		user.removeKey(keyName);
		manager.apply(config);
	}

	private User fetchUser(Config config, String userId) {
		User user = config.getUser(userId);
		if (user == null) {
			throw new NotFoundException("Could not find user: " + userId);
		}
		return user;
	}
	

	private String stripUsername(String fullKeyName) {
		if (fullKeyName.contains("@")) {
			return fullKeyName.substring(0, fullKeyName.indexOf('@'));
		}
		return "";
	}
	
}
