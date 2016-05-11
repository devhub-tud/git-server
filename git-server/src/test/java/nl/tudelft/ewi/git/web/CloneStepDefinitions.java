package nl.tudelft.ewi.git.web;

import com.google.common.io.Files;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.runtime.java.guice.ScenarioScoped;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApiImpl;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;

import javax.inject.Inject;
import java.io.File;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@ScenarioScoped
public class CloneStepDefinitions {

    public static final String DEFAULT_COMMIT_MESSAGE = "Created file";
    public static final String DEFAULT_COMMIT_AUTHOR_NAME = "Jan-Willem";
    public static final String DEFAULT_COMMIT_AUTHOR_EMAIL = "JW@Test";
    public static final String REMOTE_ORIGIN = "origin";
    public static final String MASTER_BRANCH_NAME = "master";
    @Inject
    private RepositoriesApiImpl repositoriesApi;

    @Getter
    private File workFolder;

    @Getter
    private Git git;

    @Getter
    private String repositoryName;

    @Given("^I clone repository \"([^\"]*)\"$")
    public void iCloneRepository(String name) throws Throwable {
        workFolder = Files.createTempDir();
        RepositoryModel repositoryModel = repositoriesApi.getRepository(name)
            .getRepositoryModel();

        repositoryName = name;
        git = Git.init().setBare(false).setDirectory(workFolder).call();

        RemoteAddCommand addCommand = git.remoteAdd();
        addCommand.setName(REMOTE_ORIGIN);
        addCommand.setUri(new URIish(repositoryModel.getUrl() + ".git"));
        addCommand.call();

        FetchResult fetchResult = git.fetch().setRemote(REMOTE_ORIGIN).call();

        if (!fetchResult.getAdvertisedRefs().isEmpty()) {
            git.pull().setRemote(REMOTE_ORIGIN).setRemoteBranchName(MASTER_BRANCH_NAME).call();
        }

    }

    @Given("^I create the file \"([^\"]*)\" with the contents$")
    public void iCreateTheFileWithTheContents(String name, String contents) throws Throwable {
        Files.write(contents.getBytes(), new File(workFolder, name));
    }

    @Given("^I have added \"([^\"]*)\" to the index$")
    public void iHaveAddedToTheIndex(String name) throws Throwable {
        git.add().addFilepattern(name).call();
    }

    @Given("^I committed the result$")
    public void iCommittedTheResult() throws Throwable {
        git.commit().setMessage(DEFAULT_COMMIT_MESSAGE)
            .setAuthor(DEFAULT_COMMIT_AUTHOR_NAME, DEFAULT_COMMIT_AUTHOR_EMAIL)
            .setCommitter(DEFAULT_COMMIT_AUTHOR_NAME, DEFAULT_COMMIT_AUTHOR_EMAIL)
            .call();
    }

    @Given("^I push the commit to \"([^\"]*)\"$")
    public void iPushTheCommitTo(String name) throws Throwable {
        git.push().setRemote(REMOTE_ORIGIN)
            .setForce(true)
            .setRefSpecs(new RefSpec(name+":"+name))
            .call();
    }

    @And("^I checkout branch \"([^\"]*)\"$")
    public void iCheckoutBranch(String name) throws Throwable {
        git.checkout().setForce(true)
            .setName(name).call();
    }

    @And("^I checkout a new branch \"([^\"]*)\"$")
    public void iCheckoutANewBranch(String name) throws Throwable {
        git.branchRename().setNewName(name).call();
    }
}
