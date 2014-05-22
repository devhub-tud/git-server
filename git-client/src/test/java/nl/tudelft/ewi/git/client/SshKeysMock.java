package nl.tudelft.ewi.git.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;

import com.google.common.collect.Lists;

import nl.tudelft.ewi.git.models.SshKeyModel;

public class SshKeysMock implements SshKeys {
	
	private final Map<String, SshKeyModel> keys = new HashMap<>();

	@Override
	public List<SshKeyModel> retrieveAll() {
		return Lists.newArrayList(keys.values());
	}

	@Override
	public SshKeyModel retrieve(String keyName) {
		SshKeyModel key = keys.get(keyName);
		
		if(key==null) {
			throw new NotFoundException("Key not found: " + keyName);
		}
		
		return key;
	}

	@Override
	public SshKeyModel registerSshKey(SshKeyModel sshKey) {
		keys.put(sshKey.getName(), sshKey);
		return sshKey;
		
	}

	@Override
	public void deleteSshKey(SshKeyModel sshKey) {
		keys.remove(sshKey.getName());
	}

}
