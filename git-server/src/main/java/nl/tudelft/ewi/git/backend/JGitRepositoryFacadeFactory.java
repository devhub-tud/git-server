package nl.tudelft.ewi.git.backend;

import nl.tudelft.ewi.git.web.api.Transformers;
import nl.tudelft.ewi.gitolite.repositories.Repository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.util.WeakHashMap;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Singleton
public class JGitRepositoryFacadeFactory implements RepositoryFacadeFactory {

	private final Transformers transformers;
	private final WeakHashMap<URI, JGitRepositoryFacade> cache = new WeakHashMap<>();

	@Inject
	public JGitRepositoryFacadeFactory(Transformers transformers) {
		this.transformers = transformers;
	}

	@Override
	public JGitRepositoryFacade create(Repository repository) throws IOException {
		URI uri = repository.getURI();
		JGitRepositoryFacade jGitRepositoryFacade = cache.get(uri);

		if (jGitRepositoryFacade != null) {
			synchronized (jGitRepositoryFacade) {
				if (!jGitRepositoryFacade.isClosed()) {
					jGitRepositoryFacade.incrementUses();
				}
				else {
					jGitRepositoryFacade = null;
					cache.remove(uri);
				}
			}
		}

		if (jGitRepositoryFacade == null) {
			jGitRepositoryFacade = new JGitRepositoryFacade(transformers, repository);
			cache.put(uri, jGitRepositoryFacade);
		}

		return jGitRepositoryFacade;
	}

}
