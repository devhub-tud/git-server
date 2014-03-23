package nl.tudelft.ewi.git.models;

import java.util.Collection;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This model represents a detailed view of a repository in the Gitolite config.
 *
 * @author michael
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DetailedRepositoryModel extends RepositoryModel {

	private Collection<BranchModel> branches;
	
	private Collection<TagModel> tags;
	
	private Collection<CommitModel> recentCommits;
	
}
