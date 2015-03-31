package nl.tudelft.ewi.git.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Setter;
import nl.tudelft.ewi.git.models.*;

import javax.ws.rs.NotFoundException;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Created by jgmeligmeyling on 31/03/15.
 */
public class RepositoryMock implements Repository {

    private final RepositoriesMock repositoriesMock;
    private final RepositoryModel repositoryModel;
    private final Map<String, BranchMock> branches;

    public RepositoryMock(RepositoriesMock repositoriesMock, RepositoryModel repositoryModel) {
        this.repositoriesMock = repositoriesMock;
        this.repositoryModel = repositoryModel;
        this.branches = Maps.newHashMap();
    }

    @Override
    public BranchMock retrieveBranch(String branchName) throws GitClientException {
        if(branches.containsKey(branchName)) {
            return branches.get(branchName);
        }
        throw new GitClientException("Branch " + branchName + " not found!", new NotFoundException());
    }

    @Override
    public CommitMock retrieveCommit(final String commitId) throws GitClientException {
        try {
            return branches.values().stream()
                .flatMap(BranchMock::getCommitStream)
                .filter(commitMock -> commitMock.getCommit().equals(commitId))
                .findAny().get();
        }
        catch (NoSuchElementException e) {
            throw new GitClientException("Commit " + commitId + " not found!", e);
        }
    }

    @Override
    public void setPermissions(Map<String, RepositoryModel.Level> permissions) {
        repositoryModel.setPermissions(permissions);
    }

    @Setter
    private Map<String, EntryType> listDirectoryEntries;

    @Override
    public Map<String, EntryType> listDirectoryEntries(String commitId, String path) {
        return listDirectoryEntries;
    }

    @Setter
    private String fileContents;

    @Override
    public String showFile(String commitId, String path) throws GitClientException {
        return fileContents;
    }

    @Setter
    private File binFileContents;

    @Override
    public File showBinFile(String commitId, String path) throws GitClientException {
        return binFileContents;
    }

    @Override
    public void delete() {
        repositoriesMock.delete(this);
    }

    @Override
    public String getName() {
        return repositoryModel.getName();
    }

    @Override
    public Map<String, RepositoryModel.Level> getPermissions() {
        return repositoryModel.getPermissions();
    }

    @Override
    public String getUrl() {
        return "ssh://git@localhost:2222/" + getName();
    }

    @Override
    public Collection<BranchModel> getBranches() {
        return branches.values().stream()
            .map(BranchMock::getBranchModel)
            .collect(Collectors.toList());
    }

    @Setter
    private Collection<TagModel> tags = Lists.newArrayList();

    @Override
    public Collection<TagModel> getTags() {
        return tags;
    }

    @Override
    public TagModel tag(TagModel tag) {
        if(tags == null) tags = Lists.newArrayList();
        tags.add(tag);
        return tag;
    }

    public RepositoryModel getRepositoryModel() {
        return repositoryModel;
    }

    public BranchMock createMasterBranch() {
        return createBranch("master");
    }

    public BranchMock createBranch(String name) {
        BranchMock branchMock = new BranchMock(this, name);
        this.branches.put(name, branchMock);
        return branchMock;
    }

}
