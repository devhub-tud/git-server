package nl.tudelft.ewi.git.client;

/**
 * The {@link GitServerClient} allows you to query and manipulate data from the git-server.
 */
public class GitServerClientImpl implements GitServerClient {

	private final Users users;
	private final Repositories repositories;
	private final Groups groups;

	/**
	 * Creates a new {@link GitServerClient} instance.
	 * 
	 * @param host
	 *            The hostname of the git-server.
	 */
	public GitServerClientImpl(String host) {
		this.users = new UsersImpl(host);
		this.repositories = new RepositoriesImpl(host);
		this.groups = new GroupsImpl(host);
	}

	@Override
	public Repositories repositories() {
		return repositories;
	}

	@Override
	public Users users() {
		return users;
	}
	
	@Override
	public Groups groups() {
		return groups;
	}

}
