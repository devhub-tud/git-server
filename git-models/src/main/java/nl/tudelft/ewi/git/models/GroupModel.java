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
@EqualsAndHashCode(callSuper = true)
public class GroupModel extends IdentifiableModel {

	@Pattern(regexp = "^\\@\\w[\\w._\\@+-]+$")
	private String name;

	@NotEmpty
	private Collection<IdentifiableModel> members;

}
