package nl.tudelft.ewi.git.backend;

import nl.tudelft.ewi.git.models.BlameModel;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CommitSubList;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.models.TagModel;
import nl.tudelft.ewi.gitolite.git.GitException;
import org.eclipse.jgit.lib.ObjectLoader;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * The {@link RepositoryFacade} combines Git interaction frameworks and maps them to an interface
 * usable by the API. Therefore, the {@code RepositoryFacade} speaks in objects known by the API.
 *
 * The {@code RepositoryFacade} is bound to one repository and should be closed after usage to
 * clean up the resources.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface RepositoryFacade extends AutoCloseable {

	/**
	 * Get a description of this {@link DetailedRepositoryModel}.
	 * @return the {@code DetailedRepositoryModel}.
	 */
	DetailedRepositoryModel getRepositoryModel();

	/**
	 * List the branches for this repository.
	 * @return A list of {@link BranchModel BranchModels}.
	 */
	Collection<BranchModel> getBranches();

	/**
	 * List the tags for this repository.
	 * @return A List of {@link TagModel TagModels}.
	 */
	Collection<TagModel> getTags();

	/**
	 * Get a specific branch.
	 * @param name string that the actual branch name should contain (for example: master).
	 * @return the {@link BranchModel}.
	 */
	BranchModel getBranch(String name);

	/**
	 * Create a new tag.
	 * @param tagModel Tag to add.
	 * @return The created tag.
	 */
	TagModel addTag(TagModel tagModel);

	/**
	 * List the commits in this repository.
	 * @return a List of {@link CommitModel CommitModels}.
	 */
	Collection<CommitModel> listCommits();

	/**
	 * Retrieve a specific commit.
	 * @param commitId Commit id for the commit.
	 * @return The {@link DetailedCommitModel}.
	 */
	DetailedCommitModel retrieveCommit(String commitId);

	/**
	 * Get a list of commits in a branch.
	 * @param branchName string that the actual branch name should contain (for example: master).
	 * @param skip amount of commits to skip.
	 * @param limit amount of commits wanted for the result.
	 * @return a sublist containing commits.
	 */
	CommitSubList getCommitsFor(String branchName, int skip, int limit);

	/**
	 * Get the merge base for a branch (assuming master as other branch).
	 * @param branchName string that the actual branch name should contain (for example: master).
	 * @return the {@link CommitModel} of the merge base.
	 */
	CommitModel mergeBase(String branchName);

	/**
	 * Delete a branch.
	 * @param branchName string that the actual branch name should contain (for example: master).
	 */
	void deleteBranch(String branchName);

	/**
	 * Calculate a diff between two commits.
	 * @param leftCommitId First commit id.
	 * @param rightCommitId Second commit id - may be null.
	 * @param contextLines Amount of context lines around added/removed blocks.
	 * @return A {@link DiffModel} containing the diff response.
	 */
	DiffModel calculateDiff(String leftCommitId, String rightCommitId, int contextLines);

	/**
	 * Calculate the blame for a file at a specific version.
	 * @param commitId Commit id.
	 * @param filePath Path to the file.
	 * @return {@link BlameModel} response.
	 * @throws IOException If an I/O error occurs.
	 * @throws GitException If an GitException occurs.
	 */
	BlameModel blame(String commitId,
	                 String filePath) throws IOException, GitException;

	/**
	 * Add blame data to a {@link DiffModel}.
	 * @param input {@code DiffModel} to transform.
	 * @return a {@link DiffBlameModel} that combines data from the {@link DiffModel} with data from the
	 *    {@link BlameModel BlameModels} of the edited files.
	 * @see #calculateDiff(String, String, int)
	 * @see #blame(String, String)
	 */
	DiffBlameModel addBlameData(DiffModel input);

	/**
	 * List the entries in a folder.
	 * @param commitId Commit id.
	 * @param path Path to the folder.
	 * @return a Map containing folder entries.
	 * @throws GitException If an GitException occurs.
	 * @throws IOException If an I/O error occurs.
	 */
	Map<String, EntryType> showTree(String commitId, String path)  throws GitException, IOException;

	/**
	 * Load a file from a git repository.
	 * @param commitId Commit id.
	 * @param path File path.
	 * @return {@link ObjectLoader} for the object.
	 * @throws IOException If an I/O error occurs.
	 * @throws GitException If an GitException occurs.
	 */
	ObjectLoader showFile(String commitId, String path) throws IOException, GitException;

	@Override
	void close() throws IOException;

}
