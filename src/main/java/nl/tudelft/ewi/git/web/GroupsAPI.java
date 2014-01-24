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

import nl.minicom.gitolite.manager.exceptions.ModificationException;
import nl.minicom.gitolite.manager.exceptions.ServiceUnavailable;
import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.ConfigManager;
import nl.minicom.gitolite.manager.models.Group;
import nl.minicom.gitolite.manager.models.User;
import nl.tudelft.ewi.git.web.models.GroupModel;
import nl.tudelft.ewi.git.web.models.IdentifiableModel;
import nl.tudelft.ewi.git.web.models.Transformers;
import nl.tudelft.ewi.git.web.models.UserModel;
import nl.tudelft.ewi.git.web.security.RequireAuthentication;

import org.jboss.resteasy.plugins.guice.RequestScoped;
import org.jboss.resteasy.plugins.validation.hibernate.ValidateRequest;

import com.google.common.collect.Collections2;

@Path("api/groups")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

@RequestScoped
@ValidateRequest
@RequireAuthentication
public class GroupsAPI {
	
	private final ConfigManager manager;

	@Inject
	public GroupsAPI(ConfigManager manager) {
		this.manager = manager;
	}

	@GET
	public Collection<IdentifiableModel> listAllGroups() throws IOException, ServiceUnavailable {
		Config config = manager.get();
		return Collections2.transform(config.getGroups(), Transformers.identifiables());
	}
	
	@GET
	@Path("{groupId}")
	public GroupModel getGroup(@PathParam("groupId") String groupId) throws IOException, ServiceUnavailable {
		Config config = manager.get();
		Group group = fetchGroup(config, groupId);
		return Transformers.groups().apply(group);
	}
	
	@POST
	public GroupModel createNewGroup(@Valid GroupModel model) throws IOException, ServiceUnavailable, ModificationException {
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
	
	@DELETE
	@Path("{groupId}")
	public void deleteGroup(@PathParam("groupId") String groupId) throws IOException, ServiceUnavailable, ModificationException {
		Config config = manager.get();
		Group group = fetchGroup(config, groupId);
		config.removeGroup(group);
		manager.apply(config);
	}
	
	@GET
	@Path("{groupId}/members")
	public Collection<?> listMembers(@PathParam("groupId") String groupId) throws IOException, ServiceUnavailable {
		Config config = manager.get();
		Group group = fetchGroup(config, groupId);
		return Collections2.transform(group.getAllMembers(), Transformers.detailedIdentifiables());
	}
	
	@POST
	@Path("{groupId}/members")
	public Collection<IdentifiableModel> addNewMember(@PathParam("groupId") String groupId, @Valid IdentifiableModel model) throws IOException, ServiceUnavailable, ModificationException {
		Config config = manager.get();
		Group group = fetchGroup(config, groupId);
		
		if (model instanceof UserModel) {
			group.add(fetchUser(config, model.getName()));
		}
		else if (model instanceof GroupModel) {
			group.add(fetchGroup(config, model.getName()));
		}
		
		manager.apply(config);
		return Collections2.transform(group.getAllMembers(), Transformers.detailedIdentifiables());
	}

	@DELETE
	@Path("{groupId}/members/{identifiableId}")
	public void removeMember(@PathParam("groupId") String groupId, @PathParam("identifiableId") String identifiableId) throws IOException, ServiceUnavailable, ModificationException {
		Config config = manager.get();
		Group group = fetchGroup(config, groupId);
		if (identifiableId.startsWith("@")) {
			Group subGroup = fetchGroup(config, identifiableId);
			if (!group.containsGroup(subGroup)) {
				throw new NotFoundException("The group: " + identifiableId + " is not a member of the group: " + groupId);
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
	
	private Group fetchGroup(Config config, String groupId) {
		Group group = config.getGroup(groupId);
		if (group == null) {
			throw new NotFoundException("Could not find group: " + groupId);
		}
		return group;
	}

	private User fetchUser(Config config, String name) {
		User user = config.getUser(name);
		if (user == null) {
			throw new NotFoundException("Could not find user: " + name);
		}
		return user;
	}
	
}
