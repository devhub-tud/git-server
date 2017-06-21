package nl.tudelft.ewi.git.web;

import cucumber.api.PendingException;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import cucumber.runtime.java.guice.ScenarioScoped;
import lombok.Getter;
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
import org.hamcrest.Matchers;

import javax.inject.Inject;
import javax.xml.soap.Detail;


import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;

/**
 * Created by Sayra Ranjha and Maaike Visser on 21-6-2017.
 */
@ScenarioScoped
public class AddTagStepDefinitions {

    public static final String DEFAULT_COMMIT_AUTHOR_NAME = "SayraMaaike";
    public static final String DEFAULT_COMMIT_AUTHOR_EMAIL = "MS@Test";

    @Inject
    private CloneStepDefinitions cloneStepDefinitions;
    @Inject
    private RepositoriesApiImpl repositoriesApi;

    @Getter
    private TagModel tagModel;

    @When("^I tag the commit$")
    public void iTagTheCommit() throws Throwable {
        String commitId = repositoriesApi
                .getRepository(cloneStepDefinitions.getRepositoryName())
                .getBranch("master")
                .getCommit()
                .get()
                .getCommit();

        CommitModel commitModel = repositoriesApi
                .getRepository(cloneStepDefinitions.getRepositoryName())
                .getCommit(commitId)
                .get();

        tagModel = new TagModel();
        tagModel.setName("ourtag");
        tagModel.setCommit(commitModel);
        tagModel.setDescription("This is our tag.");

        repositoriesApi.getRepository(cloneStepDefinitions.getRepositoryName())
                .addTag(tagModel);
    }

    @Then("^A tag is added to the commit$")
    public void aTagIsAddedToTheCommit() throws Throwable {
        Collection<TagModel> tags = repositoriesApi.getRepository(cloneStepDefinitions.getRepositoryName()).getTags();

        assertThat(tags, contains(
                hasProperty("name", containsString(tagModel.getName()))));

    }
}
