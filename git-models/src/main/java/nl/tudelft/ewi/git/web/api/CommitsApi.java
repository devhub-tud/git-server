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
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface CommitsApi {

	@GET
	@Path("commits")
	Collection<CommitModel> listCommits();

	@Path("commits/{commitId}")
	CommitApi getCommit(@NotNull @PathParam("commitId") String commitId);

}
