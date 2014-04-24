package nl.tudelft.ewi.git.models;

import java.util.Collection;

import javax.validation.constraints.Pattern;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Sets;

/**
 * This model represents a view of a user in the Gitolite config.
 * 
 * @author michael
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserModel extends IdentifiableModel {

	@NotEmpty
	@Pattern(regexp = "^[a-zA-Z0-9]+$")
	private String name;

	@Setter(AccessLevel.PACKAGE)
	private Collection<SshKeyModel> keys = Sets.newHashSet();

}
