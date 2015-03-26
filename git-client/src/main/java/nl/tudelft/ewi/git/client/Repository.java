package nl.tudelft.ewi.git.client;

import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.models.TagModel;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * The Repository backend can be used for all git server interactions with a repository
 * @author Jan-Willem Gmelig Meyling
 */
public interface Repository {

    /**
     * Retrieve a branch
     * @param branchName name for the branch
     * @return the Branch
     * @throws GitClientException if an GitClientException occurs
     */
    Branch retrieveBranch(String branchName) throws GitClientException;

    /**
     * Retrieve a commit
     * @param commitId id for the commit
     * @return the commit
     * @throws GitClientException if an GitClientException occurs
     */
    Commit retrieveCommit(String commitId) throws GitClientException;

    /**
     * Set the permissions for this repository
     * @param permissions permissions to set
     * @throws GitClientException if an GitClientException occurs
     */

    void setPermissions(Map<String, RepositoryModel.Level> permissions) throws GitClientException;

    /**
     * List the directory entries
     * @param commitId commit id
     * @param path folder path
     * @return the directory entries
     * @throws GitClientException if an GitClientException occurs
     */
    Map<String, EntryType> listDirectoryEntries(String commitId, String path) throws GitClientException;

    /**
     * Show a file in the repository
     * @param commitId
     * @param path
     * @return the file
     * @throws GitClientException if an GitClientException occurs
     */
    String showFile(String commitId, String path) throws GitClientException;

    /**
     * Show a file in the repository
     * @param commitId
     * @param path
     * @return
     * @throws GitClientException if an GitClientException occurs
     */
    File showBinFile(String commitId, String path) throws GitClientException;

    /**
     * Delete this repository
     * @throws GitClientException if an GitClientException occurs
     */
    void delete() throws GitClientException;

    /**
     * @return the name for this repository
     */
    String getName();

    /**
     * @return the permissions for this repository
     */
    Map<String, RepositoryModel.Level> getPermissions();

    /**
     * @return the git url for this repository
     */
    String getUrl();


    /**
     * @return a list of the branches for this repository
     */
    Collection<BranchModel> getBranches();

    /**
     * @return a list of the tags for this repository
     */
    Collection<TagModel> getTags();

}
