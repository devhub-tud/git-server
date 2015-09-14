package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.SshKeyModel;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
public interface KeysApi {

	@GET
	Collection<SshKeyModel> listSshKeys();

	@GET
	@Path("{keyId}")
	SshKeyModel retrieveSshKey(@NotNull @PathParam("keyId") String keyId);

	@POST
	SshKeyModel addNewKey(@Valid SshKeyModel sshKeyModel);

	@DELETE
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("{keyId}")
	void deleteSshKey(@NotNull @PathParam("keyId") String keyId);

}
