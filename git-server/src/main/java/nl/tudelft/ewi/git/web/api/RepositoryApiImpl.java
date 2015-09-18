package nl.tudelft.ewi.git.web.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.SneakyThrows;
import nl.tudelft.ewi.git.backend.RepositoryFacade;
import nl.tudelft.ewi.git.backend.JGitRepositoryFacade;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.models.TagModel;
import nl.tudelft.ewi.git.web.api.di.BranchApiFactory;
import nl.tudelft.ewi.git.web.api.di.CommitApiFactory;
import nl.tudelft.ewi.git.web.security.RequireAuthentication;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.git.GitException;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.parser.rules.AccessRule;
import nl.tudelft.ewi.gitolite.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.permission.Permission;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;

import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class RepositoryApiImpl extends AbstractRepositoryApi implements RepositoryApi {

	private final Provider<CommitApiFactory> commitApiFactoryProvider;
	private final Provider<BranchApiFactory> branchApiFactoryProvider;

	@Inject
	public RepositoryApiImpl(ManagedConfig managedConfig, Transformers transformers,
	                         RepositoriesManager repositoriesManager, Provider<CommitApiFactory> commitApiFactoryProvider,
	                         Provider<BranchApiFactory> branchApiFactoryProvider, @Assisted String repositoryName) {
		super(managedConfig, transformers, repositoriesManager, repositoryName);
		this.commitApiFactoryProvider = commitApiFactoryProvider;
		this.branchApiFactoryProvider = branchApiFactoryProvider;
	}

	@Override
	public Collection<BranchModel> getBranches() {
		try(RepositoryFacade repositoryFacade = new JGitRepositoryFacade(transformers, repository)) {
			return repositoryFacade.getBranches();
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	public Collection<TagModel> getTags() {
		try(RepositoryFacade repositoryFacade = new JGitRepositoryFacade(transformers, repository)) {
			return repositoryFacade.getTags();
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	public DetailedRepositoryModel getRepositoryModel() {
		try(RepositoryFacade repositoryFacade = new JGitRepositoryFacade(transformers, repository)) {
			return repositoryFacade.getRepositoryModel();
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	public DetailedRepositoryModel updateRepository(@Valid RepositoryModel repositoryModel) {
		Multimap<Permission, Identifier> permissions = HashMultimap.create();
		// Store the permissions in a multimap, switch generics
		repositoryModel.getPermissions().forEach((k, v) -> permissions.put(Permission.valueOf(v.getLevel()), new Identifier(k)));
		Identifier identifier = Identifier.valueOf(getRepositoryName());

		managedConfig.writeConfig(config -> {
			config.getRepositoryRule(identifier).stream()
				.map(RepositoryRule::getRules).flatMap(Collection::stream)
				// Filter only default access rules, keep custom ones
				.filter(accessRule -> accessRule.getAdjustedRefex().equals(AccessRule.DEFAULT_REFEX))
				.forEach(accessRule -> {
					// Clear all members...
					accessRule.getMembers().clear();
					// Add all new members
					accessRule.getMembers().addAll(permissions.get(accessRule.getPermission()));
				});

			// If a group has been deleted, remove it and its uses
			config.cleanUpModifiedRepositories();
		});

		return getRepositoryModel();
	}

	@Override
	@SneakyThrows
	@RequireAuthentication
	public void deleteRepository() {
		String repoName = repository.getURI().toString();
		repoName = repoName.substring(0, repoName.lastIndexOf('/'));
		Identifier identifier = Identifier.valueOf(repoName);
		managedConfig.writeConfig(config -> config.deleteIdentifierUses(identifier));
		repository.delete();
	}

	@Override
	public BranchApi getBranch(@NotNull String branchName) {
		return branchApiFactoryProvider.get().create(repository, branchName);
	}

	@Override
	public TagModel addTag(@Valid TagModel tagModel) {
		try(RepositoryFacade repositoryFacade = new JGitRepositoryFacade(transformers, repository)) {
			return repositoryFacade.addTag(tagModel);
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	public Collection<CommitModel> listCommits() {
		try(RepositoryFacade repositoryFacade = new JGitRepositoryFacade(transformers, repository)) {
			return repositoryFacade.listCommits();
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	public CommitApi getCommit(@NotNull String commitId) {
		return commitApiFactoryProvider.get().create(repository, commitId);
	}

}
