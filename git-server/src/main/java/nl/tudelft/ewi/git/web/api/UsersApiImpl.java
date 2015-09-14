package nl.tudelft.ewi.git.web.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.git.models.SshKeyModel;
import nl.tudelft.ewi.git.models.UserModel;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.keystore.KeyHolder;
import nl.tudelft.ewi.gitolite.keystore.PersistedKey;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class UsersApiImpl implements UsersApi {

	private final ManagedConfig managedConfig;

	@Inject
	public UsersApiImpl(ManagedConfig managedConfig) {
		this.managedConfig = managedConfig;
	}

	@Override
	public Collection<IdentifiableModel> listAllUsers() {
		return managedConfig.readKeyStore(keyStore ->
			keyStore.getUsers().stream()
				.map(IdentifiableModel::new)
				.collect(Collectors.toList()));
	}

	@Override
	public UserApi getUser(@NotNull String username) {
		return new UserApiImpl(username);
	}

	@Override
	@Deprecated
	public UserModel createNewUser(@Valid UserModel model) {
		throw new UnsupportedOperationException();
	}

	public class UserApiImpl implements UserApi {

		private final String username;

		public UserApiImpl(String username) {
			this.username = username;
		}

		@Override
		public UserModel get() {
			return managedConfig.readKeyStore(keyStore -> {
				Collection<SshKeyModel> keys = keyStore.getKeys(username).stream()
					.map(Transformers::transformSshKey)
					.collect(Collectors.toList());
				UserModel userModel = new UserModel();
				userModel.setName(username);
				userModel.setKeys(keys);
				return userModel;
			});
		}

		@Override
		@SneakyThrows
		public void deleteUser() {
			managedConfig.writeKeyStore(keyStore -> {
				for (PersistedKey key : ImmutableList.copyOf(keyStore.getKeys(username))) {
					key.delete();
				}
			});
		}

		@Override
		public KeysApi keys() {
			return new KeyApiImpl();
		}

		public class KeyApiImpl implements KeysApi {

			@Override
			public Collection<SshKeyModel> listSshKeys() {
				return managedConfig.readKeyStore(keyStore ->
						keyStore.getKeys(username).stream()
							.map(Transformers::transformSshKey)
							.collect(Collectors.toList())
				);
			}

			@Override
			public SshKeyModel retrieveSshKey(@NotNull String keyId) {
				return managedConfig.readKeyStore(keyStore ->
					Transformers.transformSshKey(keyStore.getKey(username, keyId)));
			}

			@Override
			@SneakyThrows
			public SshKeyModel addNewKey(@Valid SshKeyModel sshKeyModel) {
				KeyHolder key = new KeyHolder(username, sshKeyModel.getName(), sshKeyModel.getContents());
				return managedConfig.writeKeyStoreWithReturn(keyStore ->
					Transformers.transformSshKey(keyStore.put(key)));
			}

			@Override
			@SneakyThrows
			public void deleteSshKey(@NotNull String keyId) {
				managedConfig.writeKeyStore(keyStore ->
					keyStore.getKey(username, keyId).delete());
			}

		}

	}

}
