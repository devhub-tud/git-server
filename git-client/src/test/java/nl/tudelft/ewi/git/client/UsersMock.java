package nl.tudelft.ewi.git.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;

import com.google.common.collect.Lists;

import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.git.models.UserModel;

public class UsersMock implements Users {
	
	private final Map<String, MockedUserModel> users = new HashMap<>();

	@Override
	public List<IdentifiableModel> retrieveAll() {
		return Lists.<IdentifiableModel> newArrayList(users.values());
	}

	@Override
	public MockedUserModel retrieve(String userName) {
		MockedUserModel user = users.get(userName);
		
		if(user==null) {
			throw new NotFoundException("User not found: " + userName);
		}
		
		return user;
	}

	@Override
	public MockedUserModel retrieve(UserModel model) {
		return retrieve(model.getName());
	}

	@Override
	public MockedUserModel create(UserModel newUser) {
		String username = newUser.getName();
		if(users.containsKey(username)) {
			throw new RuntimeException("User already exists " + newUser);
		} else {
			MockedUserModel decoratedUser = new MockedUserModel(newUser, new SshKeysMock());
			users.put(newUser.getName(), decoratedUser);
			return decoratedUser;
		}
	}

	@Override
	public UserModel ensureExists(String name) {
		UserModel model = new UserModel();
		model.setName(name);
		return ensureExists(model);
	}

	@Override
	public MockedUserModel ensureExists(UserModel model) {
		try {
			return retrieve(model.getName());
		} catch (NotFoundException e) {
			return create(model);
		}
	}

	@Override
	public void delete(IdentifiableModel user) {
		// Ensure that the repository exists, or throw an exception
		retrieve(user.getName());
		users.remove(user.getName());
	}

	@Override
	public SshKeysMock sshKeys(UserModel user) {
		return retrieve(user).getSshKeys();
	}

}
