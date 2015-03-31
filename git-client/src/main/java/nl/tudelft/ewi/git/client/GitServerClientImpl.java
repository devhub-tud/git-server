package nl.tudelft.ewi.git.client;

import javax.ws.rs.client.Client;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

/**
 * The {@link GitServerClient} allows you to query and manipulate data from the git-server.
 */
public class GitServerClientImpl implements GitServerClient {

	private final Client client;
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
		this.client = new ResteasyClientBuilder().connectionPoolSize(25).build();
		this.users = new UsersImpl(client, host);
		this.repositories = new RepositoriesImpl(client, host);
		this.groups = new GroupsImpl(client, host);
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
	
	public void close() {
		client.close();
	}

}
