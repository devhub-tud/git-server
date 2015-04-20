package nl.tudelft.ewi.git.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;

import lombok.Data;

/**
 * A {@link BlameModel} is the response for Git blame requests.
 * A Blame result contains the source commitId and line number for
 * every line within a file at a certain commit.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Data
public class BlameModel {

	private String path;
	private String commitId;
	private List<BlameBlock> blames;
	
	/**
	 * In {@link BlameBlock BlameBlocks} we aggregate adjacent lines from
     * the same commit to deduplicate data.
	 * 
	 * @author Jan-Willem Gmelig Meyling
	 */
	@Data
	public static class BlameBlock {

        // Line number in the destination commit
		private int destinationFrom;
        // Line number in the commit where these lines were introduced
		private int sourceFrom;
        // Amount of adjacent lines that were introduced in the same commit
		private int length;
        // The commit that introduced these lines
		private String fromCommitId;
        // Original file path
        private String fromFilePath;
		
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
			return destinationFrom <= lineNumber && lineNumber < (destinationFrom + length);
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

        /**
         * @param lineNumber line index in the destination commit
         * @return the line number in the original commit
         */
        public Integer getFromLineNumber(Integer lineNumber) {
            return lineNumber + sourceFrom - destinationFrom;
        }
		
	}

	/**
	 * @param lineNumber the line number (starts at 1)
	 * @return the BlameBlock for the line at given index
	 */
    public BlameBlock getBlameBlock(Integer lineNumber) {
        for(BlameBlock block : blames) {
            if(block.contains(lineNumber)) {
                return block;
            }
        }
        throw new IllegalArgumentException("Line " + lineNumber + " not in " + blames);
    }
}
