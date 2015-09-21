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
import nl.tudelft.ewi.gitolite.keystore.Key;
import nl.tudelft.ewi.gitolite.keystore.KeyStore;
import nl.tudelft.ewi.gitolite.keystore.KeyStoreImpl;
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
 * Mock out the Gitolite manager components, so we are not dependent on a Gitolite installation.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class CucumberModule extends AbstractModule {

	private File adminFolder = Files.createTempDir();
	private File configFolder = ensureExists(new File(adminFolder, "conf"));
	private File keyDir = ensureExists(new File(adminFolder, "keydir"));
	private File mirrorsFolder = Files.createTempDir();
	private File repositoriesFolder = Files.createTempDir();

	@Spy private KeyStore keyStore = new KeyStoreImpl(keyDir);
	@Spy private GitManager gitManager = new MockedGitManager();
	@Spy private Config gitoliteConfig = new ConfigImpl();
	@InjectMocks private ManagedConfig managedConfig;
	@Mock private nl.tudelft.ewi.git.Config configuration;

	private RepositoriesManager repositoriesManager;

	@Override
	protected void configure() {
		MockitoAnnotations.initMocks(this);
		createMockedMirrorsFolder();
		createMockedRepositoriesFolder();
		createMockedGitoliteManagerRepo();
		repositoriesManager = new PathRepositoriesManager(repositoriesFolder);

		bind(RepositoriesManager.class).toInstance(repositoriesManager);
		bind(ManagedConfig.class).toInstance(managedConfig);
		bind(nl.tudelft.ewi.git.Config.class).toInstance(configuration);

		// Bind GitManager and Config spies so tests can verify on them
		bind(GitManager.class).annotatedWith(MockedSingleton.class).toInstance(gitManager);
		bind(Config.class).annotatedWith(MockedSingleton.class).toInstance(gitoliteConfig);
		bind(KeyStore.class).annotatedWith(MockedSingleton.class).toInstance(keyStore);
		// Bind folders so tests can prepare them
		bind(File.class).annotatedWith(Names.named("mirrors.folder")).toInstance(mirrorsFolder);
		bind(File.class).annotatedWith(Names.named("repositories.folder")).toInstance(repositoriesFolder);
		// Clean up folders on shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(this::removeFolders));
	}

	protected void createMockedMirrorsFolder() {
		String mirrorsPath = mirrorsFolder.toPath().toString() + "/";
		when(configuration.getMirrorsDirectory()).thenReturn(mirrorsFolder);
		log.info("Initialized mirrors folder in {}", mirrorsPath);
	}

	protected void createMockedRepositoriesFolder() {
		String repositoriesPath = repositoriesFolder.toPath().toString() + "/";
		when(configuration.getGitoliteBaseUrl()).thenReturn(repositoriesPath);
		when(configuration.getRepositoriesDirectory()).thenReturn(repositoriesFolder);
		log.info("Initialized bare repository folder in {}", repositoriesPath);
	}

	@SneakyThrows
	protected void createMockedGitoliteManagerRepo() {
		File config = new File(configFolder, "gitolite.conf");
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

	@SneakyThrows
	static File ensureExists(File file) {
		FileUtils.forceMkdir(file);
		return file;
	}

}
