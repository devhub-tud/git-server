package nl.tudelft.ewi.git.web.api;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.Config;
import nl.tudelft.ewi.git.backend.RepositoryFacade;
import nl.tudelft.ewi.git.backend.RepositoryFacadeFactory;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CommitSubList;
import nl.tudelft.ewi.git.models.MergeResponse;
import nl.tudelft.ewi.git.web.api.di.CommitApiFactory;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.git.GitException;
import nl.tudelft.ewi.gitolite.git.ServiceUnavailable;
import nl.tudelft.ewi.gitolite.repositories.Repository;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class BranchApiImpl extends AbstractDiffableApi implements BranchApi {

	private final Config config;
	private final CommitApiFactory commitApiFactory;
	private final String branchName;
	@Context private ResourceContext resourceContext;

	@Inject
	public BranchApiImpl(Config config, ManagedConfig managedConfig, Transformers transformers,
	                     CommitApiFactory commitApiFactory, RepositoryFacadeFactory repositoryFacadeFactory,
	                     @Assisted Repository repository, @Assisted String branchName) {
		super(managedConfig, transformers, repositoryFacadeFactory, repository);
		this.config = config;
		this.commitApiFactory = commitApiFactory;
		this.branchName = branchName;
	}

	@Override
	public BranchModel get() {
		try(RepositoryFacade repositoryFacade = repositoryFacadeFactory.create(repository)) {
			return repositoryFacade.getBranch(branchName);
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	public MergeResponse mergeInto(String message, String name, String email, String intoBranch) {
		if(Strings.isNullOrEmpty(message)) {
			message = String.format("Merged origin/%s into origin/%s", branchName, intoBranch);
		}

		Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(email));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(intoBranch));

		try {
			Git git = createOrOpenRepositoryMirror();
			org.eclipse.jgit.lib.Repository repo = git.getRepository();

			log.info(
				"Fetching remote {}",
				git.remoteList().call().stream()
					.map(RemoteConfig::getURIs)
					.flatMap(Collection::stream)
					.map(URIish::toASCIIString)
					.collect(Collectors.toList())
			);

			git.fetch().setRemote("origin").call();

			String baseBranch = "origin/" + intoBranch;
			String toMergeBranch = this.branchName.replace("refs/heads", "origin");

			log.info("Checking out {}", baseBranch);

			try {
				git.checkout()
						.setCreateBranch(true)
						.setForce(true)
						.setName(intoBranch)
						.setStartPoint(baseBranch)
						.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
						.call();
			} catch(InternalServerErrorException e){
				System.out.println("Branch already exists.");
				git.checkout()
						.setName(intoBranch)
						.setStartPoint(baseBranch)
						.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
						.call();
			}
			log.info("Pulling latest changes for {}", baseBranch);

			git.reset()
				.setRef(baseBranch)
				.setMode(ResetType.HARD)
				.call();

			if (! git.status().call().isClean()) {
				throw new InternalServerErrorException(
					new IllegalStateException("The repository " + git.getRepository().getWorkTree() +
						" is not in a clean state: " + git.describe().call())
				);
			}

			log.info("Merging {} into {}", toMergeBranch, baseBranch);

			MergeResult ret = git.merge()
				.include(repo.findRef(toMergeBranch))
				.setStrategy(MergeStrategy.RECURSIVE)
				.setSquash(false)
				.setCommit(true)
				.setMessage(message)
				.setFastForward(MergeCommand.FastForwardMode.NO_FF)
				.call();

			log.info("Merge returned with status {}", ret.getMergeStatus());

			if (Arrays.asList(
				MergeStatus.FAST_FORWARD_SQUASHED,
				MergeStatus.MERGED_SQUASHED,
				MergeStatus.MERGED
			).contains(ret.getMergeStatus())) {
				log.info("Adding author info to commit: {} <{}>", name, email);
				git.commit()
					.setAmend(true)
					.setAuthor(name, email)
					.setCommitter(name, email)
					.setMessage(message)
					.call();
			} else {
				log.info("Merge failed, resetting work directory");
				git.reset()
					.setMode(ResetCommand.ResetType.HARD)
					.call();
			}

			log.info("Pushing changes to remote");
			git.push().call();

			MergeResponse res = new MergeResponse();
			res.setStatus(ret.getMergeStatus().name());
			res.setSuccess(ret.getMergeStatus().isSuccessful());
			return res;
		}
		catch (InvalidRefNameException | NoMessageException e) {
			log.warn(e.getMessage(), e);
			throw new BadRequestException(e.getMessage(), e);
		}
		catch (TransportException e) {
			log.warn(e.getMessage(), e);
			throw new ServiceUnavailable(e);
		}
		catch (RefNotFoundException e) {
			log.warn(e.getMessage(), e);
			throw new NotFoundException(e.getMessage(), e);
		}
		catch (GitAPIException | IOException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException(e.getMessage(), e);
		}
	}

	@VisibleForTesting
	public Git createOrOpenRepositoryMirror() throws GitException, IOException, GitAPIException {
		File repositoryDirectory = new File(config.getMirrorsDirectory(), repository.getURI().toString());
		Git git;

		String uri = repository.getURI().toString();
		uri = uri.substring(0, uri.lastIndexOf(".git/"));

		try {
			git = Git.open(repositoryDirectory);
			git.fetch().call();
		}
		catch (RepositoryNotFoundException e) {
			FileUtils.forceMkdir(repositoryDirectory);
			FileUtils.cleanDirectory(repositoryDirectory);
			String repositoryURL = config.getGitoliteBaseUrl() + uri;
			log.info("Cloning repository {} into work directory {}", repositoryURL, repositoryDirectory);

			git = Git.cloneRepository()
				.setURI(repositoryURL)
				.setDirectory(repositoryDirectory)
				.setCloneAllBranches(true)
				.call();
		}

		return git;
	}

	@Override
	public CommitSubList retrieveCommitsInBranch(@DefaultValue("0") int skip, @DefaultValue("25") int limit) {
		try(RepositoryFacade repositoryFacade = repositoryFacadeFactory.create(repository)) {
			return repositoryFacade.getCommitsFor(branchName, skip, limit);
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	public CommitApi mergeBase() {
		CommitApi commitApi = commitApiFactory.create(repository, getCompareCommitId());
		if (resourceContext != null) {
			return resourceContext.initResource(commitApi);
		}
		return commitApi;
	}

	private CommitModel mergeBaseCommit() {
		try(RepositoryFacade repositoryFacade = repositoryFacadeFactory.create(repository)) {
			return repositoryFacade.mergeBase(branchName);
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	public void deleteBranch() {
		try(RepositoryFacade repositoryFacade = repositoryFacadeFactory.create(repository)) {
			repositoryFacade.deleteBranch(branchName);
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	protected String getOwnCommitId() {
		return get().getCommit().getCommit();
	}

	@Override
	protected String getCompareCommitId() {
		return mergeBaseCommit().getCommit();
	}

	@Override
	public CommitApi getCommit() {
		CommitApi commitApi = commitApiFactory.create(repository, getOwnCommitId());
		if (resourceContext != null) {
			return resourceContext.initResource(commitApi);
		}
		return commitApi;
	}

}
