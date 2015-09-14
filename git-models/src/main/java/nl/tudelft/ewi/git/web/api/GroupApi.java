package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface GroupApi {

	/**
	 * @return the group.
	 */
	@GET
	GroupModel getGroup();

	/**
	 * Delete the group.
	 */
	@DELETE
	@Produces(MediaType.WILDCARD)
	@Consumes(MediaType.WILDCARD)
	void deleteGroup();

	@GET
	@Path("members")
	Collection<IdentifiableModel> listMembers();

	@POST
	@Path("members")
	@Consumes(MediaType.APPLICATION_JSON)
	Collection<IdentifiableModel> addNewMember(@Valid IdentifiableModel model);

	@DELETE
	@Path("members")
	@Produces(MediaType.WILDCARD)
	@Consumes(MediaType.APPLICATION_JSON)
	void removeMember(@Valid IdentifiableModel model);

}
