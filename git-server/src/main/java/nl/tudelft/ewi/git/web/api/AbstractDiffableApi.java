package nl.tudelft.ewi.git.web.api;

import nl.tudelft.ewi.git.backend.RepositoryFacade;
import nl.tudelft.ewi.git.backend.JGitRepositoryFacade;
import nl.tudelft.ewi.git.backend.RepositoryFacadeFactory;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.git.GitException;
import nl.tudelft.ewi.gitolite.repositories.Repository;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import java.io.IOException;

/**
 * Both the {@link CommitApi} and {@link BranchApi} have diff endpoints. This is a basic implementation
 * for responding diffs.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public abstract class AbstractDiffableApi extends AbstractRepositoryApi implements DiffableApi {

	protected AbstractDiffableApi(ManagedConfig managedConfig, Transformers transformers, RepositoryFacadeFactory repositoryFacadeFactory, Repository repository) {
		super(managedConfig, transformers, repository, repositoryFacadeFactory);
	}

	protected abstract String getOwnCommitId();

	protected abstract String getCompareCommitId();

	@Override
	public final DiffModel diff(@DefaultValue("3") int context) {
		return diff(getCompareCommitId(), context);
	}

	@Override
	public final DiffBlameModel diffBlame(@DefaultValue("3") int context) {
		return diffBlame(getCompareCommitId(), context);
	}

	@Override
	public DiffModel diff(@NotNull String leftCommitId, @DefaultValue("3") int context) {
		String rightCommitId = getOwnCommitId();
		try(RepositoryFacade repositoryFacade = repositoryFacadeFactory.create(repository)) {
			return repositoryFacade.calculateDiff(leftCommitId, rightCommitId, context);
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	public DiffBlameModel diffBlame(@NotNull String oldCommitId, @DefaultValue("3") int context) {
		String rightCommitId = getOwnCommitId();
		try(RepositoryFacade repositoryFacade = repositoryFacadeFactory.create(repository)) {
			DiffModel diffModel = repositoryFacade.calculateDiff(oldCommitId, rightCommitId, context);
			return repositoryFacade.addBlameData(diffModel);
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}
}
