package nl.tudelft.ewi.git.client;

import java.util.Collection;

import nl.tudelft.ewi.git.models.SshKeyModel;
import nl.tudelft.ewi.git.models.UserModel;

public class MockedUserModel extends UserModel {

	private final SshKeysMock sshKeys;
	private final UserModel userModel;
	
	public MockedUserModel(UserModel userModel, SshKeysMock sshKeys) {
		this.setName(userModel.getName());
		this.userModel = userModel;
		this.sshKeys = sshKeys;
	}

	@Override
	public String getPath() {
		return userModel.getPath();
	}

	@Override
	public Collection<SshKeyModel> getKeys() {
		return sshKeys.retrieveAll();
	}

	public SshKeysMock getSshKeys() {
		return sshKeys;
	}

}
