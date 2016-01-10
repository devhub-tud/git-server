package nl.tudelft.ewi.git.models;

import javax.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * This model represents a simplified view of a group or user in the Gitolite config.
 * 
 * @author michael
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentifiableModel {

	@Pattern(regexp = "^\\@?[a-zA-Z0-9]+$")
	private String name;

}
