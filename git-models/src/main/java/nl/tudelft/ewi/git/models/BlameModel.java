package nl.tudelft.ewi.git.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;

import lombok.Data;

/**
 * A {@link BlameModel} is the response for Git blame requests
 * @author Jan-Willem Gmelig Meyling
 *
 */
@Data
public class BlameModel {

	private String path;
	private String commitId;
	private List<BlameBlock> blames;
	
	/**
	 * A Blame result contains the source commitId and line number for every line
	 * within a file at a certain commit. In {@link BlameBlock BlameBlocks} we
	 * aggregate adjacent lines from the same commit to deduplicate data.
	 * 
	 * @author Jan-Willem Gmelig Meyling
	 */
	@Data
	public static class BlameBlock {
		
		private int destinationFrom;
		private int sourceFrom;
		private int length;
		private String fromCommitId;
		
		/**
		 * Helper function to check if a line number is within this block. Note:
		 * The line number should be a line number of the file in the source
		 * commit.
		 * 
		 * @param lineNumber
		 *            line number of the file in the source commit.
		 * @return true if the line number is within this block
		 */
		public boolean contains(int lineNumber) {
			return sourceFrom >= lineNumber && lineNumber <= (sourceFrom + length);
		}
		
		@JsonIgnore
		public int getDestinationTo() {
			return destinationFrom + length;
		}
		
		@JsonIgnore
		public int getSourceTo() {
			return sourceFrom + length;
		}
		
		@JsonIgnore
		public void incrementLength() {
			length++;
		}
		
	}
	
	/**
	 * Get the line number in the destination commit for a line number in a
	 * source commit.
	 * 
	 * @param commitId
	 *            Commit id for the line number. The source commit should be
	 *            author of the line, and thus be included in the blame result.
	 * @param lineNumber
	 *            Line number of the line at the given commitId
	 * @return line number in the destination commit, or null if not found
	 */
	public Integer lineNumberFor(final String commitId, int lineNumber) {
		Preconditions.checkNotNull(commitId);
		
		if(commitId.equals(getCommitId())) {
			return lineNumber;
		}
		
		for(BlameBlock block : getBlames()) {
			if (commitId.equals(block.getFromCommitId())
					&& block.contains(lineNumber)) {
				return lineNumber - block.getSourceFrom() + block.getDestinationFrom();
			}
		}
		
		return null;
	}
	
}
