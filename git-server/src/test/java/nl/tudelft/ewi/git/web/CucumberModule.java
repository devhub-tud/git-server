package nl.tudelft.ewi.git.web;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.GitServerModule;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.config.Config;
import nl.tudelft.ewi.gitolite.config.ConfigImpl;
import nl.tudelft.ewi.gitolite.git.GitException;
import nl.tudelft.ewi.gitolite.git.GitManager;
import nl.tudelft.ewi.gitolite.repositories.PathRepositoriesManager;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;
import org.apache.commons.io.FileUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.when;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class CucumberModule extends AbstractModule {

	@Spy private GitManager gitManager = new MockedGitManager();
	@Spy private Config gitoliteConfig = new ConfigImpl();
	@InjectMocks private ManagedConfig managedConfig;
	@Mock private nl.tudelft.ewi.git.Config configuration;

	private File adminFolder;
	private File mirrorsFolder;
	private File repositoriesFolder;
	private RepositoriesManager repositoriesManager;

	@Override
	protected void configure() {
		MockitoAnnotations.initMocks(this);
		createMockedMirrorsFolder();
		createMockedRepositoriesFolder();
		createMockedGitoliteManagerRepo();
		repositoriesManager = new PathRepositoriesManager(repositoriesFolder);

		install(new GitServerModule(managedConfig, repositoriesManager, configuration));
		bind(GitManager.class).annotatedWith(MockedSingleton.class).toInstance(gitManager);
		bind(Config.class).annotatedWith(MockedSingleton.class).toInstance(gitoliteConfig);
		bind(File.class).annotatedWith(Names.named("repositories.folder")).toInstance(repositoriesFolder);
		Runtime.getRuntime().addShutdownHook(new Thread(this::removeFolders));
	}

	protected void createMockedMirrorsFolder() {
		mirrorsFolder = Files.createTempDir();
		String mirrorsPath = mirrorsFolder.toPath().toString() + "/";
		when(configuration.getMirrorsDirectory()).thenReturn(mirrorsFolder);
		log.info("Initialized mirrors folder in {}", mirrorsPath);
	}

	protected void createMockedRepositoriesFolder() {
		repositoriesFolder = Files.createTempDir();
		String repositoriesPath = repositoriesFolder.toPath().toString() + "/";
		when(configuration.getGitoliteBaseUrl()).thenReturn(repositoriesPath);
		when(configuration.getRepositoriesDirectory()).thenReturn(repositoriesFolder);
		log.info("Initialized bare repository folder in {}", repositoriesPath);
	}

	@SneakyThrows
	protected void createMockedGitoliteManagerRepo() {
		adminFolder = Files.createTempDir();
		File conf = new File(adminFolder, "conf");
		File config = new File(conf, "gitolite.conf");
		Files.createParentDirs(config);
	}

	@SneakyThrows
	private void removeFolders() {
		FileUtils.deleteDirectory(repositoriesFolder);
		FileUtils.deleteDirectory(adminFolder);
		FileUtils.deleteDirectory(mirrorsFolder);
	}

	/*
	 * Instead of stubbing the admin folder, we spy a custom implementation, so
	 * users can still reset the mock.
	 */
	public class MockedGitManager implements GitManager {

		@Override
		public File getWorkingDirectory() {
			return adminFolder;
		}

		@Override public boolean exists(){ return true; }
		@Override public void open() {}
		@Override public void remove(String filePattern) throws IOException, GitException, InterruptedException {}
		@Override public void clone(String uri) throws IOException, InterruptedException, GitException { }
		@Override public void init() throws IOException, InterruptedException, GitException { }
		@Override public boolean pull() throws IOException, InterruptedException, GitException {return false; }
		@Override public void commitChanges() throws IOException, InterruptedException, IOException, GitException {}
		@Override public void push() throws IOException, InterruptedException, GitException {}

	}

}
