package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.DiffModel;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Both the {@link CommitApi} and {@link BranchApi} have diff endpoints.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface DiffableApi {

	default DiffModel diff(String oldCommitId) {
		return diff(oldCommitId, 3);
	}

	@GET
	@Path("diff/{oldCommitId}")
	DiffModel diff(@QueryParam("oldCommitId") @NotNull String oldCommitId,
	               @DefaultValue("3") @QueryParam("context") int context);

	default DiffBlameModel diffBlame(String oldCommitId) {
		return diffBlame(oldCommitId, 3);
	}

	@GET
	@Path("diff-blame/{oldCommitId}")
	DiffBlameModel diffBlame(@QueryParam("oldCommitId") @NotNull String oldCommitId,
	                         @DefaultValue("3") @QueryParam("context") int context);

	/**
	 * @return a {@link DiffModel} for this diff with the default context size.
	 */
	default DiffModel diff() {
		return diff(3);
	}

	/**
	 * @param context amount of context lines.
	 * @return a {@link DiffModel} for this diff.
	 */
	@GET
	@Path("diff")
	DiffModel diff(@DefaultValue("3") @QueryParam("context") int context);

	/**
	 * @return a {@link DiffBlameModel} for this diff with the default context size.
	 */
	default DiffBlameModel diffBlame() {
		return diffBlame(3);
	}

	/**
	 * @param context amount of context lines.
	 * @return a {@link DiffBlameModel} for this diff.
	 */
	@GET
	@Path("diff-blame")
	DiffBlameModel diffBlame(@DefaultValue("3") @QueryParam("context") int context);

}
