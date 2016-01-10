package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.Version;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Path("api")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface BaseApi {

	/**
	 * Interact with Groups.
	 * @return the Groups API.
	 */
	@Path("groups")
	GroupsApi groups();

	/**
	 * Interact with users.
	 * @return the Users API.
	 */
	@Path("users")
	UsersApi users();

	/**
	 * Get the repositories API.
	 * @return the repositories API.
	 */
	@Path("repositories")
	RepositoriesApi repositories();

	/**
	 * Get the git server version.
	 * @return the current version of the Git server.
	 */
	@GET
	@Path("version")
	Version version();

}
