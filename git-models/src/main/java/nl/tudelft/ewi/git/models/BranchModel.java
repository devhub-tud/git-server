package nl.tudelft.ewi.git.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ComparisonChain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This class is a data class which represents a branch in a Git repository.
 * 
 * @author michael
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchModel extends BaseModel implements Comparable<BranchModel> {

	private final static String MASTER = "master";
	
	private String name;
	private CommitModel commit;
	private Integer behind, ahead;

	/**
	 * @return the simple name for this branch
	 */
	@JsonIgnore
	public String getSimpleName() {
		return name.substring(name.lastIndexOf('/') + 1);
	}

	@Override
	public int compareTo(BranchModel o) {
		return ComparisonChain.start()
				.compareTrueFirst(name.contains(MASTER), o.name.contains(MASTER))
				.compare(commit, o.commit)
				.compare(name, o.name)
				.compare(o.ahead, ahead)
				.result();
	}

}
