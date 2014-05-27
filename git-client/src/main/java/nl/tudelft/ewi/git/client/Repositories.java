package nl.tudelft.ewi.git.client;

import java.io.File;
import java.util.List;
import java.util.Map;

import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.models.RepositoryModel;

/**
 * This class allows you query and manipulate repositories on the git-server.
 */
public interface Repositories {
	
	/**
	 * @return All currently active {@link RepositoryModel} objects on the git-server.
	 */
	List<RepositoryModel> retrieveAll();

	/**
	 * This method retrieves the specified {@link RepositoryModel} from the git-server.
	 * 
	 * @param model
	 *            The {@link RepositoryModel} to retrieve from the git-server.
	 * @return The retrieved {@link RepositoryModel} object.
	 */
	DetailedRepositoryModel retrieve(RepositoryModel model);

	/**
	 * This mehtod retrieves the specified {@link RepositoryModel} from the git-server.
	 * 
	 * @param name
	 *            The name of the {@link RepositoryModel} to retrieve from the git-server.
	 * @return The retrieved {@link RepositoryModel} object.
	 */
	DetailedRepositoryModel retrieve(String name);

	/**
	 * This method creates a new {@link RepositoryModel} on the git-server.
	 * 
	 * @param newRepository
	 *            The new {@link RepositoryModel} to provision on the git-server.
	 * @return The created {@link RepositoryModel} on the git-server.
	 */
	DetailedRepositoryModel create(CreateRepositoryModel newRepository);

	/**
	 * This method deletes the specified {@link RepositoryModel} from the git-server.
	 * 
	 * @param repository
	 *            The {@link RepositoryModel} to remove from the git-server.
	 */
	void delete(RepositoryModel repository);

	/**
	 * This method lists all commits in the {@link RepositoryModel} on the git-server.
	 * 
	 * @param repository
	 *            The {@link RepositoryModel} to list all commits for.
	 * @return A {@link List} of all {@link CommitModel} object for the specified repository.
	 */
	List<CommitModel> listCommits(RepositoryModel repository);

	/**
	 * This method retrieves a specific commit in the {@link RepositoryModel} on the git-server.
	 * 
	 * @param repository
	 *            The {@link RepositoryModel} to list all commits for.
	 * @param commitId
	 *            The commit to retrieve.
	 * @return A {@link CommitModel} object from the specified repository.
	 */
	CommitModel retrieveCommit(RepositoryModel repository, String commitId);

	/**
	 * This method lists all diffs for the two specified commit IDs.
	 * 
	 * @param repository
	 *            The {@link RepositoryModel} to fetch the diffs for.
	 * @param oldCommitId
	 *            The first commit ID.
	 * @param newCommitId
	 *            The second commit ID.
	 * @return A {@link List} of all {@link DiffModel} objects.
	 */
	List<DiffModel> listDiffs(RepositoryModel repository, String oldCommitId, String newCommitId);

	/**
	 * THis method lists all entries on the specified path of the specified repository at the
	 * specified commit ID.
	 * 
	 * @param repository
	 *            The {@link RepositoryModel} to inspect.
	 * @param commitId
	 *            The commit ID to inspect.
	 * @param path
	 *            The path to list all files and folders of.
	 * @return A {@link List} with files and directory names.
	 */
	public Map<String, EntryType> listDirectoryEntries(RepositoryModel repository, String commitId, String path);

	/**
	 * This method retrieves the contents of a file at the specified commit ID of the repository.
	 * 
	 * @param repository
	 *            The {@link RepositoryModel} to inspect.
	 * @param commitId
	 *            The commit ID to inspect.
	 * @param path
	 *            The path of the file to inspect.
	 * @return The contents of the specified file.
	 */
	public String showFile(RepositoryModel repository, String commitId, String path);
	
	/**
	 * This method retrieves the contents of a binary file at the specified commit ID of the repository.
	 * 
	 * @param repository
	 *            The {@link RepositoryModel} to inspect.
	 * @param commitId
	 *            The commit ID to inspect.
	 * @param path
	 *            The path of the file to inspect.
	 * @return The contents of the specified file.
	 */
	public File showBinFile(RepositoryModel repository, String commitId, String path);
	
}
