package nl.tudelft.ewi.git.integration;

import lombok.Getter;
import lombok.SneakyThrows;
import nl.tudelft.ewi.git.Config;
import nl.tudelft.ewi.git.GitServer;
import nl.tudelft.ewi.git.web.CucumberModule;
import org.junit.rules.ExternalResource;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class GitServerTestRule extends ExternalResource {

	@Getter private GitServer gitServer;

	@Override
	protected void before() throws Throwable {
		Config config = new Config();
		config.reload();

		gitServer = new GitServer(config, new CucumberModule());
		gitServer.start();
	}

	@Override
	@SneakyThrows
	protected void after() {
		gitServer.stop();
	}

}
