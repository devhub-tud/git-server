package nl.tudelft.ewi.git.web.api;

import lombok.SneakyThrows;
import nl.tudelft.ewi.git.models.Version;
import org.jboss.resteasy.plugins.guice.RequestScoped;

import javax.inject.Inject;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import java.io.InputStream;
import java.util.Properties;

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
	@SneakyThrows
	public Version version() {
		Properties properties = new Properties();
		try (InputStream inputStream = BaseApiImpl.class.getResourceAsStream("/git-server.git.properties")) {
			properties.load(inputStream);
		}

		Version version = new Version();
		version.setGitServerCommit(properties.getProperty("git.commit.id"));
		version.setGitServerVersion(properties.getProperty("git.build.version"));
		return version;
	}

}
