package nl.tudelft.ewi.git.models;

import lombok.Data;

import com.google.common.base.Strings;

/**
 * This class is a data class which represents a commit in a Git repository.
 * 
 * @author michael
 */
@Data
public class CommitModel {

	private String commit;
	private String[] parents;
	private String author;
	private long time;
	private String message;
	
	public void setAuthor(String name, String emailAddress) {
		if (Strings.isNullOrEmpty(emailAddress)) {
			setAuthor(name);
		}
		setAuthor(name + " <" + emailAddress + ">");
	}
	
}
