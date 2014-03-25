package nl.tudelft.ewi.git.web;

import java.net.URLDecoder;

import javax.ws.rs.NotFoundException;

import lombok.SneakyThrows;
import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.Group;
import nl.minicom.gitolite.manager.models.Repository;
import nl.minicom.gitolite.manager.models.User;

abstract class BaseApi {

	User fetchUser(Config config, String userId) {
		User user = config.getUser(userId);
		if (user == null) {
			throw new NotFoundException("Could not find user: " + userId);
		}
		return user;
	}
	
	Group fetchGroup(Config config, String groupId) {
		Group group = config.getGroup(groupId);
		if (group == null) {
			throw new NotFoundException("Could not find group: " + groupId);
		}
		return group;
	}

	Repository fetchRepository(Config config, String repoId) {
		Repository repository = config.getRepository(repoId);
		if (repository == null) {
			throw new NotFoundException("Could not find repository: " + repoId);
		}
		return repository;
	}
	
	@SneakyThrows
	String decode(String value) {
		return URLDecoder.decode(value, "UTF-8");
	}

}
