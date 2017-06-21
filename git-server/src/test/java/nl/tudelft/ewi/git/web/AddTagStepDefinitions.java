package nl.tudelft.ewi.git.web;

import cucumber.api.PendingException;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.val;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.models.TagModel;
import nl.tudelft.ewi.git.web.api.CommitApi;
import nl.tudelft.ewi.git.web.api.RepositoriesApiImpl;
import nl.tudelft.ewi.git.web.api.RepositoryApiImpl;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.inject.Inject;
import javax.xml.soap.Detail;

/**
 * Created by Sayra Ranjha and Maaike Visser on 21-6-2017.
 */
public class AddTagStepDefinitions {

    public static final String DEFAULT_COMMIT_AUTHOR_NAME = "SayraMaaike";
    public static final String DEFAULT_COMMIT_AUTHOR_EMAIL = "MS@Test";

    @Inject
    private CloneStepDefinitions cloneStepDefinitions;
    @Inject
    private RepositoriesApiImpl repositoriesApi;

    @When("^I tag the commit$")
    public void iTagTheCommit() throws Throwable {


//        CommitApi commitApi = repositoriesApi.getRepository(cloneStepDefinitions.getRepositoryName()).getCommit(cloneStepDefinitions.getCreatedCommitId());
//        CommitModel commitModel = commitApi.get();

        String commitId = repositoriesApi.getRepository(cloneStepDefinitions.getRepositoryName())
                .getBranch("master")
                .getCommit()
                .get()
                .getCommit();

        CommitModel commitModel = repositoriesApi.getRepository(cloneStepDefinitions.getRepositoryName()).getCommit(commitId).get();

        TagModel tagModel = new TagModel();
        tagModel.setName("ourtag");
        tagModel.setCommit(commitModel);
        tagModel.setDescription("This is our tag.");

        repositoriesApi.getRepository(cloneStepDefinitions.getRepositoryName()).addTag(tagModel);
    }

    @Then("^A tag is added to the commit$")
    public void aTagIsAddedToTheCommit() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

}
