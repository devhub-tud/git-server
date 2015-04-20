package nl.tudelft.ewi.git.client;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.RepositoryModel;

/**
 * The {@code RepositoriesMock} mocks a {@link Repositories} class
 * 
 * @author Jan-Willem Gmelig Meyling
 */
public class RepositoriesMock implements Repositories {

	private final Map<String, RepositoryMock> mocks;

	public RepositoriesMock() {
		this.mocks = Maps.newHashMap();
	}

	@Override
	public List<RepositoryModel> retrieveAll() throws GitClientException {
		return mocks.values().stream()
			.map(RepositoryMock::getRepositoryModel)
			.collect(Collectors.toList());
	}

	@Override
	public RepositoryMock retrieve(RepositoryModel model) throws GitClientException {
		throw new UnsupportedOperationException("Cannot retrieve Repository by model in mock");
	}

	@Override
	public RepositoryMock retrieve(String name) throws GitClientException {
		RepositoryMock repository =  mocks.get(name);
		if(repository == null) {
			throw new GitClientException("Cannot find repository for name " + name, new NoSuchElementException());
		}
		return repository;
	}

	@Override
	public Repository create(CreateRepositoryModel newRepository) throws GitClientException {
		RepositoryMock repository = new RepositoryMock(this, newRepository);
		repository.createMasterBranch();
		mocks.put(newRepository.getName(), repository);
		return repository;
	}

	public void delete(RepositoryMock mock) {
		mocks.remove(mock.getName());
	}

}
