package nl.tudelft.ewi.git.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.ws.rs.NotFoundException;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.DetailedBranchModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.models.MockedRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel;

/**
 * The {@code RepositoriesMock} mocks a {@link Repositories} class
 * @author Jan-Willem
 */
public class RepositoriesMock implements Repositories {
	
	public static final List<DiffModel> EMPTY_DIFF_MODEL = new ArrayList<>();
	public static final Map<String, EntryType> EMPTY_DIRECTORY_ENTRIES = new HashMap<>();
	public static final String DEFAULT_FILE_CONTENTS = "[FILE CONTENTS\nNEWLINE\nANOTHERNEWLINE]";
	
	private final Map<String, MockedRepositoryModel> repositories = new HashMap<>();

	@Override
	public List<RepositoryModel> retrieveAll() {
		return Lists.<RepositoryModel> newArrayList(repositories.values());
	}

	@Override
	public MockedRepositoryModel retrieve(RepositoryModel model) {
		return retrieve(model.getName());
	}

	@Override
	public MockedRepositoryModel retrieve(String name) {
		MockedRepositoryModel model = repositories.get(name);
		if(model == null) {
			throw new NotFoundException("Repository model could not be found: " + name);
		}
		return model;
	}

	@Override
	public MockedRepositoryModel create(CreateRepositoryModel createRepositoryModel) {
		String name = createRepositoryModel.getName();
		MockedRepositoryModel response = MockedRepositoryModel.from(createRepositoryModel);
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
		MockedRepositoryModel mockedRepository = retrieve(repository);
		return Lists.newArrayList(mockedRepository.getCommits());
	}

	@Override
	public CommitModel retrieveCommit(RepositoryModel repository, String commitId) {
		MockedRepositoryModel mockedRepository = retrieve(repository);
		for(CommitModel commit : mockedRepository.getCommits()) {
			if(commit.getCommit().equalsIgnoreCase(commitId)){
				return commit;
			}
		}
		throw new NotFoundException("Commit could not be found " + commitId);
	}
	
	@Override
	public DetailedBranchModel retrieveBranch(RepositoryModel repository, String branchName) {
		return retrieveBranch(repository, branchName, 0, Integer.MAX_VALUE);
	}

	@Override
	public DetailedBranchModel retrieveBranch(final RepositoryModel repository,
			final String branchName, final int skip, final int limit) {
		
		MockedRepositoryModel mockedRepository = retrieve(repository);
		
		for(BranchModel branch : mockedRepository.getBranches()){
			if(branch.getName().contains(branchName)) {
				DetailedBranchModel result = DetailedBranchModel.from(branch);
				
				Set<CommitModel> commitSet = Sets.newTreeSet(new Comparator<CommitModel>() {

					@Override
					public int compare(CommitModel o1, CommitModel o2) {
						return (int) (o2.getTime() - o1.getTime());
					}
					
				});
				
				Queue<CommitModel> queue = Queues.newArrayDeque();
				queue.add(retrieveCommit(repository, result.getCommit()));
				
				while(!queue.isEmpty()) {
					CommitModel commit = queue.poll();
					if(commitSet.add(commit)) {
						for(String parent : commit.getParents()) {
							queue.add(retrieveCommit(repository, parent));
						}
					}
				}
				
				if(skip < 0 || limit < 0 || skip > commitSet.size()) {
					throw new IllegalArgumentException();
				}
				
				List<CommitModel> commitList = Lists.newArrayList(commitSet);
				result.setCommits(commitList.subList(skip, Math.min(skip + limit, commitList.size())));
				result.setAmountOfCommits(commitSet.size());
				return result;
			}
		}
		
		throw new NotFoundException("Branch does not exist!");
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
	
	private Map<String, EntryType> directoryEntries = EMPTY_DIRECTORY_ENTRIES;
	
	public void setDirectoryEntries(Map<String, EntryType> entries) {
		this.directoryEntries = entries;
	}
	
	@Override
	public Map<String, EntryType> listDirectoryEntries(RepositoryModel repository,
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
	
	private File binFile;
	
	public void setBinFile(File file) {
		this.binFile = file;
	}

	@Override
	public File showBinFile(RepositoryModel repository, String commitId, String path) {
		if(binFile == null) {
			throw new NotFoundException();
		}
		return binFile;
	}

}
