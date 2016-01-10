package nl.tudelft.ewi.git.web.api;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.Config;
import nl.tudelft.ewi.git.backend.RepositoryFacade;
import nl.tudelft.ewi.git.backend.JGitRepositoryFacade;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CommitSubList;
import nl.tudelft.ewi.git.models.MergeResponse;
import nl.tudelft.ewi.git.web.api.di.CommitApiFactory;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.git.GitException;
import nl.tudelft.ewi.gitolite.git.ServiceUnavailable;
import nl.tudelft.ewi.gitolite.repositories.Repository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.merge.MergeStrategy;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.IOException;

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
	                     CommitApiFactory commitApiFactory, @Assisted Repository repository, @Assisted String branchName) {
		super(managedConfig, transformers, repository);
		this.config = config;
		this.commitApiFactory = commitApiFactory;
		this.branchName = branchName;
	}

	@Override
	public BranchModel get() {
		try(RepositoryFacade repositoryFacade = new JGitRepositoryFacade(transformers, repository)) {
			return repositoryFacade.getBranch(branchName);
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	public MergeResponse merge(String message, String name, String email) {
		if(Strings.isNullOrEmpty(message)) {
			message = String.format("Merged origin/%s into origin/master", branchName);
		}
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(email));

		try {
			Git git = createOrOpenRepositoryMirror();
			org.eclipse.jgit.lib.Repository repo = git.getRepository();

			git.fetch().call();

			git.checkout()
				.setName("master")
				.setStartPoint("origin/master")
				.setForce(true).call();

			MergeResult ret = git.merge()
				.include(repo.getRef("origin/" + branchName.substring(branchName.lastIndexOf('/') + 1)))
				.setStrategy(MergeStrategy.RECURSIVE)
				.setSquash(false)
				.setCommit(true)
				.setMessage(message)
				.setFastForward(MergeCommand.FastForwardMode.NO_FF)
				.call();

			if (ret.getMergeStatus().isSuccessful()) {
				git.commit()
					.setAmend(true)
					.setAuthor(name, email)
					.setCommitter(name, email)
					.setMessage(message)
					.call();
			} else {
				git.reset()
					.setMode(ResetCommand.ResetType.HARD)
					.call();
			}

			git.push().call();
			log.info("Merged {} into {} with status {}", branchName, repository, ret.getMergeStatus());

			MergeResponse res = new MergeResponse();
			res.setStatus(ret.getMergeStatus().name());
			res.setSuccess(ret.getMergeStatus().isSuccessful());
			return res;
		}
		catch (InvalidRefNameException | NoMessageException e) {
			throw new BadRequestException(e);
		}
		catch (TransportException e) {
			throw new ServiceUnavailable(e);
		}
		catch (RefNotFoundException e) {
			throw new NotFoundException(e);
		}
		catch (GitAPIException | IOException e) {
			throw new InternalServerErrorException(e);
		}
	}

	private Git createOrOpenRepositoryMirror() throws GitException, IOException, GitAPIException {
		File repositoryDirectory = new File(config.getMirrorsDirectory(), repository.getURI().toString());
		Git git;

		if(!repositoryDirectory.exists()) {
			repositoryDirectory.mkdirs();
			git = Git.cloneRepository()
				.setURI(config.getGitoliteBaseUrl() + repository.getURI().toString())
				.setDirectory(repositoryDirectory)
				.setCloneAllBranches(true)
				.call();
		}
		else {
			git = Git.open(repositoryDirectory);
		}

		git.checkout().setName("master").call();
		git.pull().call();
		return git;
	}

	@Override
	public CommitSubList retrieveCommitsInBranch(@DefaultValue("0") int skip, @DefaultValue("25") int limit) {
		try(RepositoryFacade repositoryFacade = new JGitRepositoryFacade(transformers, repository)) {
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
		try(RepositoryFacade repositoryFacade = new JGitRepositoryFacade(transformers, repository)) {
			return repositoryFacade.mergeBase(branchName);
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	public void deleteBranch() {
		try(RepositoryFacade repositoryFacade = new JGitRepositoryFacade(transformers, repository)) {
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
