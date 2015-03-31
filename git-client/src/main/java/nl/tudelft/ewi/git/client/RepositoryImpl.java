package nl.tudelft.ewi.git.client;

import nl.tudelft.ewi.git.models.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class RepositoryImpl extends Backend implements Repository {

    private final DetailedRepositoryModel repositoryModel;

    public RepositoryImpl(Client client, String host, DetailedRepositoryModel repositoryModel) {
        super(client, host);
        this.repositoryModel = repositoryModel;
    }

    @Override
    public BranchImpl retrieveBranch(String branchName) {
        return new BranchImpl(client, host, repositoryModel.getBranch(branchName));
    }

    @Override
    public CommitImpl retrieveCommit(String commitId) throws GitClientException {
        return new CommitImpl(client, host, perform(new Request<DetailedCommitModel>() {
            @Override
            public DetailedCommitModel perform(WebTarget target) {
                return target.path(repositoryModel.getPath()).path("commits").path(commitId)
                        .request(MediaType.APPLICATION_JSON)
                        .get(DetailedCommitModel.class);
            }
        }));
    }

    @Override
    public Collection<BranchModel> getBranches() {
        return repositoryModel.getBranches();
    }

    @Override
    public Collection<TagModel> getTags() {
        return repositoryModel.getTags();
    }

    @Override
    public String getName() {
        return repositoryModel.getName();
    }

    @Override
    public void setPermissions(Map<String, RepositoryModel.Level> permissions) throws GitClientException {
        repositoryModel.setPermissions(permissions);
        perform(new Request<Response>() {
            @Override
            public Response perform(WebTarget target) {
                return target.path(repositoryModel.getPath())
                        .request(MediaType.APPLICATION_JSON)
                        .put(Entity.json(repositoryModel));
            }
        });
    }

    @Override
    public Map<String, RepositoryModel.Level> getPermissions() {
        return repositoryModel.getPermissions();
    }

    @Override
    public String getUrl() {
        return repositoryModel.getUrl();
    }

    @Override
    public void delete() throws GitClientException {
        perform(new Request<Response>() {
            @Override
            public Response perform(WebTarget target) {
                return target.path(repositoryModel.getPath())
                        .request()
                        .delete(Response.class);
            }
        });
    }

    @Override
    public Map<String, EntryType> listDirectoryEntries(final String commitId, final String path) throws GitClientException {
        return perform(new Request<Map<String, EntryType>>() {
            @Override
            public Map<String, EntryType> perform(WebTarget target) {
                return target.path(repositoryModel.getPath()).path("tree").path(encode(commitId)).path(encode(path))
                        .request(MediaType.APPLICATION_JSON)
                        .get(new GenericType<Map<String, EntryType>>() {
                        });
            }
        });
    }

    @Override
    public String showFile(final String commitId, final String path) throws GitClientException {
        return perform(new Request<String>() {
            @Override
            public String perform(WebTarget target) {
                return target.path(repositoryModel.getPath()).path("file").path(encode(commitId)).path(encode(path))
                        .request(MediaType.MULTIPART_FORM_DATA)
                        .get(String.class);
            }
        });
    }

    @Override
    public File showBinFile(final String commitId, final String path) throws GitClientException {
        return perform(new Request<File>() {
            @Override
            public File perform(WebTarget target) {
                return target
                        .path(repositoryModel.getPath())
                        .path("file")
                        .path(encode(commitId))
                        .path(encode(path))
                        .request(MediaType.WILDCARD_TYPE)
                        .get(File.class);
            }
        });
    }

    @Override
    public TagModel tag(TagModel tag) throws GitClientException {
        return perform(new Request<TagModel>() {
            @Override
            public TagModel perform(WebTarget target) {
                return target
                        .path(repositoryModel.getPath())
                        .path("tag")
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.json(tag), TagModel.class);
            }
        });
    }
}
