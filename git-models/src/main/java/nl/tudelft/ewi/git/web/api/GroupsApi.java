package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;

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
 * API endpoint for interacting with groups.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface GroupsApi {

	/**
	 * @return A list of all groups.
	 */
	@GET
	Collection<IdentifiableModel> listAllGroups();

	/**
	 * Create a new Group
	 * @param groupModel Group to create.
	 * @return created group.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	GroupModel create(@Valid GroupModel groupModel);

	/**
	 * @param groupName Name for the group to retrieve.
	 * @return An Api for the group.
	 */
	@Path("{groupName}")
	GroupApi getGroup(@NotNull @PathParam("groupName") String groupName);

}
