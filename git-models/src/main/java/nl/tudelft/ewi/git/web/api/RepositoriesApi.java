package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel;

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
 * API endpoint for interacting with repositories.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface RepositoriesApi {

	/**
	 * List all repositories.
	 *
	 * @return a collection of repositories.
	 */
	@GET
	Collection<RepositoryModel> listAllRepositories();

	/**
	 * Get a specific repository.
	 * @param repositoryId name for the repository.
	 * @return a Repository API endpoint.
	 * @see RepositoryApi#getRepositoryModel()
	 */
	@Path("{repoId}")
	RepositoryApi getRepository(@NotNull @PathParam("repoId") String repositoryId);

	/**
	 * Create a new repository.
	 * @param createRepositoryModel RepositoryModel to create.
	 * @return the created repository.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	DetailedRepositoryModel createRepository(@Valid CreateRepositoryModel createRepositoryModel);


}
