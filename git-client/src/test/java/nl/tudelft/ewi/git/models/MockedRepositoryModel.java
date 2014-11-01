package nl.tudelft.ewi.git.models;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class MockedRepositoryModel extends DetailedRepositoryModel {

	private List<DetailedCommitModel> commits;
	
	static public MockedRepositoryModel from(CreateRepositoryModel createRepositoryModel) {
		MockedRepositoryModel model = new MockedRepositoryModel();
		model.setName(createRepositoryModel.getName());
		model.setPath(createRepositoryModel.getPath());
		model.setPermissions(createRepositoryModel.getPermissions());
		
		model.setTags(Lists.<TagModel> newArrayList());
		model.setCommits(Lists.<DetailedCommitModel> newArrayList());
		model.setBranches(Lists.<BranchModel> newArrayList());
		
		model.setUrl("ssh://git@localhost:2222/" + createRepositoryModel.getName());
		return model;
	}
	
	public void addCommit(DetailedCommitModel commit) {
		commits.add(commit);
	}
	
	public void addBranch(BranchModel branch) {
		getBranches().add(branch);
	}
	
	public void addTag(TagModel tag) {
		getTags().add(tag);
	}
	
}
