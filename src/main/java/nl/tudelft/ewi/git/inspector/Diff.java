package nl.tudelft.ewi.git.inspector;

import lombok.Data;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;

/**
 * This class is a data class which represents a diff between two commits in a Git repository.
 * 
 * @author michael
 */
@Data
public class Diff {

	private ChangeType changeType;
	private String oldPath;
	private String newPath;
	private String[] raw;

}
