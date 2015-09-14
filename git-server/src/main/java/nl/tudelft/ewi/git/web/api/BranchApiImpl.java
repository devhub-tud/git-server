package nl.tudelft.ewi.git.web.api;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import nl.tudelft.ewi.git.backend.RepositoryFacade;
import nl.tudelft.ewi.git.backend.JGitRepositoryFacade;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CommitSubList;
import nl.tudelft.ewi.git.models.MergeResponse;
import nl.tudelft.ewi.git.web.api.di.CommitApiFactory;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.git.GitException;
import nl.tudelft.ewi.gitolite.repositories.Repository;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import java.io.IOException;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class BranchApiImpl extends AbstractDiffableApi implements BranchApi {

	private final HttpServletResponse response;
	private final CommitApiFactory commitApiFactory;
	private final String branchName;

	@Inject
	public BranchApiImpl(ManagedConfig managedConfig, HttpServletResponse response, Transformers transformers,
	                     CommitApiFactory commitApiFactory, @Assisted Repository repository, @Assisted String branchName) {
		super(managedConfig, transformers, repository);
		this.commitApiFactory = commitApiFactory;
		this.branchName = branchName;
		this.response = response;
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
	public MergeResponse merge() {
		return null;
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
	public CommitModel mergeBase() {
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
		return mergeBase().getCommit();
	}

	@Override
	public CommitApi getCommit() {
		return commitApiFactory.create(repository, getOwnCommitId());
	}
}
