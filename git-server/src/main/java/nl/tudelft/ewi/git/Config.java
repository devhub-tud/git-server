package nl.tudelft.ewi.git;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Config {

	private final Properties properties;
	
	public Config() {
		this.properties = new Properties();
		reload();
	}
	
	public void reload() {
		try (InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/config.properties"))) {
			properties.load(reader);
		}
		catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	public int getHttpPort() {
		return Integer.parseInt(properties.getProperty("http.port", "8080"));
	}
	
	public String getGitoliteBaseUrl() {
		return properties.getProperty("gitolite.base-url");
	}
	
	public String getGitoliteRepoUrl() {
		return properties.getProperty("gitolite.repo-url");
	}

	public String getPassphrase() {
		return properties.getProperty("gitolite.passphrase", null);
	}

	public File getRepositoriesDirectory() {
		String path = properties.getProperty("gitolite.repositories");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(path));
		return new File(path);
	}

	public File getMirrorsDirectory() {
		String path = properties.getProperty("gitolite.mirrors");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(path));
		return new File(path);
	}

	public String getGitoliteAdmin() {
		return properties.getProperty("gitolite.administrator", "git");
	}
}
