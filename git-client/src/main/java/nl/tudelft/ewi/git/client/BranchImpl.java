package nl.tudelft.ewi.git.client;

import com.google.common.collect.ComparisonChain;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CommitSubList;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.MergeResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class BranchImpl extends Backend implements Branch {

    private final static String MASTER = "master";

    private final BranchModel branchModel;

    public BranchImpl(Client client, String host, BranchModel branchModel) {
        super(client, host);
        this.branchModel = branchModel;
    }

    @Override
    public DiffModel diff() throws GitClientException {
        return perform(new Request<DiffModel>() {
            @Override
            public DiffModel perform(WebTarget target) {
                return target.path(branchModel.getPath())
                        .path("/diff")
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
                return target.path(branchModel.getPath())
                        .path("/diff-blame")
                        .request(MediaType.APPLICATION_JSON)
                        .get(DiffBlameModel.class);
            }
        });
    }

    @Override
    public CommitSubList retrieveCommits(int skip, int limit) throws GitClientException {
        return perform(new Request<CommitSubList>() {
            @Override
            public CommitSubList perform(WebTarget target) {
                return target.path(branchModel.getPath()).path("commits")
                        .queryParam("skip", skip)
                        .queryParam("limit", limit)
                        .request(MediaType.APPLICATION_JSON)
                        .get(CommitSubList.class);
            }
        });
    }

    @Override
    public MergeResponse merge(final String message, final String user, final String email) throws GitClientException {
        return perform(new Request<MergeResponse>() {
            @Override
            public MergeResponse perform(WebTarget target) {
                return target.path(branchModel.getPath()).path("merge")
                        .queryParam("message", message)
                        .queryParam("name", user)
                        .queryParam("email", email)
                        .request(MediaType.APPLICATION_JSON)
                        .post(null, MergeResponse.class);
            }
        });
    }

    @Override
    public String getName() {
        return branchModel.getName();
    }

    @Override
    public Integer getAhead() {
        return branchModel.getAhead();
    }

    @Override
    public Integer getBehind() {
        return branchModel.getBehind();
    }

    @Override
    public boolean isAhead() {
        return branchModel.isAhead();
    }

    @Override
    public boolean isBehind() {
        return branchModel.isBehind();
    }

    @Override
    public CommitModel getCommit() {
        return branchModel.getCommit();
    }

    @Override
    public String getSimpleName() {
        return branchModel.getSimpleName();
    }

    @Override
    public CommitModel mergeBase() throws GitClientException {
        return perform(new Request<CommitModel>() {
            @Override
            public CommitModel perform(WebTarget target) {
                return target.path(branchModel.getPath()).path("merge-base")
                        .request(MediaType.APPLICATION_JSON)
                        .get(CommitModel.class);
            }
        });
    }

    @Override
    public void delete() throws GitClientException {
        perform(new Request<Response>() {
            @Override
            public Response perform(WebTarget target) {
                return target.path(branchModel.getPath())
                        .request(MediaType.APPLICATION_JSON)
                        .delete();
            }
        });
    }

    @Override
    public int compareTo(Branch o) {
        return ComparisonChain.start()
            .compareTrueFirst(getName().contains(MASTER), o.getName().contains(MASTER))
            .compare(getCommit(), o.getCommit())
            .compare(getName(), o.getName())
            .compare(o.getAhead(), getAhead())
            .result();
    }
}
