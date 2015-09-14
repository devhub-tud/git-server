package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.models.TagModel;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
public interface RepositoryApi extends CommitsApi {

	@GET
	DetailedRepositoryModel getRepositoryModel();

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	DetailedRepositoryModel updateRepository(@Valid RepositoryModel repositoryModel);

	@DELETE
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	void deleteRepository();

	@GET
	@Path("tags")
	Collection<TagModel> getTags();

	@GET
	@Path("branches")
	Collection<BranchModel> getBranches();

	@Path("branch/{branchName}")
	BranchApi getBranch(@NotNull @PathParam("branchName") String branchName);

	@POST
	@Path("tag")
	TagModel addTag(@Valid TagModel tagModel);

}
