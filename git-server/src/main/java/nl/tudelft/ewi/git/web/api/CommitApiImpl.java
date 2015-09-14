package nl.tudelft.ewi.git.web.api;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.Getter;
import nl.tudelft.ewi.git.backend.RepositoryFacade;
import nl.tudelft.ewi.git.backend.JGitRepositoryFacade;
import nl.tudelft.ewi.git.models.BlameModel;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.git.GitException;
import nl.tudelft.ewi.gitolite.repositories.Repository;
import org.eclipse.jgit.lib.ObjectLoader;
import org.jboss.resteasy.annotations.cache.Cache;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Cache(maxAge = 86400)
public class CommitApiImpl extends AbstractDiffableApi implements CommitApi {

	private final HttpServletResponse response;
	@Getter private final String ownCommitId;

	@Inject
	public CommitApiImpl(ManagedConfig managedConfig, Transformers transformers, HttpServletResponse response, @Assisted Repository repository, @Assisted String ownCommitId) {
		super(managedConfig, transformers, repository);
		this.ownCommitId = ownCommitId;
		this.response = response;
	}

	@Override
	public DetailedCommitModel get() {
		try(RepositoryFacade repositoryFacade = new JGitRepositoryFacade(transformers, repository)) {
			return repositoryFacade.retrieveCommit(ownCommitId);
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	protected String getCompareCommitId() {
		String[] parents = get().getParents();
		return (parents.length > 0) ? parents[0] : null;
	}

	@Override
	public BlameModel blame(String filePath) {
		try(RepositoryFacade repositoryFacade = new JGitRepositoryFacade(transformers, repository)) {
			return repositoryFacade.blame(ownCommitId, filePath);
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	public Map<String, EntryType> showTree(final String path) {
		try(RepositoryFacade repositoryFacade = new JGitRepositoryFacade(transformers, repository)) {
			return repositoryFacade.showTree(ownCommitId, path);
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

	@Override
	public InputStream showFile(final String path) {
		try(RepositoryFacade repositoryFacade = new JGitRepositoryFacade(transformers, repository)) {
			String fileName = path.substring(path.lastIndexOf('/') + 1);
			ObjectLoader objectLoader = repositoryFacade.showFile(ownCommitId, path);
			response.setHeader("Content-Length", Long.toString(objectLoader.getSize()));
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			return objectLoader.openStream();
		}
		catch (IOException e) {
			throw new GitException(e);
		}
	}

}
