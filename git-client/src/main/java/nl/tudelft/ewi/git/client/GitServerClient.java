package nl.tudelft.ewi.git.client;



public class GitServerClient {
	
	private final Users users;
	private final Repositories repositories;
	private final Groups groups;

	public GitServerClient(String host) {
		this.users = new Users(host);
		this.repositories = new Repositories(host);
		this.groups = new Groups(host);
	}
	
	public Repositories repositories() {
		return repositories;
	}
	
	public Users users() {
		return users;
	}

	public Groups groups() {
		return groups;
	}
	
}
