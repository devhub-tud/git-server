package nl.tudelft.ewi.git.models;

import lombok.Data;

/**
 * This class is a data class which represents a tag in a Git repository.
 * 
 * @author michael
 */
@Data
public class TagModel {

	private String name;
	private String commit;
	
}
