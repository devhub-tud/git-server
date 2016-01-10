package nl.tudelft.ewi.git.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchModel implements Comparable<BranchModel> {

	/**
	 * Master branch ref.
	 */
	private final static String MASTER = "master";

	/**
	 * The name for the branch.
	 */
	private String name;

	/**
	 * The commit for the branch.
	 */
	private CommitModel commit;

	/**
	 * Number of commits that this branch is behind.
	 */
	private @JsonProperty("behind") Integer behind;

	/**
	 * Number of commits that this branch is behind.
	 */
	private @JsonProperty("ahead") Integer ahead;

	/**
	 * @return true if this branch is ahead.
	 */
	@JsonIgnore
	public boolean isAhead() {
		return ahead > 0;
	}

	/**
	 * @return true if this branch is behind.
	 */
	@JsonIgnore
	public boolean isBehind() {
		return behind > 0;
	}

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
