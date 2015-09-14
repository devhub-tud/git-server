package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.UserModel;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface UserApi {

	@GET
	UserModel get();

	@DELETE
	@Produces(MediaType.WILDCARD)
	void deleteUser();

	@Path("keys")
	KeysApi keys();

}
