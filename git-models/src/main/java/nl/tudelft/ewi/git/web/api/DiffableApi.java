package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.DiffModel;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

	/**
	 * Default amount of context lines.
	 */
	int DEFAULT_CONTEXT_AMOUNT = 3;

	/**
	 * Default amount of context lines.
	 */
	String DEFAULT_CONTEXT_AMOUNT_STR = "3";

	/**
	 * Generate a diff for this commit.
	 *
	 * @param oldCommitId Commit id to compare with.
	 * @return {@link DiffModel}.
	 * @see CommitApi#diff(String, int)
	 */
	default DiffModel diff(String oldCommitId) {
		return diff(oldCommitId, DEFAULT_CONTEXT_AMOUNT);
	}

	/**
	 * Generate a diff for this commit.
	 *
	 * @param oldCommitId Commit id to compare with.
	 * @param context Amount of context lines.
	 * @return {@link DiffModel}.
	 */
	@GET
	@Path("diff/{oldCommitId}")
	DiffModel diff(@PathParam("oldCommitId") @NotNull String oldCommitId,
	               @DefaultValue(DEFAULT_CONTEXT_AMOUNT_STR) @QueryParam("context") int context);

	/**
	 * Generate a diff with blame details.
	 *
	 * @param oldCommitId Commit id to compare with.
	 * @return {@link DiffBlameModel}.
	 * @see #diffBlame(String, int)
	 */
	default DiffBlameModel diffBlame(String oldCommitId) {
		return diffBlame(oldCommitId, DEFAULT_CONTEXT_AMOUNT);
	}

	/**
	 * Generate a diff with blame details.
	 *
	 * @param oldCommitId Commit id to compare with.
	 * @param context Amount of context lines.
	 * @return {@link DiffBlameModel}.
	 */
	@GET
	@Path("diff-blame/{oldCommitId}")
	DiffBlameModel diffBlame(@PathParam("oldCommitId") @NotNull String oldCommitId,
	                         @DefaultValue(DEFAULT_CONTEXT_AMOUNT_STR) @QueryParam("context") int context);

	/**
	 * Generate a diff.
	 *
	 * @return a {@link DiffModel} for this diff with the default context size.
	 * @see #diff(int)
	 */
	default DiffModel diff() {
		return diff(DEFAULT_CONTEXT_AMOUNT);
	}

	/**
	 * Generate a diff.
	 *
	 * @param context amount of context lines.
	 * @return a {@link DiffModel} for this diff.
	 */
	@GET
	@Path("diff")
	DiffModel diff(@DefaultValue(DEFAULT_CONTEXT_AMOUNT_STR) @QueryParam("context") int context);

	/**
	 * Generate a diff with blame details.
	 *
	 * @return a {@link DiffBlameModel} for this diff with the default context size.
	 * @see #diffBlame(int)
	 */
	default DiffBlameModel diffBlame() {
		return diffBlame(3);
	}

	/**
	 * Generate a diff with blame details.
	 *
	 * @param context amount of context lines.
	 * @return a {@link DiffBlameModel} for this diff.
	 */
	@GET
	@Path("diff-blame")
	DiffBlameModel diffBlame(@DefaultValue(DEFAULT_CONTEXT_AMOUNT_STR) @QueryParam("context") int context);

}
