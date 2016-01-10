package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.models.Version;
import nl.tudelft.ewi.gitolite.config.Config;
import org.jboss.resteasy.plugins.guice.RequestScoped;

import javax.inject.Inject;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;

/**
 * Implementation for {@link BaseApi}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@RequestScoped
public class BaseApiImpl implements BaseApi {

	@Inject @Context private ResourceContext resourceContext;
	@Inject private GroupsApi groups;
	@Inject private UsersApi users;
	@Inject private RepositoriesApi repositories;

	@Override
	public GroupsApi groups() {
		return resourceContext.initResource(groups);
	}

	@Override
	public UsersApi users() {
		return resourceContext.initResource(users);
	}

	@Override
	public RepositoriesApi repositories() {
		return resourceContext.initResource(repositories);
	}

	@Override
	public Version version() {
		Package gitoliteManagerPackage = Config.class.getPackage();
		Package gitServerPackage = BaseApiImpl.class.getPackage();

		Version version = new Version();
		version.setGitoliteAdminVersion(gitoliteManagerPackage.getImplementationVersion());
		version.setGitServerVersion(gitServerPackage.getImplementationVersion());
		return version;
	}

}
