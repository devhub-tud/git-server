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
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface RepositoriesApi {

	@GET
	Collection<RepositoryModel> listAllRepositories();

	@Path("{repoId}")
	RepositoryApi getRepository(@NotNull @PathParam("repoId") String repositoryId);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	DetailedRepositoryModel createRepository(@Valid CreateRepositoryModel createRepositoryModel);


}
