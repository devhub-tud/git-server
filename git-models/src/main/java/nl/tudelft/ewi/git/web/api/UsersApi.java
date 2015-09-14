package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.IdentifiableModel;
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
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface UsersApi {

	@GET
	Collection<IdentifiableModel> listAllUsers();

	@Path("{username}")
	UserApi getUser(@NotNull @PathParam("username") String username);

	@POST
	@Deprecated
	@Consumes(MediaType.APPLICATION_JSON)
	UserModel createNewUser(@Valid UserModel model);

}
