package nl.tudelft.ewi.git.client;

/**
 * The {@link GitServerClient} allows you to query and manipulate data from the git-server.
 */
public interface GitServerClient {
	
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
	
}
