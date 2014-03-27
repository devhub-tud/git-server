package nl.tudelft.ewi.git.web;

import java.io.IOException;
import java.util.Collection;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nl.minicom.gitolite.manager.exceptions.GitException;
import nl.minicom.gitolite.manager.exceptions.ModificationException;
import nl.minicom.gitolite.manager.exceptions.ServiceUnavailable;
import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.ConfigManager;
import nl.minicom.gitolite.manager.models.Group;
import nl.minicom.gitolite.manager.models.User;
import nl.tudelft.ewi.git.models.GroupModel;
import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.git.models.Transformers;
import nl.tudelft.ewi.git.models.UserModel;
import nl.tudelft.ewi.git.web.security.RequireAuthentication;

import org.jboss.resteasy.plugins.guice.RequestScoped;
import org.jboss.resteasy.plugins.validation.hibernate.ValidateRequest;

import com.google.common.collect.Collections2;

/**
 * This class is a RESTEasy resource which provides an interface to users over HTTP to retrieve,
 * list, create, and remove groups in the Gitolite configuration.
 */
@Path("api/groups")
@RequestScoped
@ValidateRequest
@RequireAuthentication
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GroupsApi extends BaseApi {

	private final ConfigManager manager;

	@Inject
	GroupsApi(ConfigManager manager) {
		this.manager = manager;
	}

	/**
	 * This will list all groups currently in the Gitolite configuration.
	 * 
	 * @return A {@link Collection} of {@link IdentifiableModel}s, each representing a group in the
	 *         Gitolite configuration.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@GET
	public Collection<IdentifiableModel> listAllGroups() throws IOException, ServiceUnavailable, GitException {
		Config config = manager.get();
		return Collections2.transform(config.getGroups(), Transformers.identifiables());
	}

	/**
	 * This will retrieve a representation of a specific group in the Gitolite configuration.
	 * 
	 * @param groupId
	 *            The <code>@name</code> of the group to retrieve.
	 * @return A {@link GroupModel} representation of the specified group.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@GET
	@Path("{groupId}")
	public GroupModel getGroup(@PathParam("groupId") String groupId) throws IOException, ServiceUnavailable,
			GitException {

		Config config = manager.get();
		Group group = fetchGroup(config, decode(groupId));
		return Transformers.groups().apply(group);
	}

	/**
	 * This creates a new group in the Gitolite configuration and returns a representation of it.
	 * 
	 * @param model
	 *            A {@link GroupModel} describing the properties of the group.
	 * @return A {@link GroupModel} representing the final properties of the created group.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws ModificationException
	 *             If the modification conflicted with another request.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@POST
	public GroupModel createNewGroup(@Valid GroupModel model) throws IOException, ServiceUnavailable,
			ModificationException, GitException {

		Config config = manager.get();
		Group group = config.createGroup(model.getName());
		if (model.getMembers() != null) {
			for (IdentifiableModel identifiable : model.getMembers()) {
				String name = identifiable.getName();
				if (identifiable instanceof UserModel) {
					group.add(fetchUser(config, name));
				}
				else if (identifiable instanceof GroupModel) {
					group.add(fetchGroup(config, name));
				}
			}
		}
		manager.apply(config);
		return Transformers.groups().apply(group);
	}

	/**
	 * This removes an existing group from the Gitolite configuration.
	 * 
	 * @param groupId
	 *            The <code>@name</code> of the group to remove.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws ModificationException
	 *             If the modification conflicted with another request.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@DELETE
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("{groupId}")
	public void deleteGroup(@PathParam("groupId") String groupId) throws IOException, ServiceUnavailable,
			ModificationException, GitException {

		Config config = manager.get();
		Group group = fetchGroup(config, decode(groupId));
		config.removeGroup(group);
		manager.apply(config);
	}

	/**
	 * This lists all the members of a specific group in the Gitolite configuration.
	 * 
	 * @param groupId
	 *            The <code>@name</code> of the group to list all members of.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@GET
	@Path("{groupId}/members")
	public Collection<IdentifiableModel> listMembers(@PathParam("groupId") String groupId) throws IOException,
			ServiceUnavailable, GitException {

		Config config = manager.get();
		Group group = fetchGroup(config, decode(groupId));
		return Collections2.transform(group.getAllMembers(), Transformers.detailedIdentifiables());
	}

	/**
	 * This adds either a group or user to another group as a member in the Gitolite configuration.
	 * 
	 * @param groupId
	 *            The <code>@name</code> of the group to add a member to.
	 * @param model
	 *            The {@link IdentifiableModel} describing the user or group to add as a member.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws ModificationException
	 *             If the modification conflicted with another request.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@POST
	@Path("{groupId}/members")
	public Collection<IdentifiableModel> addNewMember(@PathParam("groupId") String groupId,
			@Valid IdentifiableModel model) throws IOException, ServiceUnavailable, ModificationException, GitException {

		Config config = manager.get();
		Group group = fetchGroup(config, decode(groupId));

		if (model instanceof UserModel) {
			group.add(fetchUser(config, model.getName()));
		}
		else if (model instanceof GroupModel) {
			group.add(fetchGroup(config, model.getName()));
		}

		manager.apply(config);
		return Collections2.transform(group.getAllMembers(), Transformers.detailedIdentifiables());
	}

	/**
	 * This removes either a group or user from another group in the Gitolite configuration.
	 * 
	 * @param groupId
	 *            The <code>@name</code> of the group to remove a member from.
	 * @param identifiableId
	 *            The name describing the user or group to remove from the group.
	 * @throws IOException
	 *             If one or more files in the repository could not be read.
	 * @throws ServiceUnavailable
	 *             If the service could not be reached.
	 * @throws ModificationException
	 *             If the modification conflicted with another request.
	 * @throws GitException
	 *             If an exception occurred while using the Git API.
	 */
	@DELETE
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("{groupId}/members/{identifiableId}")
	public void removeMember(@PathParam("groupId") String groupId, @PathParam("identifiableId") String identifiableId)
			throws IOException, ServiceUnavailable, ModificationException, GitException {

		Config config = manager.get();
		Group group = fetchGroup(config, decode(groupId));
		identifiableId = decode(identifiableId);

		if (identifiableId.startsWith("@")) {
			Group subGroup = fetchGroup(config, identifiableId);
			if (!group.containsGroup(subGroup)) {
				throw new NotFoundException("The group: " + identifiableId + " is not a member of the group: "
						+ groupId);
			}
			group.remove(subGroup);
		}
		else {
			User member = fetchUser(config, identifiableId);
			if (!group.containsUser(member)) {
				throw new NotFoundException("The user: " + identifiableId + " is not a member of the group: " + groupId);
			}
			group.remove(member);
		}
		manager.apply(config);
	}

}
