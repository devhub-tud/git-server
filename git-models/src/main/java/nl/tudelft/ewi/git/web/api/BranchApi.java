package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CommitSubList;
import nl.tudelft.ewi.git.models.MergeResponse;

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
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface BranchApi extends DiffableApi {

	@GET
	BranchModel get();

	@POST
	@Path("merge")
	MergeResponse merge();

	default CommitSubList retrieveCommitsInBranch() {
		return retrieveCommitsInBranch(0, 25);
	}

	@GET
	@Path("commits")
	CommitSubList retrieveCommitsInBranch(@QueryParam("skip") @DefaultValue("0") int skip,
	                                      @QueryParam("limit") @DefaultValue("25") int limit);

	@GET
	@Path("merge-base")
	CommitModel mergeBase();

	@DELETE
	void deleteBranch();

	@Path("commit")
	CommitApi getCommit();

}
