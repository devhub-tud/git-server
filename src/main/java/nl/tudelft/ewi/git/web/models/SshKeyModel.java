package nl.tudelft.ewi.git.web.models;

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
	@Pattern(regexp = "^([a-zA-Z0-9\\.]+\\@)?[a-zA-Z0-9\\.]+\\.pub$")
	private String name;
	
	@NotEmpty
	@Pattern(regexp = "^[ssh-rsa\\ ].+$")
	private String contents;
	
}
