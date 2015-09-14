package nl.tudelft.ewi.git.web.api.di;

import nl.tudelft.ewi.git.web.api.RepositoryApi;

/**
 * Factory used for instantiating the {@link RepositoryApi} sub resource through Guice assisted inject.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface RepositoryApiFactory extends Factory<RepositoryApi> {

	RepositoryApi create(String repositoryId);

}
