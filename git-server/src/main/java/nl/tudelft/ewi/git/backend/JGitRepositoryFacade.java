package nl.tudelft.ewi.git.backend;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.git.inspector.DiffContextFormatter;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffContext;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffFile;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffLine;
import nl.tudelft.ewi.git.models.BlameModel;
import nl.tudelft.ewi.git.models.BlameModel.BlameBlock;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.CommitSubList;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.DiffBlameModel.DiffBlameLine;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.models.TagModel;
import nl.tudelft.ewi.git.web.api.Transformers;
import nl.tudelft.ewi.gitolite.git.GitException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.ObjectStream;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Implementation for {@link RepositoryFacade} using JGit.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class JGitRepositoryFacade implements RepositoryFacade {

	public static final String REF_MASTER = "master";

	private final Transformers transformers;

	@Getter
	private final Git git;

	@Getter
	private final Repository repo;

	private final nl.tudelft.ewi.gitolite.repositories.Repository repository;

	private final AtomicInteger uses = new AtomicInteger(1);

	private final AtomicBoolean closed = new AtomicBoolean(false);

	public JGitRepositoryFacade(Transformers transformers, nl.tudelft.ewi.gitolite.repositories.Repository repository) throws IOException {
		Preconditions.checkNotNull(repository);
		this.transformers = transformers;
		this.git = Git.open(repository.getPath().toFile());
		this.repo = git.getRepository();
		this.repository = repository;
	}

	void incrementUses() {
		uses.incrementAndGet();
	}

	public boolean isClosed() {
		return closed.get();
	}

	@Override
	public DetailedRepositoryModel getRepositoryModel() {
		DetailedRepositoryModel model = new DetailedRepositoryModel();
		transformers.setBaseAttributes(repository, model);
		model.setBranches(getBranches());
		model.setTags(getTags());
		return model;
	}

	@Override
	public Collection<BranchModel> getBranches() {
		try {
			return git.branchList()
				.setListMode(ListMode.ALL)
				.call().stream()
				.map(this::transformToBranchModel)
				.collect(Collectors.toList());
		}
		catch (GitAPIException e) {
			throw new GitException(e.getMessage(), e);
		}
	}

	@Override
	public Collection<TagModel> getTags() {
		try {
			return git.tagList()
				.call().stream()
				.map(this::transformTagModel)
				.collect(Collectors.toList());
		}
		catch (GitAPIException e) {
			throw new GitException(e.getMessage(), e);
		}
	}

	@Override
	public BranchModel getBranch(String name) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
		try {
			return git.branchList()
				.setListMode(ListMode.ALL)
//				.setContains(name)
				.call().stream()
				.filter(branch -> branch.getName().endsWith(name))
				.map(this::transformToBranchModel)
				.findFirst()
				.get();
		}
		catch (NoSuchElementException | RefNotFoundException e) {
			throw new NotFoundException(e.getMessage(), e);
		}
		catch (GitAPIException e) {
			throw new GitException(e.getMessage(), e);
		}
	}

	@Override
	public TagModel addTag(TagModel tagModel) {
		try(RevWalk walk = new RevWalk(repo)) {

			String commitId = tagModel.getCommit().getCommit();
			ObjectId id = repo.resolve(commitId);
			RevCommit commit = walk.parseCommit(id);

			TagCommand command = git.tag()
				.setName(tagModel.getName())
				.setObjectId(commit);

			if (tagModel.getDescription() != null)
				command.setMessage(tagModel.getDescription());

			return transformTagModel(command.call());
		}
		catch (MissingObjectException e) {
			throw new NotFoundException(e.getMessage(), e);
		}
		catch (IOException | GitAPIException e) {
			throw new GitException(e);
		}
	}

	@Override
	public Collection<CommitModel> listCommits() {
		try {
			Iterable<RevCommit> revCommits = git.log()
				.all()
				.setMaxCount(100)
				.call();
			return StreamSupport.stream(revCommits.spliterator(), false)
				.map(this::transformDetailedCommitModel)
				.collect(Collectors.toList());
		}
		catch (IOException | GitAPIException e) {
			throw new GitException(e);
		}
	}

	protected TagModel transformTagModel(Ref input) {
		TagModel tag = new TagModel();

		tag.setName(input.getName());

		input = repo.peel(input);
		ObjectId objectId = input.getPeeledObjectId();
		if (objectId == null) {
			objectId = input.getObjectId();
		}
		else {
			try(RevWalk revWalk = new RevWalk(repo)) {
				RevTag annotatedTag = revWalk.parseTag(input.getObjectId());
				tag.setDescription(annotatedTag.getShortMessage());
			}
			catch(IOException e) {
				log.warn("Failed to read tag message", e);
			}
		}

		try {
			tag.setCommit(retrieveCommit(objectId.getName()));
		}
		catch (GitException e) {
			log.warn("Failed to fetch commit" + objectId, e);
		}

		return tag;
	}

	@Override
	public DetailedCommitModel retrieveCommit(String commitId) {
		Preconditions.checkNotNull(commitId);

		try(RevWalk walk = new RevWalk(repo)) {
			RevCommit revCommit = walk.parseCommit(repo.resolve(commitId));
			return transformDetailedCommitModel(revCommit);
		}
		catch (MissingObjectException e) {
			throw new NotFoundException(e.getMessage(), e);
		}
		catch (IOException e) {
			throw new GitException(e.getMessage(), e);
		}
	}

	protected BranchModel transformToBranchModel(Ref input) {
		BranchModel branch = new BranchModel();
		String name = input.getName();
		branch.setName(name);

		try(RevWalk walk = new RevWalk(repo)) {
			RevCommit localCommit = walk.parseCommit(input.getObjectId());
			branch.setCommit(transformDetailedCommitModel(localCommit));
			determineAheadBehindCounts(input, branch, walk, localCommit);
		}
		catch (IOException e) {
			log.warn("Failed to calculate branch tracking status", e);
		}

		return branch;
	}

	protected void determineAheadBehindCounts(Ref input, BranchModel branch, RevWalk walk, RevCommit localCommit) throws IOException {
		Ref master = repo.getRef(REF_MASTER);
		int ahead = 0, behind = 0;

		if(!input.equals(master)){
			RevCommit trackingCommit = walk.parseCommit(master.getObjectId());

			walk.setRevFilter(RevFilter.MERGE_BASE);
			walk.markStart(localCommit);
			walk.markStart(trackingCommit);
			RevCommit mergeBase = walk.next();

			walk.reset();
			walk.setRevFilter(RevFilter.ALL);
			ahead = RevWalkUtils.count(walk, localCommit, mergeBase);
			behind = RevWalkUtils.count(walk, trackingCommit, mergeBase);
		}

		branch.setAhead(ahead);
		branch.setBehind(behind);
	}

	protected <T extends CommitModel> T setBasicCommitModelProperties(RevCommit revCommit, T commit) {
		PersonIdent committerIdent = revCommit.getCommitterIdent();
		ObjectId revCommitId = revCommit.getId();

		RevCommit[] parents = revCommit.getParents();
		String[] parentIds = new String[parents.length];
		for (int i = 0; i < parents.length; i++) {
			ObjectId parentId = parents[i].getId();
			parentIds[i] = parentId.getName();
		}

		commit.setCommit(revCommitId.getName());
		commit.setParents(parentIds);
		commit.setTime(revCommit.getCommitTime());
		commit.setAuthor(committerIdent.getName(), committerIdent.getEmailAddress());
		commit.setMessage(revCommit.getShortMessage());
		return commit;
	}

	@Deprecated
	protected CommitModel transformCommitModel(RevCommit revCommit) {
		return setBasicCommitModelProperties(revCommit, new CommitModel());
	}

	protected DetailedCommitModel transformDetailedCommitModel(RevCommit revCommit) {
		DetailedCommitModel commit = setBasicCommitModelProperties(revCommit, new DetailedCommitModel());
		commit.setFullMessage(revCommit.getFullMessage());
		return commit;
	}

	@Override
	public void close() {
		if (uses.decrementAndGet() == 0) {
			repo.close();
			git.close();
			closed.set(true);
		}
	}

	@Override
	public CommitSubList getCommitsFor(String branchName, int skip, int limit) {
		try {
			Iterable<RevCommit> allCommits = git.log()
				.add(repo.resolve(branchName))
				.call();

			Iterable<RevCommit> commitsInRange = git.log()
				.add(repo.resolve(branchName))
				.setSkip(skip)
				.setMaxCount(limit)
				.call();

			CommitSubList commitSubList = new CommitSubList();
			commitSubList.setSkip(skip);
			commitSubList.setLimit(limit);
			commitSubList.setTotal(Iterables.size(allCommits));
			commitSubList.setCommits(Lists.newArrayList(Iterables.transform(commitsInRange, this::transformDetailedCommitModel)));
			return commitSubList;
		}
		catch (MissingObjectException e) {
			throw new NotFoundException(e.getMessage(), e);
		}
		catch (IOException | GitAPIException e) {
			throw new GitException(e);
		}
	}

	@Override
	public CommitModel mergeBase(String branchName) {
		try(RevWalk walk = new RevWalk(git.getRepository())) {
			walk.setRevFilter(RevFilter.MERGE_BASE);
			walk.markStart(walk.lookupCommit(repo.resolve(REF_MASTER)));
			walk.markStart(walk.lookupCommit(repo.resolve(branchName)));
			RevCommit mergeBase = walk.next();
			return transformDetailedCommitModel(mergeBase);
		}
		catch (MissingObjectException e) {
			throw new NotFoundException(e.getMessage(), e);
		}
		catch (IOException e) {
			throw new GitException(e.getMessage(), e);
		}
	}

	@Override
	public void deleteBranch(String branchName) {
		try {
			git.branchDelete()
				.setBranchNames(branchName)
				.setForce(true)
				.call();
		}
		catch (GitAPIException e) {
			throw new GitException(e.getMessage(), e);
		}
	}

	@Override
	public DiffModel calculateDiff(String leftCommitId, String rightCommitId, int contextLines) {
		Preconditions.checkNotNull(rightCommitId);

		StoredConfig config = repo.getConfig();
		config.setString("diff", null, "algorithm", "histogram");

		try(ObjectReader objectReader = repo.newObjectReader()) {
			DiffModel diffModel = new DiffModel();

			AbstractTreeIterator oldTreeIter = new EmptyTreeIterator();
			if (!Strings.isNullOrEmpty(leftCommitId)) {
				oldTreeIter = createTreeParser(leftCommitId);
				diffModel.setOldCommit(retrieveCommit(leftCommitId));
			}

			AbstractTreeIterator newTreeIter = createTreeParser(rightCommitId);
			diffModel.setNewCommit(retrieveCommit(rightCommitId));

			List<DiffEntry> diffs = git.diff()
				.setContextLines(contextLines)
				.setOldTree(oldTreeIter)
				.setNewTree(newTreeIter)
				.call();

			RenameDetector rd = new RenameDetector(repo);
			rd.addAll(diffs);
			diffs = rd.compute();

			List<DiffFile<DiffContext<DiffLine>>> diffFiles = diffs.stream()
				.filter(diffEntry -> isBlob(objectReader, diffEntry))
				.map(this::transformToDiffFile)
				.collect(Collectors.toList());

			List<CommitModel> commitModels = commitDifference(rightCommitId, leftCommitId).stream()
				.map(this::transformDetailedCommitModel)
				.collect(Collectors.toList());

			diffModel.setCommits(commitModels);
			diffModel.setDiffs(diffFiles);
			return diffModel;
		}
		catch (IOException | GitAPIException e) {
			throw new GitException(e);
		}
	}

	@SneakyThrows
	private boolean isBlob(ObjectReader objectReader, DiffEntry diffEntry) {

		ObjectId objectId = diffEntry.getNewId().toObjectId().name().equals("0000000000000000000000000000000000000000") ?
				diffEntry.getOldId().toObjectId(): 
				diffEntry.getNewId().toObjectId();	
		return objectReader.has(objectId);
	}

	protected DiffFile<DiffContext<DiffLine>> transformToDiffFile(DiffEntry input) {
		DiffFile<DiffContext<DiffLine>>  diff = new DiffFile<>();
		diff.setType(convertChangeType(input.getChangeType()));
		diff.setOldPath(input.getOldPath());
		diff.setNewPath(input.getNewPath());

		DiffContextFormatter formatter = new DiffContextFormatter(diff, repo);

		try {
			formatter.format(input);
		}
		catch (IOException e) {
			log.warn(e.getMessage(), e);
		}

		return diff;
	}

	protected nl.tudelft.ewi.git.models.ChangeType convertChangeType(ChangeType changeType) {
		switch (changeType) {
			case ADD:
				return nl.tudelft.ewi.git.models.ChangeType.ADD;
			case COPY:
				return nl.tudelft.ewi.git.models.ChangeType.COPY;
			case DELETE:
				return nl.tudelft.ewi.git.models.ChangeType.DELETE;
			case MODIFY:
				return nl.tudelft.ewi.git.models.ChangeType.MODIFY;
			case RENAME:
				return nl.tudelft.ewi.git.models.ChangeType.RENAME;
			default:
				throw new IllegalArgumentException("Cannot convert change type: " + changeType);
		}
	}

	protected CanonicalTreeParser createTreeParser(String ref) throws IOException, GitAPIException {
		assert ref != null && !ref.isEmpty() : "Ref should not be empty or null";

		RevCommit commit;
		try(RevWalk walk = new RevWalk(repo)) {
			commit = walk.parseCommit(repo.resolve(ref));

			RevTree commitTree = commit.getTree();
			RevTree tree = walk.parseTree(commitTree.getId());

			CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
			try(ObjectReader oldReader = repo.newObjectReader()) {
				oldTreeParser.reset(oldReader, tree.getId());
			}

			return oldTreeParser;
		}
		catch (MissingObjectException e) {
			throw new NotFoundException(e.getMessage(), e);
		}
	}

	protected List<RevCommit> commitDifference(String startRef, String endRef) throws IOException {
		assert !Strings.isNullOrEmpty(startRef) : "Ref should not be empty or null";

		try(RevWalk walk = new RevWalk(repo)) {
			RevCommit start = walk.parseCommit(repo.resolve(startRef));
			RevCommit end = endRef == null ? null : walk.parseCommit(repo.resolve(endRef));
			return RevWalkUtils.find(walk, start, end);
		}
	}

	@Override
	public BlameModel blame(String commitId,
	                        String filePath) throws IOException, GitException {

		Preconditions.checkNotNull(commitId);
		Preconditions.checkNotNull(filePath);

		try {
			BlameResult blameResult = git.blame()
				.setStartCommit(repo.resolve(commitId))
				.setFilePath(filePath)
				.setFollowFileRenames(true)
				.call();

			if(blameResult == null) {
				throw new NotFoundException(String.format("%s not found in %S at %s", filePath,
					repository.toString(), commitId));
			}

			return transformBlameModel(blameResult, commitId, filePath);
		}
		catch (JGitInternalException | GitAPIException e) {
			Throwable cause = e.getCause();
			if(MissingObjectException.class.isInstance(cause)) {
				throw new NotFoundException(cause.getMessage(), e);
			}
			throw new GitException(e);
		}
	}

	protected BlameModel transformBlameModel(BlameResult input, String commitId, String path) {
		final BlameModel model = new BlameModel();
		model.setCommitId(commitId);
		model.setPath(path);
		final List<BlameBlock> blames = Lists.<BlameBlock> newArrayList();

		for(int i = 0, length = input.getResultContents().size(); i < length; i++) {
			BlameBlock block = new BlameBlock();
			block.setFromCommitId(input.getSourceCommit(i).getName());
			block.setSourceFrom(input.getSourceLine(i) + 1);
			block.setDestinationFrom(i + 1);
			block.setFromFilePath(input.getSourcePath(i));
			block.setLength(1);

			for(int j = i + 1; j < length &&
				equalCommitId(input, j, block) &&
				nextLineNumberInBlock(input, j, block); j++) {
				i++;
				block.incrementLength();
			}

			blames.add(block);
		}

		model.setBlames(blames);
		return model;
	}

	private static boolean equalCommitId(BlameResult input, int index, BlameBlock block) {
		return input.getSourceCommit(index).getName().equals(block.getFromCommitId());
	}

	private static boolean nextLineNumberInBlock(BlameResult input, int index, BlameBlock block) {
		return block.getSourceFrom() + block.getLength() == input.getSourceLine(index) + 1;
	}

	@Override
	public DiffBlameModel addBlameData(DiffModel input) {
		DiffBlameModel result = new DiffBlameModel();
		result.setOldCommit(input.getOldCommit());
		result.setNewCommit(input.getNewCommit());
		result.setCommits(input.getCommits());

		result.setDiffs(input.getDiffs().parallelStream().map((diffFile) -> {
			BlameModel oldBlame = (!diffFile.isAdded()) ? getBlameModel(input.getOldCommit().getCommit(), diffFile.getOldPath(), diffFile) : null;
			BlameModel newBlame = (!diffFile.isDeleted()) ? getBlameModel(input.getNewCommit().getCommit(), diffFile.getNewPath(), diffFile) : null;

			DiffFile<DiffContext<DiffBlameLine>> diffBlameFile = new DiffFile<>();
			diffBlameFile.setNewPath(diffFile.getNewPath());
			diffBlameFile.setOldPath(diffFile.getOldPath());
			diffBlameFile.setType(diffFile.getType());

			diffBlameFile.setContexts(diffFile.getContexts().stream().map(context -> {

				DiffContext<DiffBlameLine> diffBlameContext = new DiffContext<>();

				diffBlameContext.setLines(context.getLines().stream().map(line -> {
					DiffBlameLine diffBlameLine = new DiffBlameLine();
					diffBlameLine.setNewLineNumber(line.getNewLineNumber());
					diffBlameLine.setOldLineNumber(line.getOldLineNumber());
					diffBlameLine.setContent(line.getContent());

					int lineNumber;
					BlameBlock block;

					if (diffBlameLine.isRemoved()) {
						lineNumber = line.getOldLineNumber();
						block = oldBlame.getBlameBlock(lineNumber);
					} else {
						lineNumber = line.getNewLineNumber();
						block = newBlame.getBlameBlock(lineNumber);
					}

					diffBlameLine.setSourceCommitId(block.getFromCommitId());
					diffBlameLine.setSourceFilePath(block.getFromFilePath());
					diffBlameLine.setSourceLineNumber(block.getFromLineNumber(lineNumber));
					return diffBlameLine;
				}).collect(Collectors.toList()));

				return diffBlameContext;

			}).collect(Collectors.toList()));

			return diffBlameFile;

		}).collect(Collectors.toList()));

		return result;
	}

	/*
	 * TODO:
	 *    Helper method that provides a fallback mechanism for creating blame data.
	 *    We should investigate how to generate a blame for symbolic links nicer,
	 *    and why blame and diff are implemented inconsistently in JGit anyway.
	 *
	 *    (Diff returns a file containing the symlink target, but while debugging
	 *    blame, the object points to another blob object - the reference).
	 */
	private BlameModel getBlameModel(String commit, String path, DiffFile file) {
		try {
			return blame(commit, path);
		}
		catch (NotFoundException e) {
			return fallbackBlameModel(commit, path, file.isAdded() ? file.getLinesAdded() : file.getLinesRemoved());
		}
		catch (GitException | IOException e) {
			throw new RuntimeException(String.format("Failed to get blame model for %s at %s: %s",
				path, commit, e.getMessage()), e);
		}
	}

	/*
	 * The fallback BlameModel is a blame model that acts as if all lines for the
	 * specified path were introduced at the specified commit.
	 */
	private static BlameModel fallbackBlameModel(String commit, String path, int length) {
		BlameModel blameModel = new BlameModel();
		blameModel.setPath(path);
		blameModel.setCommitId(commit);
		BlameBlock blameBlock = new BlameBlock();
		blameBlock.setSourceFrom(1);
		blameBlock.setDestinationFrom(1);
		blameBlock.setLength(length);
		blameBlock.setFromFilePath(path);
		blameBlock.setFromCommitId(commit);
		blameModel.setBlames(Collections.singletonList(blameBlock));
		return blameModel;
	}

	private static EntryType of(org.eclipse.jgit.lib.Repository repo, TreeWalk walker, String entry) throws IOException {
		if (entry.endsWith("/")) {
			return EntryType.FOLDER;
		}

		ObjectId objectId = walker.getObjectId(0);
		ObjectLoader loader = repo.open(objectId);
		try (ObjectStream stream = loader.openStream()) {
			if (RawText.isBinary(stream)) {
				return EntryType.BINARY;
			}
			return EntryType.TEXT;
		}
	}

	@Override
	public Map<String, EntryType> showTree(String commitId, String path)  throws GitException, IOException {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(commitId));
		Preconditions.checkNotNull(path);

		try(RevWalk revWalk = new RevWalk(repo);
		    TreeWalk walker = new TreeWalk(repo)) {

			walker.setFilter(TreeFilter.ALL);
			RevTree tree = revWalk.parseCommit(repo.resolve(commitId)).getTree();
			walker.addTree(tree);
			walker.setRecursive(true);

			if (!path.endsWith("/") && !Strings.isNullOrEmpty(path)) {
				path += "/";
			}

			Map<String, EntryType> handles = Maps.newLinkedHashMap();
			while (walker.next()) {
				String entryPath = walker.getPathString();
				// Skip entries not in path
				if (!entryPath.startsWith(path)) {
					continue;
				}

				// Skip commit objects...
				if (!walker.getObjectReader().has(walker.getObjectId(0))) {
					continue;
				}

				String entry = entryPath.substring(path.length());
				if (entry.contains("/")) {
					entry = entry.substring(0, entry.indexOf('/') + 1);
				}

				handles.put(entry, of(repo, walker, entry));
			}

			if (handles.isEmpty()) {
				throw new NotFoundException("The map of handles was empty");
			}

			return handles;
		}
		catch (MissingObjectException e) {
			throw new NotFoundException(e.getMessage(), e);
		}
	}

	@Override
	public ObjectLoader showFile(final String commitId, final String path) throws IOException, GitException {
		Preconditions.checkNotNull(commitId);
		Preconditions.checkNotNull(path);

		try(RevWalk revWalk = new RevWalk(repo);
		    TreeWalk walker = new TreeWalk(repo)) {

			walker.setFilter(TreeFilter.ALL);
			RevTree tree = revWalk.parseCommit(repo.resolve(commitId)).getTree();
			walker.addTree(tree);
			walker.setRecursive(true);

			while (walker.next()) {
				String entryPath = walker.getPathString();
				if (!entryPath.startsWith(path)) {
					continue;
				}

				ObjectId objectId = walker.getObjectId(0);
				ObjectLoader loader = repo.open(objectId);
				return loader;
			}

			throw new NotFoundException("File " + path + " not found in commit " + commitId);
		}
		catch (MissingObjectException e) {
			throw new NotFoundException(e.getMessage(), e);
		}
	}

}
