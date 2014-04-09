package nl.tudelft.ewi.git.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This model represents a view of a repository in the Gitolite config.
 *
 * @author michael
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateRepositoryModel extends RepositoryModel {
	
	private String templateRepository;
	
}
