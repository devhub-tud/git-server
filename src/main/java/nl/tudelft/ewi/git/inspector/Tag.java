package nl.tudelft.ewi.git.inspector;

import lombok.Data;

/**
 * This class is a data class which represents a tag in a Git repository.
 * 
 * @author michael
 */
@Data
public class Tag {

	private String name;
	private String commit;
	
}
