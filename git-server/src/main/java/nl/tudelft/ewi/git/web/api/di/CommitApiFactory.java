package nl.tudelft.ewi.git.web.api.di;

import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.gitolite.repositories.Repository;

/**
 * Factory used for instantiating the {@link CommitApi} sub resource through Guice assisted inject.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface CommitApiFactory extends Factory<CommitApi> {

	CommitApi create(Repository repository, String commit);

}
