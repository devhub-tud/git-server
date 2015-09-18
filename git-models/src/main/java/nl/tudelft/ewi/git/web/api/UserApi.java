package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.UserModel;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * API to interact with a user.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface UserApi {

	/**
	 * Get the user model.
	 * @return the user model.
	 */
	@GET
	UserModel get();

	/**
	 * Delete the user.
	 */
	@DELETE
	@Produces(MediaType.WILDCARD)
	void deleteUser();

	/**
	 * Get the keys.
	 * @return Keys api.
	 * @see KeysApi#listSshKeys()
	 */
	@Path("keys")
	KeysApi keys();

}
