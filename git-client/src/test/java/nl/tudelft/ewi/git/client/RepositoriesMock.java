package nl.tudelft.ewi.git.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;

import com.google.common.collect.Lists;

import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.models.TagModel;

/**
 * The {@code RepositoriesMock} mocks a {@link Repositories} class
 * @author Jan-Willem
 */
public class RepositoriesMock implements Repositories {
	
	public static final List<DiffModel> EMPTY_DIFF_MODEL = new ArrayList<>();
	public static final List<String> EMPTY_DIRECTORY_ENTRIES = new ArrayList<>();
	public static final String DEFAULT_FILE_CONTENTS = "[FILE CONTENTS]";
	
	private final Map<String, DetailedRepositoryModel> repositories = new HashMap<>();

	@Override
	public List<RepositoryModel> retrieveAll() {
		return Lists.<RepositoryModel> newArrayList(repositories.values());
	}

	@Override
	public DetailedRepositoryModel retrieve(RepositoryModel model) {
		return retrieve(model.getName());
	}

	@Override
	public DetailedRepositoryModel retrieve(String name) {
		DetailedRepositoryModel model = repositories.get(name);
		if(model == null) {
			throw new NotFoundException("Repository model could not be found: " + name);
		}
		return model;
	}

	@Override
	public DetailedRepositoryModel create(CreateRepositoryModel newRepository) {
		String name = newRepository.getName();
		
		DetailedRepositoryModel response = new DetailedRepositoryModel();
		response.setName(name);
		response.setPermissions(newRepository.getPermissions());
		response.setBranches(new ArrayList<BranchModel>());
		response.setRecentCommits(new ArrayList<CommitModel>());
		response.setTags(new ArrayList<TagModel>());
		repositories.put(name, response);
		
		return response;
	}
	
	@Override
	public void delete(RepositoryModel repository) {
		// Ensure that the repository exists, or throw an exception
		retrieve(repository);
		repositories.remove(repository.getName());
	}

	@Override
	public List<CommitModel> listCommits(RepositoryModel repository) {
		DetailedRepositoryModel detailedRepository = retrieve(repository);
		return Lists.newArrayList(detailedRepository.getRecentCommits());
	}

	@Override
	public CommitModel retrieveCommit(RepositoryModel repository, String commitId) {
		DetailedRepositoryModel detailedRepository = retrieve(repository);
		for(CommitModel commit : detailedRepository.getRecentCommits()) {
			if(commit.getCommit().equalsIgnoreCase(commitId)){
				return commit;
			}
		}
		throw new NotFoundException("Commit could not be found " + commitId);
	}
	
	private List<DiffModel> listDiffs = EMPTY_DIFF_MODEL;
	
	public void setListDiffs(List<DiffModel> listDiffs) {
		this.listDiffs = listDiffs;
	}

	@Override
	public List<DiffModel> listDiffs(RepositoryModel repository,
			String oldCommitId, String newCommitId) {
		return listDiffs;
	}
	
	private List<String> directoryEntries = EMPTY_DIRECTORY_ENTRIES;
	
	public void setDirectoryEntries(List<String> entries) {
		this.directoryEntries = entries;
	}
	
	@Override
	public List<String> listDirectoryEntries(RepositoryModel repository,
			String commitId, String path) {
		return directoryEntries;
	}
	
	private String file = DEFAULT_FILE_CONTENTS;
	
	public void setFile(String file) {
		this.file = file;
	}

	@Override
	public String showFile(RepositoryModel repository, String commitId,
			String path) {
		return file;
	}

}
