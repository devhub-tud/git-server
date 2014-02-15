package nl.tudelft.ewi.git.web.models;

import java.util.Collection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.git.inspector.Branch;
import nl.tudelft.ewi.git.inspector.Commit;
import nl.tudelft.ewi.git.inspector.Tag;

/**
 * This model represents a detailed view of a repository in the Gitolite config.
 *
 * @author michael
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DetailedRepositoryModel extends RepositoryModel {

	private Collection<Branch> branches;
	
	private Collection<Tag> tags;
	
	private Collection<Commit> recentCommits;
	
}
