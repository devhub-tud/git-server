package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.CommitModel;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * API for interacting with commit objects.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface CommitsApi {

	/**
	 * List all commits.
	 *
	 * @return a collection of commits.
	 */
	@GET
	@Path("commits")
	Collection<CommitModel> listCommits();

	/**
	 * Get a specific commit.
	 *
	 * @param commitId Commit id of the commit.
	 * @return {@link CommitApi} for the commit.
	 */
	@Path("commits/{commitId}")
	CommitApi getCommit(@NotNull @PathParam("commitId") String commitId);

}
