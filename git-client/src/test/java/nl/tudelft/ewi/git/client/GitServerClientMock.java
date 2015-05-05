package nl.tudelft.ewi.git.client;

import nl.tudelft.ewi.git.models.Version;

import javax.inject.Inject;

public class GitServerClientMock implements GitServerClient {
	
	private final RepositoriesMock repositories;
	private final UsersMock users;
	private final GroupsMock groups;

	@Inject
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

	@Override
	public Version version() {
		Version version = new Version();
		Package gitServerPackage = GitServerClientMock.class.getPackage();
		version.setGitServerVersion(gitServerPackage.getImplementationVersion());
		return version;
	}

	@Override
	public void close() throws Exception {
		// no-op
	}
}
