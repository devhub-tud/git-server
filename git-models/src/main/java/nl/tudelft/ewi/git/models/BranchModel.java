package nl.tudelft.ewi.git.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * This class is a data class which represents a branch in a Git repository.
 * 
 * @author michael
 */
@Data
public class BranchModel {

	private static final String REFS_REMOTES_ORIGIN = "refs/remotes/origin/";
	
	private String name;
	private String commit;
	
	@JsonIgnore
	public String getSimpleName() {
		if (name.startsWith(REFS_REMOTES_ORIGIN)) {
			return name.substring(REFS_REMOTES_ORIGIN.length());
		}
		return name;
	}

}
