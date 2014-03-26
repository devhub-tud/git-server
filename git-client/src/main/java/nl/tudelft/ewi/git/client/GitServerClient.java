package nl.tudelft.ewi.git.client;

/**
 * The {@link GitServerClient} allows you to query and manipulate data from the git-server.
 */
public class GitServerClient {

	private final Users users;
	private final Repositories repositories;
	private final Groups groups;

	/**
	 * Creates a new {@link GitServerClient} instance.
	 * 
	 * @param host
	 *            The hostname of the git-server.
	 */
	public GitServerClient(String host) {
		this.users = new Users(host);
		this.repositories = new Repositories(host);
		this.groups = new Groups(host);
	}

	/**
	 * @return the {@link Repositories} interface which lets you query and manipulate data related
	 *         to repositories.
	 */
	public Repositories repositories() {
		return repositories;
	}

	/**
	 * @return the {@link Users} interface which lets you query and manipulate data related to
	 *         users.
	 */
	public Users users() {
		return users;
	}

	/**
	 * @return the {@link Groups} interface which lets you query and manipulate data related to
	 *         groups.
	 */
	public Groups groups() {
		return groups;
	}

}
