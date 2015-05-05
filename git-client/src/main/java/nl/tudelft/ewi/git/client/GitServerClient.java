package nl.tudelft.ewi.git.client;

import nl.tudelft.ewi.git.models.Version;

/**
 * The {@link GitServerClient} allows you to query and manipulate data from the git-server.
 */
public interface GitServerClient extends AutoCloseable {
	
	/**
	 * @return the {@link Repositories} interface which lets you query and manipulate data related
	 *         to repositories.
	 */
	Repositories repositories();

	/**
	 * @return the {@link Users} interface which lets you query and manipulate data related to
	 *         users.
	 */
	Users users();

	/**
	 * @return the {@link Groups} interface which lets you query and manipulate data related to
	 *         groups.
	 */
	Groups groups();

	/**
	 * @return the {@link Version} for the Git server
	 */
	Version version();
	
}
