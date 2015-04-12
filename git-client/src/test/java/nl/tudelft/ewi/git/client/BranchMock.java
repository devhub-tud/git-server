package nl.tudelft.ewi.git.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Setter;
import nl.tudelft.ewi.git.models.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by jgmeligmeyling on 31/03/15.
 */
@Data
public class BranchMock implements Branch {

    private final RepositoryMock repositoryMock;
    private final String name;
    private final List<CommitMock> commits;

    public BranchMock(RepositoryMock repositoryMock, String name) {
        this.repositoryMock = repositoryMock;
        this.name = name;
        this.commits = Lists.newArrayList();
    }

    public Stream<CommitMock> getCommitStream() {
        return commits.stream();
    }

    private String commitId;
    private Integer ahead = 0;
    private Integer behind = 0;


    public BranchModel getBranchModel() {
        BranchModel branchModel = new BranchModel();
        branchModel.setName(name);
        branchModel.setAhead(ahead);
        branchModel.setBehind(behind);
        return branchModel;
    }

    @Setter
    private DiffModel diffModel;

    @Override
    public DiffModel diff() throws GitClientException {
        return diffModel;
    }

    @Setter
    private DiffBlameModel diffBlameModel;

    @Override
    public DiffBlameModel diffBlame() throws GitClientException {
        return diffBlameModel;
    }

    @Override
    public CommitSubList retrieveCommits(int skip, int limit) throws GitClientException {
        int size = commits.size();

        List<CommitModel> commitModels = commits.subList(skip, Math.min(size, skip + limit)).stream()
            .map(CommitMock::getCommitModel)
            .collect(Collectors.toList());

        CommitSubList res = new CommitSubList();
        res.setLimit(limit);
        res.setTotal(size);
        res.setCommits(commitModels);
        res.setSkip(skip);
        return  res;
    }

    @Override
    public DetailedCommitModel getCommit() {
        try {
            return repositoryMock.retrieveCommit(commitId).getCommitModel();
        }
        catch (GitClientException e) {
            throw new RuntimeException("Wrong configuration of BranchMock!", e);
        }
    }

    @JsonIgnore
    public String getSimpleName() {
        return name.substring(name.lastIndexOf('/') + 1);
    }

    @Override
    public MergeResponse merge(final String message, final String user, final String email) throws GitClientException {
        MergeResponse response = new MergeResponse();
        response.setSuccess(true);
        response.setStatus("OK");
        return response;
    }

    public CommitMock addCommit(String message, String author) {
        DetailedCommitModel commitModel = new DetailedCommitModel();
        commitModel.setCommit(UUID.randomUUID().toString());
        commitModel.setFullMessage(message);
        commitModel.setTime(System.currentTimeMillis() / 1000);
        commitModel.setAuthor(author);
        commitModel.setParents(new String[0]);
        return addCommit(commitModel);
    }

    public CommitMock addCommit(DetailedCommitModel commitModel) {
        CommitMock commitMock = new CommitMock(commitModel);
        commits.add(commitMock);
        return commitMock;
    }

    @Override
    public int compareTo(Branch o) {
        return 0;
    }
}
