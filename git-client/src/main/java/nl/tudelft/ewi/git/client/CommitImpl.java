package nl.tudelft.ewi.git.client;

import nl.tudelft.ewi.git.models.BlameModel;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.DiffModel;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class CommitImpl extends Backend implements Commit {

    private final DetailedCommitModel commitModel;

    public CommitImpl(Client client, String host, DetailedCommitModel commitModel) {
        super(client, host);
        this.commitModel = commitModel;
    }

    @Override
    public DiffModel diff() throws GitClientException {
        return perform(new Request<DiffModel>() {
            @Override
            public DiffModel perform(WebTarget target) {
                return target.path(commitModel.getPath())
                        .path("/diff")
                        .request(MediaType.APPLICATION_JSON)
                        .get(DiffModel.class);
            }
        });
    }

    @Override
    public DiffModel diff(final String commitId) throws GitClientException {
        return perform(new Request<DiffModel>() {
            @Override
            public DiffModel perform(WebTarget target) {
                return target.path(commitModel.getPath())
                        .path("/diff").path(commitId)
                        .request(MediaType.APPLICATION_JSON)
                        .get(DiffModel.class);
            }
        });
    }

    @Override
    public DiffBlameModel diffBlame() throws GitClientException {
        return perform(new Request<DiffBlameModel>() {
            @Override
            public DiffBlameModel perform(WebTarget target) {
                return target.path(commitModel.getPath())
                        .path("/diff-blame")
                        .request(MediaType.APPLICATION_JSON)
                        .get(DiffBlameModel.class);
            }
        });
    }

    @Override
    public DiffBlameModel diffBlame(final String commitId) throws GitClientException {
        return perform(new Request<DiffBlameModel>() {
            @Override
            public DiffBlameModel perform(WebTarget target) {
                return target.path(commitModel.getPath())
                        .path("/diff-blame").path(commitId)
                        .request(MediaType.APPLICATION_JSON)
                        .get(DiffBlameModel.class);
            }
        });
    }

    @Override
    public BlameModel blame(String filePath) throws GitClientException {
        return perform(new Request<BlameModel>() {
            @Override
            public BlameModel perform(WebTarget target) {
                return target.path(commitModel.getPath())
                        .path("/blame").path(encode(filePath))
                        .request(MediaType.APPLICATION_JSON)
                        .get(BlameModel.class);
            }
        });
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
        return Long.signum(getTime() - o.getTime());
    }

}
