package nl.tudelft.ewi.git.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	private List<DiffContext> diffContexts;

	private int amountOfLinesWithType(final DiffLine.Type type) {
		int amount = 0;
		for(DiffContext context : diffContexts)
			amount += context.amountOfLinesWithType(type);
		return amount;
	}

	/**
	 * @return the amount of added lines in this {@code DiffModel}
	 */
	@JsonIgnore
	public int getLinesAdded() {
		return amountOfLinesWithType(DiffLine.Type.ADDED);
	}

	/**
	 * @return the amount of removed lines in this {@code DiffModel}
	 */
	@JsonIgnore
	public int getLinesRemoved() {
		return amountOfLinesWithType(DiffLine.Type.REMOVED);
	}

	@JsonIgnore
	public boolean isDeleted() {
		return type.equals(DiffModel.Type.DELETE);
	}

	@JsonIgnore
	public boolean isAdded() {
		return type.equals(DiffModel.Type.ADD);
	}

	@JsonIgnore
	public boolean isModified() {
		return type.equals(DiffModel.Type.MODIFY);
	}

	@JsonIgnore
	public boolean isCopied() {
		return type.equals(DiffModel.Type.COPY);
	}

	@JsonIgnore
	public boolean isMoved() {
		return type.equals(DiffModel.Type.RENAME);
	}
	
}
