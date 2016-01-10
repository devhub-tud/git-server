package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CommitSubList;
import nl.tudelft.ewi.git.models.MergeResponse;
import org.hibernate.validator.constraints.NotEmpty;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * The {@code BranchApi} is an API endpoint for requests on a branch objet.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface BranchApi extends DiffableApi {

	/**
	 * Default page size.
	 */
	int DEFAULT_PAGE_SIZE = 25;

	/**
	 * Get the {@code BranchModel}.
	 *
	 * @return the {@code BranchModel}.
	 */
	@GET
	BranchModel get();

	/**
	 * Merge the branch into the master.
	 *
	 * @param message Commit message for the merge commit.
	 * @param name Author name for the merge commit.
	 * @param email Author email for the merge commit.
	 * @return the {@code MergeResult}.
	 */
	@POST
	@Path("merge")
	MergeResponse merge(@QueryParam("message") String message,
	                    @QueryParam("name") @NotEmpty String name,
	                    @QueryParam("email") @NotEmpty String email);

	/**
	 * Retrieve the commits in this branch.
	 *
	 * @return a {@link CommitSubList}.
	 * @see #retrieveCommitsInBranch(int, int)
	 * @deprecated Any client will have to use pagination, and thus have these values precomputed anyway.
	 */
	@GET
	@Deprecated
	@Path("default/commits-skip0-limit25")
	default CommitSubList retrieveCommitsInBranch() {
		return retrieveCommitsInBranch(0, DEFAULT_PAGE_SIZE);
	}

	/**
	 * Retrieve the commits in this branch.
	 *
	 * @param skip Amount of commits to skip.
	 * @param limit Limit the amount of results.
	 * @return a {@link CommitSubList}.
	 */
	@GET
	@Path("commits")
	CommitSubList retrieveCommitsInBranch(@QueryParam("skip") @DefaultValue("0") int skip,
	                                      @QueryParam("limit") @DefaultValue("25") int limit);

	/**
	 * Delete the current branch.
	 */
	@DELETE
	void deleteBranch();

	/**
	 * Get the branch commit.
	 *
	 * @return {@link CommitApi} for the commit ref of this branch.
	 */
	@Path("commit")
	CommitApi getCommit();

	/**
	 * Get the merge base for this branch.
	 *
	 * @return {@link CommitApi} for the merge-base ref of this branch.
	 */
	@Path("merge-base")
	CommitApi mergeBase();

}
