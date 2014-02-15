package nl.tudelft.ewi.git.web.models;

import java.util.Collection;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This model represents a view of a user in the Gitolite config.
 *
 * @author michael
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserModel extends IdentifiableModel {

	private Collection<SshKeyModel> keys;
	
}
