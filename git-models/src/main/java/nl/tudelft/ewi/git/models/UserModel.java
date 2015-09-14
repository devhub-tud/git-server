package nl.tudelft.ewi.git.models;

import java.util.Collection;

import javax.validation.constraints.Pattern;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Sets;

/**
 * This model represents a view of a user in the Gitolite config.
 * 
 * @author michael
 */
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserModel extends IdentifiableModel {

	@Pattern(regexp = "^\\w[\\w._\\@+-]+$")
	private String name;

	private Collection<SshKeyModel> keys = Sets.newHashSet();

	@Override
	public boolean equals(Object other) {
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
