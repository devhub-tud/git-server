package nl.tudelft.ewi.git.web;

import nl.minicom.gitolite.manager.git.GitManager;
import nl.tudelft.ewi.git.models.Version;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Path("api/version")
@Produces(MediaType.APPLICATION_JSON)
public class VersionApi {

    /**
     * @return the GitServer version
     */
    @GET
    public Version getVersion() {
        Package gitoliteManagerPackage = GitManager.class.getPackage();
        Package gitServerPackage = VersionApi.class.getPackage();

        Version version = new Version();
        version.setGitoliteAdminVersion(gitoliteManagerPackage.getImplementationVersion());
        version.setGitServerVersion(gitServerPackage.getImplementationVersion());
        return version;
    }

}
