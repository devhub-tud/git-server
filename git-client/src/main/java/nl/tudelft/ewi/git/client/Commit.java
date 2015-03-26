package nl.tudelft.ewi.git.client;

import nl.tudelft.ewi.git.models.BlameModel;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.DiffModel;

/**
 * The Commit backend can be used for all git server interactions with a commit
 * @author Jan-Willem Gmelig Meyling
 */
public interface Commit extends Comparable<Commit> {

    /**
     * Fetch the diff for this commit
     * @return the diffs
     * @throws GitClientException if an GitClientException occurs
     */
    DiffModel diff() throws GitClientException;

    /**
     * Compare this commit to another commit
     * @param commitId another commit
     * @return the diff between this commit and another commit
     * @throws GitClientException if an GitClientException occurs
     */
    DiffModel diff(String commitId) throws GitClientException;

    /**
     * Fetch the diffs for this commit with blame information
     * @return the diffs
     * @throws GitClientException if an GitClientException occurs
     */
    DiffBlameModel diffBlame() throws GitClientException;

    /**
     * Compare this commit to another commit
     * @param commitId another commit
     * @return the diff between this commit and another commit with blame information
     * @throws GitClientException if an GitClientException occurs
     */
    DiffBlameModel diffBlame(String commitId) throws GitClientException;

    /**
     * Blame a file
     * @param filePath path for the file
     * @return BlameModel for the file
     * @throws GitClientException if an GitClientException occurs
     */
    BlameModel blame(String filePath) throws  GitClientException;

    /**
     * The commit id for this commit
     * @return commit id
     */
    String getCommit();

    /**
     * The parent ids for this commit
     * @return the parent ids
     */
    String[] getParents();

    /**
     * The author for this commit
     * @return the author
     */
    String getAuthor();

    /**
     * The time for this commit
     * @return the time for this commit
     */
    long getTime();

    /**
     * Commit title
     * @return the commit title
     */
    String getTitle();

    /**
     * Commit message
     * @return the commit message
     */
    String getMessage();
}
