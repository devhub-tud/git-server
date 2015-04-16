package nl.tudelft.ewi.git.models;

import java.util.Collection;

import javax.validation.constraints.Pattern;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * This model represents a view of a group in the Gitolite config.
 * 
 * @author michael
 */
@Data
public class GroupModel extends IdentifiableModel {

	@Pattern(regexp = "^\\@\\w[\\w._\\@+-]+$")
	private String name;

	@NotEmpty
	private Collection<IdentifiableModel> members;

	@Override
	public boolean equals(Object other) {
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
