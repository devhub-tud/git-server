package nl.tudelft.ewi.git.client;

public class GitServerClientMock implements GitServerClient {
	
	private final Repositories repositories;
	private final Users users;
	private final Groups groups;
	
	public GitServerClientMock() {
		this(new RepositoriesMock(), new UsersMock(), new GroupsMock());
	}
	
	public GitServerClientMock(Repositories repositories, Users users,
			Groups groups) {
		this.repositories = repositories;
		this.users = users;
		this.groups = groups;
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
