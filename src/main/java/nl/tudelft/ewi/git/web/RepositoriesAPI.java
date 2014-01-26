package nl.tudelft.ewi.git.web;

import java.io.IOException;
import java.util.Collection;

import javax.inject.Inject;
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
import nl.minicom.gitolite.manager.models.Permission;
import nl.minicom.gitolite.manager.models.Repository;
import nl.minicom.gitolite.manager.models.User;
import nl.tudelft.ewi.git.inspector.Inspector;
import nl.tudelft.ewi.git.web.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.web.models.RepositoryModel;
import nl.tudelft.ewi.git.web.models.Transformers;
import nl.tudelft.ewi.git.web.security.RequireAuthentication;

import org.jboss.resteasy.plugins.guice.RequestScoped;
import org.jboss.resteasy.plugins.validation.hibernate.ValidateRequest;

import com.google.common.collect.Collections2;

@Path("api/repositories")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

@RequestScoped
@ValidateRequest
@RequireAuthentication
public class RepositoriesAPI {
	
	private final ConfigManager manager;
	private final Inspector inspector;

	@Inject
	public RepositoriesAPI(ConfigManager manager, Inspector inspector) {
		this.manager = manager;
		this.inspector = inspector;
	}

	@GET
	public Collection<RepositoryModel> listAllRepositories() throws IOException, ServiceUnavailable {
		Config config = manager.get();
		return Collections2.transform(config.getRepositories(), Transformers.repositories());
	}
	
	@GET
	@Path("{repoId}")
	public DetailedRepositoryModel showRepository(@PathParam("repoId") String repoId) throws IOException, ServiceUnavailable {
		Config config = manager.get();
		Repository repository = fetchRepository(config, repoId);
		return Transformers.detailedRepositories(inspector).apply(repository);
	}
	
	@POST
	public DetailedRepositoryModel createRepository(RepositoryModel model) throws IOException, ServiceUnavailable, ModificationException {
		Config config = manager.get();
		Repository repository = config.createRepository(model.getName());
		repository.setPermission(fetchUser(config, "git"), Permission.ALL);
		
		// TODO: Give access to current client.
		// TODO: Give access to current user.
		
		manager.apply(config);
		return Transformers.detailedRepositories(inspector).apply(repository);
	}
	
	@DELETE
	@Path("{repoId}")
	public void deleteRepository(@PathParam("repoId") String repoId) throws IOException, ServiceUnavailable, ModificationException {
		Config config = manager.get();
		Repository repository = fetchRepository(config, repoId);
		config.removeRepository(repository);
		manager.apply(config);
	}
	
	@GET
	@Path("{repoId}/diff/{oldId}/{newId}")
	public Collection<Diff> calculateDiff(@PathParam("repoId") String repoId, @PathParam("oldId") String oldId, @PathParam("newId") String newId) throws IOException, ServiceUnavailable, GitException {
		Config config = manager.get();
		Repository repository = fetchRepository(config, repoId);
		return inspector.calculateDiff(repository, oldId, newId);
	}

	private User fetchUser(Config config, String userId) {
		User user = config.getUser(userId);
		if (user == null) {
			throw new NotFoundException("Could not find user: " + userId);
		}
		return user;
	}

	private Repository fetchRepository(Config config, String repoId) {
		Repository repository = config.getRepository(repoId);
		if (repository == null) {
			throw new NotFoundException("Could not find repository: " + repoId);
		}
		return repository;
	}
}
