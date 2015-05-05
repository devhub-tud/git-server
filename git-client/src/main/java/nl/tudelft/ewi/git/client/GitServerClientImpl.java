package nl.tudelft.ewi.git.client;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;

import nl.tudelft.ewi.git.models.Version;
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
	@Inject
	public GitServerClientImpl(@Named("git.server.host") String host) {
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

	@Override
	public Version version() {
		return this.client.target("api")
			.path("version")
			.request()
			.get(Version.class);
	}

	public void close() {
		client.close();
	}

}
