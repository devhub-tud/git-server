package nl.tudelft.ewi.git.unit;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.backend.JGitRepositoryFacade;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.web.CucumberModule;
import nl.tudelft.ewi.git.web.api.Transformers;
import nl.tudelft.ewi.gitolite.repositories.PathRepositoriesManager;
import nl.tudelft.ewi.gitolite.repositories.PathRepositoriesManager.PathRepositoryImpl;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@RunWith(JukitoRunner.class)
@UseModules(CucumberModule.class)
public class RepositoryFacadeTest {

	@Inject Transformers transformers;
	@Inject PathRepositoriesManager pathRepositoriesManager;
	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

	Git git;
	PathRepositoryImpl pathRepository;
	JGitRepositoryFacade gitRepositoryFacade;

	@Before
	public void setUp() throws GitAPIException, IOException {
		Git.init().setBare(false).setDirectory(temporaryFolder.getRoot()).call();
		pathRepository = pathRepositoriesManager.new PathRepositoryImpl(temporaryFolder.getRoot().toPath());
		gitRepositoryFacade = new JGitRepositoryFacade(transformers, pathRepository);
		git = gitRepositoryFacade.getGit();
	}

	@Test
	public void testGitBlameWithSubmodule() throws Exception {
		addSubmodule();
		RevCommit commit = createCommit();
		String commitId = commit.getName();

		DiffModel diff = gitRepositoryFacade.calculateDiff(null, commitId, 3);

		DiffBlameModel diffBlameModel = gitRepositoryFacade.addBlameData(diff);
		assertEquals(".gitmodules", diffBlameModel.getDiffs().get(0).getNewPath());
	}

	@Test
	public void testGitTreeWithSubmodule() throws Exception {
		addSubmodule();
		RevCommit commit = createCommit();
		String commitId = commit.getName();

		Map<String, EntryType> tree = gitRepositoryFacade.showTree(commitId, "");
		assertThat(tree, equalTo(ImmutableMap.of(".gitmodules", EntryType.TEXT)));
	}

	@Test
	public void testGitBlameWithSymbolicLink() throws Exception {
		File readme = addReadme("README.md");
		addSymbolicLink(readme, "symbolic-link");

		RevCommit commit = createCommit();
		String commitId = commit.getName();

		DiffModel diff = gitRepositoryFacade.calculateDiff(null, commitId, 3);
		gitRepositoryFacade.addBlameData(diff); // Should not fail
	}

	@Test
	public void testGitTreeWithSymbolicLink() throws Exception {
		File readme = addReadme("README.md");
		addSymbolicLink(readme, "symbolic-link");

		RevCommit commit = createCommit();
		String commitId = commit.getName();

		Map<String, EntryType> tree = gitRepositoryFacade.showTree(commitId, "");
		assertThat(tree, equalTo(ImmutableMap.of("README.md", EntryType.TEXT, "symbolic-link", EntryType.TEXT)));
	}

	private File addReadme(String name) throws IOException, GitAPIException {
		File readme = new File(temporaryFolder.getRoot(), name);
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(readme))) {
			bufferedWriter.write("Initial contents\n");
		}
		git.add().addFilepattern(name).call();
		return readme;
	}

	private void addSymbolicLink(File from, String name) throws IOException, GitAPIException {
		Files.createSymbolicLink(new File(temporaryFolder.getRoot(), name).toPath(), from.toPath());
		git.add().addFilepattern(name).call();
	}

	private RevCommit createCommit() throws GitAPIException {
		return git.commit().setMessage("Added submodule").call();
	}

	private void addSubmodule() throws GitAPIException {
		git.submoduleAdd().setURI("https://github.com/octocat/Spoon-Knife.git").setPath("Spoon-Knife").call();
	}
}
