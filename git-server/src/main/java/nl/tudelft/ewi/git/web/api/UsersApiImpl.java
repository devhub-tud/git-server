package nl.tudelft.ewi.git.web.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import nl.tudelft.ewi.git.models.IdentifiableModel;
import nl.tudelft.ewi.git.models.SshKeyModel;
import nl.tudelft.ewi.git.models.UserModel;
import nl.tudelft.ewi.git.web.exceptions.IllegalArgumentExceptionMapper;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.keystore.KeyHolder;
import nl.tudelft.ewi.gitolite.keystore.PersistedKey;
import nl.tudelft.ewi.gitolite.objects.Identifier;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class UsersApiImpl implements UsersApi {

	private final ManagedConfig managedConfig;
	@Context private ResourceContext resourceContext;

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
		UserApi userApi = new UserApiImpl(username);
		if (resourceContext != null) {
			return resourceContext.initResource(userApi);
		}
		return userApi;
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
				managedConfig.writeConfig(config -> config.deleteIdentifierUses(new Identifier(username)));
			});
		}

		@Override
		public KeysApi keys() {
			KeyApiImpl keyApi = new KeyApiImpl();
			if (resourceContext != null) {
				return resourceContext.initResource(keyApi);
			}
			return keyApi;
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
				return managedConfig.readKeyStore(keyStore -> {
					try {

						return Transformers.transformSshKey(keyStore.getKey(username, keyId));
					}
					catch (NoSuchElementException e) {
						throw new NotFoundException(
							String.format("Failed to delete key %s for user %s: %s", keyId, username, e.getMessage()),
							e
						);
					}
					catch (RuntimeException e) {
						throw new InternalServerErrorException(
							String.format("Failed to delete key %s for user %s: %s", keyId, username, e.getMessage()),
							e
						);
					}
				});
			}

			@Override
			@SneakyThrows
			public SshKeyModel addNewKey(@Valid SshKeyModel sshKeyModel) {
				KeyHolder key = new KeyHolder(username, sshKeyModel.getName(), sshKeyModel.getContents());
				return managedConfig.writeKeyStoreWithReturn(keyStore -> {
					try {
						return Transformers.transformSshKey(keyStore.put(key));
					}
					catch (IllegalArgumentException e) {
						throw new BadRequestException(
							String.format("Failed to delete key %s for user %s: %s", sshKeyModel.getName(), username, e.getMessage()),
							e
						);
					}
					catch (RuntimeException e) {
						throw new InternalServerErrorException(
							String.format("Failed to delete key %s for user %s: %s", sshKeyModel.getName(), username, e.getMessage()),
							e
						);
					}
				});
			}

			@Override
			@SneakyThrows
			public void deleteSshKey(@NotNull String keyId) {
				managedConfig.writeKeyStore(keyStore -> {
					try {
						keyStore.getKey(username, keyId).delete();
					}
					catch (NoSuchElementException e) {
						throw new NotFoundException(
							String.format("Failed to delete key %s for user %s: %s", keyId, username, e.getMessage()),
							e
						);
					}
					catch (RuntimeException e) {
						throw new InternalServerErrorException(
							String.format("Failed to delete key %s for user %s: %s", keyId, username, e.getMessage()),
							e
						);
					}
				});
			}

		}

	}

}
