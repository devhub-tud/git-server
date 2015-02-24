package nl.tudelft.ewi.git.models;

import java.util.List;

import nl.tudelft.ewi.git.models.DiffLine.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Diffs usually contain 3 context lines around the actual changes. Context
 * lines are unchanged lines between two commits.
 * 
 * @author Jan-Willem Gmelig Meyling
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiffContext {

	private Integer oldStart, oldEnd, newStart, newEnd;
	private List<DiffLine> diffLines;

	/**
	 * @param type
	 *            for the lines
	 * @return the amount of lines with a specific type (eg. only additions)
	 */
	public int amountOfLinesWithType(final DiffLine.Type type) {
		int amount = 0;
		for(DiffLine diffLine : diffLines)
			if(diffLine.getType().equals(type))
				amount++;
		return amount;
	}
	
	/**
	 * @return the amount of lines in this {@code DiffContext}
	 */
	@JsonIgnore
	public int getLineCount() {
		return diffLines.size();
	}
	
	@JsonIgnore
	public int getRemovedCount() {
		return amountOfLinesWithType(Type.REMOVED);
	}
	
	@JsonIgnore
	public int getAddedCount() {
		return amountOfLinesWithType(Type.ADDED);
	}
	
}
