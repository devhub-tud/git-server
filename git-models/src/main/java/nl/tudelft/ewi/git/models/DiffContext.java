package nl.tudelft.ewi.git.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * Diffs usually contain 3 context lines around the actual changes. Context
 * lines are unchanged lines between two commits.
 * 
 * @author Jan-Willem Gmelig Meyling
 *
 */
@Data
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

}
