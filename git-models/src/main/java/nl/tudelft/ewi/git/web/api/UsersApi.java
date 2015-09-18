package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.git.models.SshKeyModel;
import nl.tudelft.ewi.git.models.UserModel;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * API to interact with users.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface UsersApi {

	/**
	 * List all users.
	 * @return a collection of users.
	 */
	@GET
	Collection<IdentifiableModel> listAllUsers();

	/**
	 * Get a specific user.
	 * @param username username for the user.
	 * @return the UserAPI.
	 * @see UserApi#get()
	 */
	@Path("{username}")
	UserApi getUser(@NotNull @PathParam("username") String username);

	/**
	 * Create a new user.

	 * @param model {@link UserModel} to create.
	 * @return the created user.
	 * @see #getUser(String)
	 * @see UserApi#keys()
	 * @see KeysApi#addNewKey(SshKeyModel)
	 *
	 * @deprecated
	 *    Users are identified by the username under which an SSH-key
	 *    is stored in the Key Store. These usernames are then matched
	 *    against access rules in the Gitolite config. Therefore, a
	 *    user cannot exist without an SSH key, and should be created
	 *    through {@link UserApi#keys()}.
	 *
	 */
	@POST
	@Deprecated
	@Consumes(MediaType.APPLICATION_JSON)
	UserModel createNewUser(@Valid UserModel model);

}
