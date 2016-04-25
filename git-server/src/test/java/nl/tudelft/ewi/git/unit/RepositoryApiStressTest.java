package nl.tudelft.ewi.git.unit;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import nl.tudelft.ewi.git.backend.JGitRepositoryFacade;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;
import nl.tudelft.ewi.git.web.CucumberModule;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@RunWith(JukitoRunner.class)
@UseModules(CucumberModule.class)
public class RepositoryApiStressTest {

	@Inject Transformers transformers;
	@Inject PathRepositoriesManager pathRepositoriesManager;
	@Inject RepositoriesApi repositoriesApi;
	DetailedRepositoryModel detailedRepositoryModel;

	@Before
	public void setUp() throws GitAPIException, IOException {
		val crm = new CreateRepositoryModel();
		crm.setTemplateRepository("https://github.com/SERG-Delft/jpacman-template.git");
		crm.setName("stress-test");
		crm.setPermissions(ImmutableMap.of("me", Level.ADMIN));
		detailedRepositoryModel = repositoriesApi.createRepository(crm);
	}

	@Test
	public void testGitBlameWithSubmodule() throws Exception {
		System.out.println(detailedRepositoryModel);
	}


}
