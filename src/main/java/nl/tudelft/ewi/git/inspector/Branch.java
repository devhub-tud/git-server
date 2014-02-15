package nl.tudelft.ewi.git.inspector;

import lombok.Data;

/**
 * This class is a data class which represents a branch in a Git repository.
 * 
 * @author michael
 */
@Data
public class Branch {

	private String name;
	private String commit;

}
