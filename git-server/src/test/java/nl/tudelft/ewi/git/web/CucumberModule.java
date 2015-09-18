package nl.tudelft.ewi.git.web;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.GitServerModule;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.config.Config;
import nl.tudelft.ewi.gitolite.config.ConfigImpl;
import nl.tudelft.ewi.gitolite.git.GitManager;
import nl.tudelft.ewi.gitolite.repositories.PathRepositoriesManager;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;
import org.apache.commons.io.FileUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.File;

import static org.mockito.Mockito.when;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class CucumberModule extends AbstractModule {

	@Mock private GitManager gitManager;
	@Spy private Config gitoliteConfig = new ConfigImpl();
	@InjectMocks private ManagedConfig managedConfig;
	@Mock private nl.tudelft.ewi.git.Config configuration;

	private File confFolder;
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
		confFolder = Files.createTempDir();
		FileUtils.forceMkdir(new File(confFolder, "conf"));
		when(gitManager.getWorkingDirectory()).thenReturn(confFolder);
	}

	@SneakyThrows
	private void removeFolders() {
		FileUtils.deleteDirectory(repositoriesFolder);
		FileUtils.deleteDirectory(confFolder);
		FileUtils.deleteDirectory(mirrorsFolder);
	}

}
