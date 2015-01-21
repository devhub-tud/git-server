package nl.tudelft.ewi.git.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A {@code DiffLine} represents one line in a {@link DiffModel}. It can be added,
 * removed or unchanged (context).
 * 
 * @author Jan-Willem Gmelig Meyling
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiffLine {
	
	public enum Type {
		CONTEXT, ADDED, REMOVED;
	}
	
	private Type type;
	private String content;
	
	/**
	 * @return true if this line was added to the file between these commits
	 */
	@JsonIgnore
	public boolean isAdded() {
		return type == Type.ADDED;
	}
	
	/**
	 * @return true if this line was removed from the file between these commits
	 */
	@JsonIgnore
	public boolean isRemoved() {
		return type == Type.REMOVED;
	}
	
	/**
	 * @return true if this line was not changed between these commits
	 */
	@JsonIgnore
	public boolean isUnchanged() {
		return type == Type.CONTEXT;
	}
	
}
