package nl.tudelft.ewi.git.integration;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffContext;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffFile;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffLine;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.models.Version;
import nl.tudelft.ewi.git.web.api.BaseApi;
import nl.tudelft.ewi.git.web.api.GroupsApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.UsersApi;
import nl.tudelft.ewi.gitolite.permission.Permission;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;
import nl.tudelft.ewi.gitolite.repositories.RepositoryNotFoundException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class WebTest {

	private static final String REPOSITORY_NAME = "test";
	private static final String AUTHOR_NAME = "Jan-Willem";
	private static final String AUTHOR_EMAIL = "jan-willem@devhub";
	private static final String README_MD_FILE = "README.md";
	private static final String INITIAL_COMMIT_MESSAGE = "Initial commit";
	private static final String README_MD_CONTENTS = "This is a text file.";
	private static final String REF_MASTER = "master";

	@ClassRule public static GitServerTestRule gitServerTestRule = new GitServerTestRule();
	@Inject @Named("repositories.folder") private File repositoriesFolder;
	@Inject @Named("mirrors.folder") private File mirrorsFolder;
	@Inject private RepositoriesManager repositoriesManager;

	@Before
	public void setupRepository() throws GitAPIException, IOException {
		gitServerTestRule.getGitServer().getInjector().injectMembers(this);
		try {
			repositoriesManager.getRepository(URI.create(REPOSITORY_NAME + ".git/"));
		}
		catch (RepositoryNotFoundException e) {
			prepareBareRepository(REPOSITORY_NAME);
		}
	}

	BaseApi baseApi;
	RepositoriesApi repositoriesApi;
	GroupsApi groupsApi;
	UsersApi usersApi;

	@Before
	public void setUp() {
		ResteasyClient resteasyClient = new ResteasyClientBuilder().build();
		baseApi = resteasyClient.target("http://localhost:8081").proxy(BaseApi.class);
		usersApi = baseApi.users();
		groupsApi = baseApi.groups();
		repositoriesApi = baseApi.repositories();
	}

	@Test
	public void testVersionResource() {
		Version version = baseApi.version();
		assertNotNull(version);
	}

	@Test
	public void testListRepositories() {
		Collection<RepositoryModel> repositories = repositoriesApi.listAllRepositories();

		assertThat(repositories, contains(allOf(
			featureMatcher("name", RepositoryModel::getName, equalTo(REPOSITORY_NAME)),
			featureMatcher("permissions", RepositoryModel::getPermissions, equalTo(ImmutableMap.of()))
		)));
	}

	@Test
	public void testGetRepository() throws InterruptedException {
		DetailedRepositoryModel repositoryModel = repositoriesApi.getRepository(REPOSITORY_NAME).getRepositoryModel();

		assertThat(repositoryModel.getName(), equalTo(REPOSITORY_NAME));
		assertThat(repositoryModel.getBranches(), contains(masterBranchMatcher()));
	}

	@Test
	public void testListCommits() {
		Collection<CommitModel> commits = repositoriesApi.getRepository(REPOSITORY_NAME).listCommits();

		assertThat(commits, contains(initialCommitMatcher()));
	}

	@Test
	public void testListBranches() {
		Collection<BranchModel> branches = repositoriesApi.getRepository(REPOSITORY_NAME).getBranches();

		assertThat(branches, contains(masterBranchMatcher()));
	}

	@Test
	public void testGetBranch() {
		BranchModel branch = repositoriesApi.getRepository(REPOSITORY_NAME).getBranch(REF_MASTER).get();

		assertThat(branch, masterBranchMatcher());
	}

	@Test
	public void testGetBranchDiff() throws InterruptedException {
		DiffModel diffModel = repositoriesApi.getRepository(REPOSITORY_NAME)
			.getBranch(REF_MASTER)
			.getCommit()
			.diff();

		assertThat(diffModel.getDiffs(), contains(diffFileMatcher()));
	}

	@Test
	public void testGetBranchDiffBlame() throws InterruptedException {
		DiffBlameModel diffModel = repositoriesApi.getRepository(REPOSITORY_NAME)
			.getBranch(REF_MASTER)
			.getCommit()
			.diffBlame();

		assertThat(diffModel.getDiffs(), contains(diffFileMatcher()));
	}

	@Test
	public void testTree() {
		Map<String, EntryType> tree = repositoriesApi.getRepository(REPOSITORY_NAME)
			.getBranch(REF_MASTER)
			.getCommit()
			.showTree();

		assertThat(tree, equalTo(ImmutableMap.of(README_MD_FILE, EntryType.TEXT)));
	}

	@Test
	public void readTextFile() throws IOException {
		String contents = repositoriesApi.getRepository(REPOSITORY_NAME)
			.getBranch(REF_MASTER)
			.getCommit()
			.showTextFile(README_MD_FILE);

		assertThat(contents, equalTo(README_MD_CONTENTS));
	}

	@Test
	public void assertCommitResponseEqual() {
		DetailedCommitModel branchCommit = repositoriesApi.getRepository(REPOSITORY_NAME)
			.getBranch(REF_MASTER)
			.getCommit()
			.get();

		DetailedCommitModel actualCommit = repositoriesApi.getRepository(REPOSITORY_NAME)
			.getCommit(branchCommit.getCommit())
			.get();

		assertThat(actualCommit, equalTo(branchCommit));
	}

	static Matcher<DiffFile<? extends DiffContext<? extends DiffLine>>> diffFileMatcher() {
		return featureMatcher("", DiffFile::getContexts, contains(diffContextMatcher()));
	}

	static Matcher<DiffContext<? extends DiffLine>> diffContextMatcher() {
		return featureMatcher("", DiffContext::getLines, contains(
			diffLineMatcher(null, 1, README_MD_CONTENTS)
		));
	}

	static Matcher<DiffLine> diffLineMatcher(Integer oldLineNumber, Integer newLineNumber, String content) {
		return allOf(
			featureMatcher("old", DiffLine::getOldLineNumber, equalTo(oldLineNumber)),
			featureMatcher("new", DiffLine::getNewLineNumber, equalTo(newLineNumber)),
			featureMatcher("content", DiffLine::getContent, equalTo(content))
		);
	}

	static Matcher<BranchModel> masterBranchMatcher() {
		return allOf(
			featureMatcher("name", BranchModel::getName, containsString(REF_MASTER)),
			featureMatcher("behind", BranchModel::getBehind, equalTo(0)),
			featureMatcher("ahead", BranchModel::getAhead, equalTo(0)),
			featureMatcher("commit", BranchModel::getCommit, initialCommitMatcher())
		);
	}

	static Matcher<CommitModel> initialCommitMatcher() {
		return allOf(
			featureMatcher("commitId", CommitModel::getCommit, Matchers.notNullValue()),
			featureMatcher("author", CommitModel::getAuthor, equalTo(String.format("%s <%s>", AUTHOR_NAME, AUTHOR_EMAIL))),
			featureMatcher("message", CommitModel::getMessage, equalTo(INITIAL_COMMIT_MESSAGE)),
			featureMatcher("parents",CommitModel::getParents, emptyArray())
		);
	}

	static <T, V> FeatureMatcher<V, T> featureMatcher(String name, Function<V, ? extends T> supplier, Matcher<T> matcher) {
		return new FeatureMatcher<V, T>(matcher, name, name) {
			@Override
			protected T featureValueOf(V v) {
				return supplier.apply(v);
			}
		};
	}

	// Simulate the gitolite server that creates a bare repository if not exists....
	private void prepareBareRepository(String name) throws GitAPIException, IOException {
		File repositoryFolder = new File(repositoriesFolder, name + ".git");

		Git.init()
			.setBare(true)
			.setDirectory(repositoryFolder)
			.call();

		File mirrorFolder = new File(mirrorsFolder, name);
		Git mirror = Git.init()
			.setDirectory(mirrorFolder)
			.call();

		IOUtils.copy(
			WebTest.class.getResourceAsStream("/template-files/README.md"),
			new FileOutputStream(new File(mirrorFolder, README_MD_FILE))
		);

		mirror.add().addFilepattern(README_MD_FILE).call();

		mirror.commit()
			.setAuthor(AUTHOR_NAME, AUTHOR_EMAIL)
			.setCommitter(AUTHOR_NAME, AUTHOR_EMAIL)
			.setMessage(INITIAL_COMMIT_MESSAGE)
			.call();

		mirror.push().setRemote(repositoryFolder.getPath()).call();
		repositoriesManager.reload();
	}

}
