package nl.tudelft.ewi.git.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;

import javax.validation.constraints.NotNull;

/**
 * This class is a data class which represents a commit in a Git repository.
 * 
 * @author michael
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommitModel extends BaseModel implements Comparable<CommitModel> {

	@NotNull
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

	@Override
	public int compareTo(CommitModel o) {
		return Long.signum(o.time - time);
	}

}
