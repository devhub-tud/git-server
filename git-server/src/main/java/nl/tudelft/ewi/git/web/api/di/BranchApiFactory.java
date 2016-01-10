package nl.tudelft.ewi.git.web.api.di;

import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.gitolite.repositories.Repository;

/**
 * Factory used for instantiating the {@link BranchApi} sub resource through Guice assisted inject.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface BranchApiFactory extends Factory<BranchApi> {

	BranchApi create(Repository repository, String branchName);

}
