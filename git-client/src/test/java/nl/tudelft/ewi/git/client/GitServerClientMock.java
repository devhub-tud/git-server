package nl.tudelft.ewi.git.client;

public class GitServerClientMock implements GitServerClient {
	
	private final RepositoriesMock repositories;
	private final UsersMock users;
	private final GroupsMock groups;
	
	public GitServerClientMock() {
		this.repositories = new RepositoriesMock();
		this.users = new UsersMock();
		this.groups = new GroupsMock();
	}

	@Override
	public RepositoriesMock repositories() {
		return repositories;
	}

	@Override
	public UsersMock users() {
		return users;
	}

	@Override
	public GroupsMock groups() {
		return groups;
	}

}
