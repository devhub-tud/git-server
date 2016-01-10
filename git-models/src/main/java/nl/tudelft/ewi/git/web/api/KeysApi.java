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
 * API for interating with Ssh keys.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface KeysApi {

	/**
	 * List the SSH keys.
	 * @return a collection of SSH keys.
	 */
	@GET
	Collection<SshKeyModel> listSshKeys();

	/**
	 * Retrieve an SSH key.
	 * @param keyId Name for the key.
	 * @return the {@link SshKeyModel}.
	 */
	@GET
	@Path("{keyId}")
	SshKeyModel retrieveSshKey(@NotNull @PathParam("keyId") String keyId);

	/**
	 * Add a new SSH key.
	 * @param sshKeyModel {@link SshKeyModel} for the SSH key.
	 * @return the {@link SshKeyModel}.
	 */
	@POST
	SshKeyModel addNewKey(@Valid SshKeyModel sshKeyModel);

	/**
	 * Delete SSH key.
	 * @param keyId name for the SSH key.
	 */
	@DELETE
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("{keyId}")
	void deleteSshKey(@NotNull @PathParam("keyId") String keyId);

}
