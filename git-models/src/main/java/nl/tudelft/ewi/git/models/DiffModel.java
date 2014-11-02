package nl.tudelft.ewi.git.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This class is a data class which represents a diff between two commits in a Git repository.
 * 
 * @author michael
 */
@Data
@EqualsAndHashCode
public class DiffModel {
	
	public enum Type {
		/** Add a new file to the project */
		ADD,

		/** Modify an existing file in the project (content and/or mode) */
		MODIFY,

		/** Delete an existing file from the project */
		DELETE,

		/** Rename an existing file to a new location */
		RENAME,

		/** Copy an existing file to a new location, keeping the original */
		COPY;
	}

	private Type type;
	private String oldPath;
	private String newPath;
	private String[] raw;

}
