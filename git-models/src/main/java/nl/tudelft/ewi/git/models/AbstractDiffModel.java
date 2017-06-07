package nl.tudelft.ewi.git.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffFile;

import java.util.List;

/**
 * Shared data between a {@link DiffModel} and a {@link DiffBlameModel}.
 *
 * @param <DF>  a generic parameter for the used {@link DiffFile}, {@link DiffContext}
 *            and {@link DiffLine} implementation.
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public abstract class AbstractDiffModel<DF extends DiffFile> {


	private CommitModel newCommit;

	private CommitModel oldCommit;

	/**
	 * The changed files between newCommit and oldCommit
	 */
	private List<DF> diffs;

	/**
	 * The commits between newCommit and oldCommit
	 */
	private List<CommitModel> commits;

	@JsonIgnore
	public boolean isAhead() {
		return !commits.isEmpty();
	}

	@JsonIgnore
	public int getAhead() {
		return commits.size();
	}

	/**
	 * This class is a data class which represents a diff between two commits in a Git repository.
	 *
	 * @author michael
	 */
	@Data
	@EqualsAndHashCode
	public static class DiffFile<DC extends DiffContext> {

		private ChangeType type;
		private String oldPath;
		private String newPath;
		private List<DC> contexts;

		protected int amountOfLinesWithType(final LineType type) {
			return getContexts().stream()
				.mapToInt(context -> context.amountOfLinesWithType(type))
				.sum();
		}

		/**
		 * @return the amount of added lines in this {@code DiffModel}
		 */
		@JsonIgnore
		public int getLinesAdded() {
			return amountOfLinesWithType(LineType.ADDED);
		}

		/**
		 * @return the amount of removed lines in this {@code DiffModel}
		 */
		@JsonIgnore
		public int getLinesRemoved() {
			return amountOfLinesWithType(LineType.REMOVED);
		}

		@JsonIgnore
		public boolean isDeleted() {
			return type.equals(ChangeType.DELETE);
		}

		@JsonIgnore
		public boolean isAdded() {
			return type.equals(ChangeType.ADD);
		}

		@JsonIgnore
		public boolean isModified() {
			return type.equals(ChangeType.MODIFY);
		}

		@JsonIgnore
		public boolean isCopied() {
			return type.equals(ChangeType.COPY);
		}

		@JsonIgnore
		public boolean isMoved() {
			return type.equals(ChangeType.RENAME);
		}

	}

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
	public static class DiffContext<DL extends DiffLine> {

		private List<DL> lines;

		/**
		 * @param type
		 *            for the lines
		 * @return the amount of lines with a specific type (eg. only additions)
		 */
		public int amountOfLinesWithType(final LineType type) {
			int amount = 0;
			for(DiffLine line : lines) {
				switch (type){
					case ADDED:
						if(line.isAdded()) amount++;
						break;
					case CONTEXT:
						if(line.isUnchanged()) amount++;
						break;
					case REMOVED:
						if(line.isRemoved()) amount++;
						break;
				}
			}
			return amount;
		}

	}

	/**
	 * A {@code DiffLine} represents one line in a {@link DiffFile}. It can be added,
	 * removed or unchanged (context).
	 *
	 * @author Jan-Willem Gmelig Meyling
	 *
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DiffLine {

		private Integer oldLineNumber;
		private Integer newLineNumber;
		private String content;

		/**
		 * @return true if this line was added to the file between these commits
		 */
		@JsonIgnore
		public boolean isAdded() {
			return oldLineNumber == null;
		}

		/**
		 * @return true if this line was removed from the file between these commits
		 */
		@JsonIgnore
		public boolean isRemoved() {
			return newLineNumber == null;
		}

		/**
		 * @return true if this line was not changed between these commits
		 */
		@JsonIgnore
		public boolean isUnchanged() {
			return oldLineNumber != null && newLineNumber != null;
		}

	}

	/**
	 * @return the amount of lines w/ a type
	 * @param type
	 * @return int
	 */
	private int amountOfLinesWithType(final LineType type) {
		return getDiffs().stream()
				.mapToInt(context -> context.amountOfLinesWithType(type))
				.sum();
	}

	/**
	 * @return the amount of added lines in this {@code DiffMContext}
	 */
	@JsonIgnore
	public int getLinesAdded() {
		return amountOfLinesWithType(LineType.ADDED);
	}

	/**
	 * @return the amount of removed lines in this {@code DiffContext}
	 */
	@JsonIgnore
	public int getLinesRemoved() {
		return amountOfLinesWithType(LineType.REMOVED);
	}


}
