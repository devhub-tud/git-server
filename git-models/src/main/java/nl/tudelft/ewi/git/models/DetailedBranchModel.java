package nl.tudelft.ewi.git.models;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This model represents a detailed view of a repository in the Gitolite config.
 * In addition to the {@link BranchModel} this model also provides a (possibly
 * filtered) set of commits.
 * 
 * @author Jan-Willem
 *
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class DetailedBranchModel extends BranchModel {
	
	private Collection<CommitModel> commits;
	
	private int amountOfCommits;
	
	/**
	 * Construct a {@code DetailedBranchModel} based on a {@link BranchModel}
	 * 
	 * @param model 
	 * @return a {@code DetailedBranchModel}
	 */
	static public DetailedBranchModel from(BranchModel model) {
		DetailedBranchModel result = new DetailedBranchModel();
		result.setName(model.getName());
		result.setCommit(model.getCommit());
		return result;
	}
	
}
