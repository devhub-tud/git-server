package nl.tudelft.ewi.git.web;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.models.MergeResponse;
import nl.tudelft.ewi.git.web.api.BranchApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApiImpl;
import org.eclipse.jgit.api.Git;

import javax.inject.Inject;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@ScenarioScoped
public class MergeStepDefinitions {

    public static final String DEFAULT_COMMIT_MESSAGE = "Created file";
    public static final String DEFAULT_COMMIT_AUTHOR_NAME = "Jan-Willem";
    public static final String DEFAULT_COMMIT_AUTHOR_EMAIL = "JW@Test";

    @Inject private CloneStepDefinitions cloneStepDefinitions;
    @Inject private RepositoriesApiImpl repositoriesApi;
    MergeResponse mergeResponse;

    @When("^I merge the branch \"([^\"]*)\" into \"([^\"]*)\"$")
    public void iMergeTheBranchInto(String branchName, String otherBranchName) throws Throwable {
        assertTrue("The branch should be ahead",
            getBranchApi(branchName)
                .get().isAhead());
        mergeResponse = getBranchApi(branchName)
            .merge(
                DEFAULT_COMMIT_MESSAGE,
                DEFAULT_COMMIT_AUTHOR_NAME,
                DEFAULT_COMMIT_AUTHOR_EMAIL
            );
    }

    @Then("^the branch \"([^\"]*)\" is merged into \"([^\"]*)\"$")
    public void theBranchIsMergedInto(String branchName, String otherBranchName) throws Throwable {
        assertTrue("The merge should be successful", mergeResponse.isSuccess());
        assertFalse(
            "The branch should not be ahead",
            getBranchApi(branchName)
                .get().isAhead()
        );
    }

    @Then("^the work folder is clean$")
    public void theWorkFolderIsClean() throws Throwable {
        Git git = cloneStepDefinitions.getGit();

        assertTrue(
            "Working directory should be clean",
            git.status().call().isClean()
        );
    }

    @Then("^the merge fails with an exception$")
    public void theMergeFailsWithAnException() throws Throwable {
        assertFalse("The merge should be successful", mergeResponse.isSuccess());
    }


    private BranchApi getBranchApi(String branchName) {
        return repositoriesApi.getRepository(cloneStepDefinitions.getRepositoryName())
            .getBranch("refs/heads/" + branchName);
    }

}
