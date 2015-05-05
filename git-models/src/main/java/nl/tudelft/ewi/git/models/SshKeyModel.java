package nl.tudelft.ewi.git.models;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * This model represents a view of a SSH key in the Gitolite config.
 *
 * @author michael
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SshKeyModel extends BaseModel {

	@NotNull
	@Pattern(regexp = "^[\\w._+-]*$")
	private String name;
	
	@NotEmpty
	@Pattern(regexp = "ssh-rsa AAAA[0-9A-Za-z+/]+[=]{0,3}(\\s([^@]+@[^@]+))?[\\r\\n\\s]*")
	private String contents;
	
}
