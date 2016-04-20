package nl.tudelft.ewi.git.web.api;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
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
import nl.tudelft.ewi.gitolite.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.permission.Permission;
import nl.tudelft.ewi.gitolite.repositories.RepositoriesManager;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

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
	@Context private ResourceContext resourceContext;


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
		RepositoryApi repositoryApi = repositoryApiFactory.create(repositoryId);
		if (resourceContext != null) {
			return resourceContext.initResource(repositoryApi);
		}
		return repositoryApi;
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
			cloneTemplateRepository(name, repositoryUrl, templateUrl);
		}
		else {
			initializeBareRepository(name, repositoryUrl);
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

			managedConfig.readConfig(readConfig -> {
				for(Level level : permissions.keySet()) {
					Permission permission = Permission.valueOf(level.getLevel());
					Collection<Identifier> members = Lists.newArrayList();
					Collection<GroupRule> groups = Lists.newArrayList();

					for (String name : permissions.get(level)) {
						if (name.startsWith("@")) {
							groups.add(readConfig.getGroup(name));
						}
						else {
							members.add(Identifier.valueOf(name));
						}
					}

					repositoryRuleBuilder.rule(new AccessRule(permission, groups, members));
				}

				managedConfig.writeConfig(config ->
					config.addRepositoryRule(repositoryRuleBuilder.build()));
			});
		}
	}

	protected void initializeBareRepository(String repositoryName, String repositoryUrl) {

		File repositoryDirectory = new File(config.getMirrorsDirectory(), repositoryName);

		try {

			if(repositoryDirectory.exists()) {
				FileUtils.deleteDirectory(repositoryDirectory);
			}

			FileUtils.forceMkdir(repositoryDirectory);

			log.info("Preparing bare repository in {}", repositoryDirectory);
			Git repo = Git.init()
				.setDirectory(repositoryDirectory)
				.setBare(false)
				.call();

			//if we can add a file do so
			if( new File(repositoryDirectory,"README.md").createNewFile()) {
				repo.add().addFilepattern("README.md").call();
				repo.commit().setMessage("intial commit containing empty readme").call();
			}

			log.info("Pushing {} to {}", repositoryDirectory, repositoryUrl);
			repo.push()
				.setRemote(repositoryUrl)
				.setPushAll()
				.setPushTags()
				.call();

			log.info("Finished provisioning {}", repositoryName);
		}
		catch (GitAPIException | IOException e) {
			log.warn(e.getMessage(), e);
			FileUtils.deleteQuietly(repositoryDirectory);
			throw new GitException(e);
		}
	}

	protected void cloneTemplateRepository(String repositoryName, String repositoryUrl, String templateUrl) {

		File repositoryDirectory = new File(config.getMirrorsDirectory(), repositoryName);

		try {

			if(repositoryDirectory.exists()) {
				FileUtils.deleteDirectory(repositoryDirectory);
			}

			FileUtils.forceMkdir(repositoryDirectory);

			log.info("Cloning {} into {}", templateUrl, repositoryDirectory);
			Git repo = Git.cloneRepository()
				.setDirectory(repositoryDirectory)
				.setURI(templateUrl)
				.setCloneAllBranches(true)
				.setCloneSubmodules(true)
				.call();

			log.info("Pushing {} to {}", repositoryDirectory, repositoryUrl);
			repo.push()
				.setRemote(repositoryUrl)
				.setPushAll()
				.setPushTags()
				.call();

			log.info("Finished provisioning {}", repositoryName);
		}
		catch (GitAPIException | IOException e) {
			log.warn(e.getMessage(), e);
			FileUtils.deleteQuietly(repositoryDirectory);
			throw new GitException(e);
		}

	}

}
