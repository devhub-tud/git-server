package nl.tudelft.ewi.git.web.api;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.Config;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;
import nl.tudelft.ewi.git.web.api.di.RepositoryApiFactory;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.git.GitException;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.parser.rules.AccessRule;
import nl.tudelft.ewi.gitolite.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.permission.Permission;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * Implementation for {@link RepositoriesApi}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class RepositoriesApiImpl implements RepositoriesApi {

	private final Transformers transformers;
	private final RepositoriesManager repositoriesManager;
	private final RepositoryApiFactory repositoryApiFactory;
	private final ManagedConfig managedConfig;
	private final Config config;


	@Inject
	public RepositoriesApiImpl(Transformers transformers, RepositoriesManager repositoriesManager,
	                           RepositoryApiFactory repositoryApiFactory, ManagedConfig managedConfig,
	                           Config config) {
		this.transformers = transformers;
		this.repositoriesManager = repositoriesManager;
		this.repositoryApiFactory = repositoryApiFactory;
		this.managedConfig = managedConfig;
		this.config = config;
	}

	@Override
	public Collection<RepositoryModel> listAllRepositories() {
		return Collections2.transform(repositoriesManager.getRepositories(), transformers::transformRepository);
	}

	@Override
	public RepositoryApi getRepository(@NotNull String repositoryId) {
		return repositoryApiFactory.create(repositoryId);
	}

	@Override
	public DetailedRepositoryModel createRepository(@Valid CreateRepositoryModel createRepositoryModel) {
		Preconditions.checkNotNull(createRepositoryModel);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(createRepositoryModel.getName()));
		createAccessRules(createRepositoryModel);

		String name = createRepositoryModel.getName();
		String templateUrl = createRepositoryModel.getTemplateRepository();
		String repositoryUrl = config.getGitoliteBaseUrl().concat(name);
		if(!Strings.isNullOrEmpty(templateUrl)) {
			cloneTemplateRepository(repositoryUrl, templateUrl);
		}

		return getRepository(name).getRepositoryModel();
	}

	protected void createAccessRules(@Valid CreateRepositoryModel createRepositoryModel) {
		Multimap<Level, String> permissions = HashMultimap.create();
		// Store the permissions in a multimap, switch generics
		createRepositoryModel.getPermissions().forEach((k, v) -> permissions.put(v, k));

		if(!permissions.isEmpty()) {
			RepositoryRule.RepositoryRuleBuilder repositoryRuleBuilder = RepositoryRule.builder()
				.identifiable(Identifier.valueOf(createRepositoryModel.getName()));

			for(Level level : permissions.keySet()) {
				Permission permission = Permission.valueOf(level.getLevel());
				Collection<Identifier> identifiers = Collections2.transform(permissions.get(level), Identifier::valueOf);
				repositoryRuleBuilder.rule(new AccessRule(permission, Collections.emptyList(), identifiers));
			}

			managedConfig.writeConfig(config ->
				config.addRepositoryRule(repositoryRuleBuilder.build()));
		}
	}

	protected void cloneTemplateRepository(String repositoryUrl, String templateUrl) {
		File dir = Files.createTempDir();

		try {
			Git repo = Git.cloneRepository()
				.setDirectory(dir)
				.setURI(templateUrl)
				.setCloneAllBranches(true)
				.setCloneSubmodules(true)
				.call();

			repo.push()
				.setRemote(repositoryUrl)
				.setPushAll()
				.setPushTags()
				.call();
		}
		catch (GitAPIException e) {
			log.warn(e.getMessage(), e);
			throw new GitException(e);
		}
		finally {
			FileUtils.deleteQuietly(dir);
		}
	}

}
