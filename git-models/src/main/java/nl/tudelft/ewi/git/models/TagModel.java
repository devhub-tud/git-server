package nl.tudelft.ewi.git.models;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * This class is a data class which represents a tag in a Git repository.
 * 
 * @author michael
 */
@Data
public class TagModel implements Comparable<TagModel> {

	@NotEmpty
	private String name;
	
	private DetailedCommitModel commit;
	private String description;
	
	@Override
	public int compareTo(TagModel o) {
		return commit.compareTo(o.commit);
	}
	
	@JsonIgnore
	public String getSimpleName() {
		return name.substring(name.lastIndexOf('/') + 1);
	}
	
	public String getDescription(){
		return description != null ? description : commit.getFullMessage();
	}
	
}
