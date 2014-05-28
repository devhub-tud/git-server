package nl.tudelft.ewi.git.models;

import com.google.common.collect.Lists;

public class DetailedRepositoryModelFactory {

	public static DetailedRepositoryModel create(CreateRepositoryModel createRepositoryModel) {
		DetailedRepositoryModel model = new DetailedRepositoryModel();
		model.setName(createRepositoryModel.getName());
		model.setPath(createRepositoryModel.getPath());
		model.setPermissions(createRepositoryModel.getPermissions());
		model.setRecentCommits(Lists.<CommitModel> newArrayList());
		model.setTags(Lists.<TagModel> newArrayList());
		model.setUrl(createRepositoryModel.getUrl());
		return model;
	}
	
}
