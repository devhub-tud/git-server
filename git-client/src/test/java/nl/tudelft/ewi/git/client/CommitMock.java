package nl.tudelft.ewi.git.client;

import lombok.Data;
import lombok.Setter;
import nl.tudelft.ewi.git.models.*;

/**
 * Created by jgmeligmeyling on 31/03/15.
 */
@Data
public class CommitMock implements Commit {

    private final DetailedCommitModel commitModel;

    public CommitMock(DetailedCommitModel commitModel) {
        this.commitModel = commitModel;
    }

    @Setter
    private DiffModel diffModel;

    @Override
    public DiffModel diff() throws GitClientException {
        return diffModel;
    }

    @Override
    public DiffModel diff(String commitId) throws GitClientException {
        return diffModel;
    }

    @Setter
    private DiffBlameModel diffBlameModel;

    @Override
    public DiffBlameModel diffBlame() throws GitClientException {
        return diffBlameModel;
    }

    @Override
    public DiffBlameModel diffBlame(String commitId) throws GitClientException {
        return diffBlameModel;
    }

    @Setter
    private BlameModel blameModel;

    @Override
    public BlameModel blame(String filePath) throws GitClientException {
        return blameModel;
    }

    @Override
    public String getCommit() {
        return commitModel.getCommit();
    }

    @Override
    public String[] getParents() {
        return commitModel.getParents();
    }

    @Override
    public String getAuthor() {
        return commitModel.getAuthor();
    }

    @Override
    public long getTime() {
        return commitModel.getTime();
    }

    @Override
    public String getTitle() {
        return commitModel.getMessage();
    }

    @Override
    public String getMessage() {
        return commitModel.getMessageTail();
    }

    @Override
    public int compareTo(Commit o) {
        return 0;
    }

    public DetailedCommitModel getCommitModel() {
        return commitModel;
    }

}
