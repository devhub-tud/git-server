package nl.tudelft.ewi.git.web.api.di;

import nl.tudelft.ewi.git.web.api.GroupApi;

/**
 * Factory used for instantiating the {@link GroupApi} sub resource through Guice assisted inject.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface GroupApiFactory extends Factory<GroupApi> {

	GroupApi create(String groupName);

}
