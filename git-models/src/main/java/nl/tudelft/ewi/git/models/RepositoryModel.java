package nl.tudelft.ewi.git.models;

import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * This model represents a view of a repository in the Gitolite config.
 *
 * @author michael
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RepositoryModel extends BaseModel {

	@NotEmpty
	private String name;
	
	@Setter(AccessLevel.PACKAGE)
	private String url;
	
	private Map<String, String> permissions;
	
}
