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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import nl.minicom.gitolite.manager.exceptions.GitException;
import nl.minicom.gitolite.manager.exceptions.ModificationException;
import nl.minicom.gitolite.manager.exceptions.ServiceUnavailable;
import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.ConfigManager;
import nl.minicom.gitolite.manager.models.User;
import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.git.models.SshKeyModel;
import nl.tudelft.ewi.git.models.Transformers;
import nl.tudelft.ewi.git.models.UserModel;
import nl.tudelft.ewi.git.web.security.RequireAuthentication;

import org.apache.sshd.common.util.Buffer;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.ImmutablePair;

import org.jboss.resteasy.plugins.guice.RequestScoped;
import org.jboss.resteasy.plugins.validation.hibernate.ValidateRequest;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;

/**
 * This class is a RESTEasy resource which provides an interface to users over HTTP to retrieve, list, create, and
 * remove users in the Gitolite configuration.
 * 
 * @author michael
 */
@Path("api/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
@ValidateRequest
@RequireAuthentication
public class UsersApi extends BaseApi {

	private final ConfigManager manager;

	@Inject
	UsersApi(ConfigManager manager) {
		this.manager = manager;
	}

	/**
	 * This will list all users currently in the Gitolite configuration.
	 * 
	 * @return A {@link Collection} of {@link IdentifiableModel}s, each representing a user in the Gitolite
	 *         configuration.
	 * @throws IOException
	 *         If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *         If the service could not be reached.
	 * @throws GitException
	 *         If an exception occurred while using the Git API.
	 */
	@GET
	public Collection<IdentifiableModel> listAllUsers() throws IOException, ServiceUnavailable, GitException {
		Config config = manager.get();
		return Collections2.transform(config.getUsers(), Transformers.identifiables());
	}

	/**
	 * This will retrieve a representation of a specific user in the Gitolite configuration.
	 * 
	 * @param userId
	 *        The <code>name</code> of the user to retrieve.
	 * @return A {@link UserModel} representation of the specified user.
	 * @throws IOException
	 *         If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *         If the service could not be reached.
	 * @throws GitException
	 *         If an exception occurred while using the Git API.
	 */
	@GET
	@Path("{userId}")
	public UserModel getUser(@PathParam("userId") String userId) throws IOException, ServiceUnavailable, GitException {
		Config config = manager.get();
		User user = fetchUser(config, decode(userId));
		return Transformers.users().apply(user);
	}

	/**
	 * This creates a new user in the Gitolite configuration and returns a representation of it.
	 * 
	 * @param model
	 *        A {@link UserModel} describing the properties of the user.
	 * @return A {@link UserModel} representing the final properties of the created user.
	 * @throws IOException
	 *         If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *         If the service could not be reached.
	 * @throws ModificationException
	 *         If the modification conflicted with another request.
	 * @throws GitException
	 *         If an exception occurred while using the Git API.
	 */
	@POST
	public UserModel createNewUser(@Valid UserModel model) throws IOException, ServiceUnavailable,
			ModificationException, GitException {

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

	/**
	 * This removes an existing user from the Gitolite configuration.
	 * 
	 * @param userId
	 *        The <code>name</code> of the user to remove.
	 * @throws IOException
	 *         If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *         If the service could not be reached.
	 * @throws ModificationException
	 *         If the modification conflicted with another request.
	 * @throws GitException
	 *         If an exception occurred while using the Git API.
	 */
	@DELETE
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("{userId}")
	public void deleteUser(@PathParam("userId") String userId) throws IOException, ServiceUnavailable,
			ModificationException, GitException {

		Config config = manager.get();
		User user = fetchUser(config, decode(userId));
		config.removeUser(user);
		manager.apply(config);
	}

	/**
	 * This lists all the SSH keys of a specific user in the Gitolite configuration.
	 * 
	 * @param userId
	 *        The <code>name</code> of the user to list all SSH keys of.
	 * @throws IOException
	 *         If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *         If the service could not be reached.
	 * @throws GitException
	 *         If an exception occurred while using the Git API.
	 */
	@GET
	@Path("{userId}/keys")
	public Collection<SshKeyModel> listSshKeys(@PathParam("userId") String userId) throws IOException,
			ServiceUnavailable, GitException {

		Config config = manager.get();
		User user = fetchUser(config, decode(userId));
		return Collections2.transform(user.getKeys().entrySet(), Transformers.sshKeys(user));
	}

	/**
	 * This returns a representation of the specified SSH key of a specific user in the Gitolite configuration.
	 * 
	 * @param userId
	 *        The <code>name</code> of the user to return the SSH key of.
	 * @param keyId
	 *        The <code>name</code> of the SSH key.
	 * @throws IOException
	 *         If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *         If the service could not be reached.
	 * @throws GitException
	 *         If an exception occurred while using the Git API.
	 */
	@GET
	@Path("{userId}/keys/{keyId}")
	public SshKeyModel retrieveSshKey(@PathParam("userId") String userId, @PathParam("keyId") String keyId)
			throws IOException, ServiceUnavailable, GitException {

		keyId = decode(keyId);
		if (keyId.equals("default")) {
			keyId = "";
		}
		
		Config config = manager.get();
		User user = fetchUser(config, decode(userId));
		ImmutableMap<String, String> keys = user.getKeys();
		if (!keys.containsKey(keyId)) {
			throw new IllegalArgumentException("The user: " + user.getName() + " doesn't own any keys named: \"" + keyId + "\"!");
		}

		String key = keys.get(keyId);
		if (key == null) {
			throw new NotFoundException("Could not find SSH key: " + keyId);
		}

		return Transformers.sshKeys(user).apply(ImmutablePair.of(keyId, key));
	}

	/**
	 * This adds a new SSH key to a user in the Gitolite configuration.
	 * 
	 * @param userId
	 *        The <code>name</code> of the user to add a SSH key to.
	 * @param model
	 *        The {@link SshKeyModel} describing the SSH key to add.
	 * @throws IOException
	 *         If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *         If the service could not be reached.
	 * @throws ModificationException
	 *         If the modification conflicted with another request.
	 * @throws GitException
	 *         If an exception occurred while using the Git API.
	 */
	@POST
	@Path("{userId}/keys")
	public SshKeyModel addNewKey(@PathParam("userId") String userId, @Valid SshKeyModel model) throws IOException,
			ServiceUnavailable, ModificationException, GitException {

		String keyId = model.getName();
		String content = model.getContents();

		Config config = manager.get();
		User user = fetchUser(config, decode(userId));
		if (keyId.equals("default")) {
			keyId = "";
		}

		validateKeyNotExists(model, keyId, user);
		validateKeyContents(content);
		user.setKey(keyId, model.getContents());
		manager.apply(config);

		return Transformers.sshKeys(user).apply(ImmutablePair.of(keyId, content));
	}

	/**
	 * Validate that the key does not exists
	 *
	 * @param model
	 * 			The {@link SshKeyModel} describing the SSH key to add.
	 * @param keyId
	 * 			KeyId for key
	 * @param user
	 * 			User for key
	 */
	@VisibleForTesting
	static void validateKeyNotExists(SshKeyModel model, String keyId, User user) {
		if (user.getKeys().containsKey(keyId)) {
			throw new IllegalArgumentException("A key with name: \"" + model.getName() + "\" already exists!");
		}
	}

	/**
	 * Validate the contents of a key. This method checks the the key contents stronger
	 * than the regular expression does. A valid key consists of:
	 *
	 * <ul>
	 * 	<li>the prefix {@code ssh-rsa}</li>
	 * 	<li>the RSA modulus, a positive integer</li>
	 * 	<li>the RSA public exponent, a positive integer</li>
	 * </ul>
	 *
	 * The key should be Base64 decoded.
	 *
	 * @param content key contents
	 */
	@VisibleForTesting
	static void validateKeyContents(final String content) {
		Preconditions.checkNotNull(content);

		try {
			String[] parts = content.split("[\\r\\n\\s]+");
			String keyPart = parts[1];
			final byte[] bin = Base64.decodeBase64(keyPart);
			new Buffer(bin).getRawPublicKey();
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Validation failed for key \"" + content + "\"", e);
		}
	}

	/**
	 * This removes a specific SSH key from a user in the Gitolite configuration.
	 * 
	 * @param userId
	 *        The <code>name</code> of the user to remove a SSH key from.
	 * @param keyId
	 *        The <code>name</code> of the SSH key to remove.
	 * @throws IOException
	 *         If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *         If the service could not be reached.
	 * @throws ModificationException
	 *         If the modification conflicted with another request.
	 * @throws GitException
	 *         If an exception occurred while using the Git API.
	 */
	@DELETE
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("{userId}/keys/{keyId}")
	public void deleteSshKey(@PathParam("userId") String userId, @PathParam("keyId") String keyId) throws IOException,
			ServiceUnavailable, ModificationException, GitException {

		keyId = decode(keyId);
		userId = decode(userId);
		if (keyId.equals("default")) {
			keyId = "";
		}
		
		Config config = manager.get();
		User user = fetchUser(config, userId);
		if (!user.getKeys().containsKey(keyId)) {
			throw new IllegalArgumentException("User: \"" + userId + "\" doesn't own any keys named: \"" + keyId + "\"!");
		}

		user.removeKey(keyId);
		manager.apply(config);
	}

}
