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
 * API endpoint for interacting with a repository.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface RepositoryApi extends CommitsApi {

	/**
	 * Get the repository model.
	 * @return The {@link DetailedRepositoryModel}.
	 */
	@GET
	DetailedRepositoryModel getRepositoryModel();

	/**
	 * Update the repositories permissions.
	 * @param repositoryModel Repository model.
	 * @return the updated repository.
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	DetailedRepositoryModel updateRepository(@Valid RepositoryModel repositoryModel);

	/**
	 * Delete the repository.
	 */
	@DELETE
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	void deleteRepository();

	/**
	 * List the tags.
	 * @return A collection of tags.
	 */
	@GET
	@Path("tags")
	Collection<TagModel> getTags();

	/**
	 * List the branches.
	 * @return A collection of branches.
	 */
	@GET
	@Path("branches")
	Collection<BranchModel> getBranches();

	/**
	 * Get a branch.
	 * @param branchName branch name for the branch.
	 * @return the branch.
	 * @see BranchApi#get()
	 */
	@Path("branch/{branchName}")
	BranchApi getBranch(@NotNull @PathParam("branchName") String branchName);

	/**
	 * Create a new tag.
	 * @param tagModel {@link TagModel} to create.
	 * @return the created tag.
	 */
	@POST
	@Path("tag")
	TagModel addTag(@Valid TagModel tagModel);

}
