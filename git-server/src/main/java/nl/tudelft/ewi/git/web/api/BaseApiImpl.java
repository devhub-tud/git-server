package nl.tudelft.ewi.git.web.api;

import lombok.Getter;
import lombok.experimental.Accessors;
import nl.tudelft.ewi.git.models.Version;
import nl.tudelft.ewi.gitolite.config.Config;

import javax.inject.Inject;

/**
 * Implementation for {@link BaseApi}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Accessors(fluent = true)
public class BaseApiImpl implements BaseApi {

	@Inject @Getter private GroupsApi groups;
	@Inject @Getter private UsersApi users;
	@Inject @Getter private RepositoriesApi repositories;

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
