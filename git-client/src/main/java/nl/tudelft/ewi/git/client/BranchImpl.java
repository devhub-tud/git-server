package nl.tudelft.ewi.git.client;

import com.google.common.collect.ComparisonChain;
import nl.tudelft.ewi.git.models.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

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
    public MergeResponse merge(final String message) throws GitClientException {
        return perform(new Request<MergeResponse>() {
            @Override
            public MergeResponse perform(WebTarget target) {
                return target.path(branchModel.getPath()).path("merge")
                        .queryParam("message", message)
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
    public CommitModel getCommit() {
        return branchModel.getCommit();
    }

    @Override
    public String getSimpleName() {
        return branchModel.getSimpleName();
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
