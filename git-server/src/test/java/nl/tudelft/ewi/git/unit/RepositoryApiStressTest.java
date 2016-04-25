package nl.tudelft.ewi.git.unit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import nl.tudelft.ewi.git.models.CreateRepositoryModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.models.RepositoryModel.Level;
import nl.tudelft.ewi.git.web.CucumberModule;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Jan-Willem Gmelig Meyling
 * @author Liam Clark
 */
@Slf4j
@RunWith(JukitoRunner.class)
@UseModules(CucumberModule.class)
public class RepositoryApiStressTest {

	static final int NUM_THREADS = 4;
	@Inject volatile RepositoriesApi repositoriesApi;
	DetailedRepositoryModel detailedRepositoryModel;
	ListeningExecutorService executorService;
	CountDownLatch countDownLatch;

	@Before
	public void setUp() throws GitAPIException, IOException {
		val crm = new CreateRepositoryModel();
		crm.setTemplateRepository("https://github.com/SERG-Delft/jpacman-template.git");
		crm.setName("stress-test");
		crm.setPermissions(ImmutableMap.of("me", Level.ADMIN));
		detailedRepositoryModel = repositoriesApi.createRepository(crm);

		executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(NUM_THREADS));
		countDownLatch = new CountDownLatch(NUM_THREADS);
	}

	@Test
	public void testGitBlameWithSubmodule() throws Exception {
		List<ListenableFuture<?>> futures = IntStream.range(0, NUM_THREADS)
			.<Runnable> mapToObj((a) -> this::initiateDanger)
			.map(executorService::submit)
			.collect(Collectors.toList());

		Futures.allAsList(futures).get();
	}

	@SneakyThrows
	public void initiateDanger() {
		countDownLatch.countDown();
		countDownLatch.await();

		String startId = repositoriesApi.getRepository(detailedRepositoryModel.getName())
			.getBranch("master")
			.getCommit()
			.get()
			.getCommit();

		walkCommitTree(startId);
		walkFileTree(startId, "");
	}

	public void walkFileTree(String commitId, String path) throws IOException {
		Map<String, EntryType> tree = repositoriesApi.getRepository(detailedRepositoryModel.getName())
			.getCommit(commitId)
			.showTree(path);

		path = path.isEmpty() ? path : path + '/';

		for (Entry<String ,EntryType> entry : tree.entrySet()) {
			switch (entry.getValue()) {
				case FOLDER:
					walkFileTree(commitId, path + entry.getKey().substring(0, entry.getKey().length() - 1));
					break;
				case TEXT:
					readFile(commitId, path + entry.getKey());
					break;
			}
		}
	}

	public void readFile(String commitId, String path) throws IOException {
		repositoriesApi.getRepository(detailedRepositoryModel.getName())
            .getCommit(commitId)
            .showTextFile(path);
	}

	public void walkCommitTree(String startId) {
		Set<String> commitIds = Sets.newHashSet();
		Queue<String> toVisit = Queues.newArrayDeque();
		toVisit.add(startId);
		while (!toVisit.isEmpty()) {
			String commitId = toVisit.remove();
			if (commitIds.add(commitId)) {
				toVisit.addAll(
					getParents(commitId)
				);
			}
		}
	}

	public List<String> getParents(String commitId) {
		return Arrays.asList(
            repositoriesApi.getRepository(detailedRepositoryModel.getName())
                .getCommit(commitId)
                .get().getParents()
        );
	}

	@After
	public void terminateExecutor() {
		executorService.shutdownNow();
	}

}
