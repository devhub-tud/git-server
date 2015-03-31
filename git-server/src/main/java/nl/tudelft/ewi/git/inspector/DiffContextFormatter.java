package nl.tudelft.ewi.git.inspector;

import static org.eclipse.jgit.diff.DiffEntry.Side.NEW;
import static org.eclipse.jgit.diff.DiffEntry.Side.OLD;
import static org.eclipse.jgit.lib.FileMode.GITLINK;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.diff.ContentSource;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.pack.PackConfig;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import nl.tudelft.ewi.git.models.DiffContext;
import nl.tudelft.ewi.git.models.DiffLine;
import nl.tudelft.ewi.git.models.DiffModel;

public class DiffContextFormatter {

	private static final byte[] EMPTY = new byte[] {};
	private static final byte[] BINARY = new byte[] {};
	
	private DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm(SupportedAlgorithm.MYERS);
	private RawTextComparator comparator = RawTextComparator.DEFAULT;
	private int binaryFileThreshold = PackConfig.DEFAULT_BIG_FILE_THRESHOLD;
	private final List<DiffContext> list;
	private final ObjectReader reader;
	private final ContentSource.Pair source;
	private int context = 3;
	
	public DiffContextFormatter(final DiffModel diffModel, final Repository repository) {
		Preconditions.checkNotNull(diffModel);
		Preconditions.checkNotNull(repository);
		
		reader = repository.newObjectReader();
		ContentSource cs = ContentSource.create(reader);
		source = new ContentSource.Pair(cs, cs);
		
		if(diffModel.getDiffContexts() != null) {
			list = diffModel.getDiffContexts();
		}
		else {
			list = Lists.newArrayList();
			diffModel.setDiffContexts(list);
		}
	}
	
	public void format(final DiffEntry ent) throws IOException {
		if (ent.getOldMode() == GITLINK || ent.getNewMode() == GITLINK
				|| ent.getOldId() == null || ent.getNewId() == null) {
			// No diff lines for git links, renames, file adds
			return;
		}
		
		byte[] aRaw = open(OLD, ent);
		byte[] bRaw = open(NEW, ent);
		
		if (aRaw == BINARY || bRaw == BINARY //
				|| RawText.isBinary(aRaw) || RawText.isBinary(bRaw)) {
			// No diff lines for binary files
			return;
		}
		
		RawText a = new RawText(aRaw);
		RawText b = new RawText(bRaw);
		List<Edit> edits = diff(a, b);
		
		for (int curIdx = 0; curIdx < edits.size();) {
			Edit curEdit = edits.get(curIdx);
			final int endIdx = findCombinedEnd(edits, curIdx);
			final Edit endEdit = edits.get(endIdx);

			int aCur = Math.max(0, curEdit.getBeginA() - context);
			int bCur = Math.max(0, curEdit.getBeginB() - context);
			final int aEnd = Math.min(a.size(), endEdit.getEndA() + context);
			final int bEnd = Math.min(b.size(), endEdit.getEndB() + context);
			
			final DiffContext diffContext = new DiffContext();
			diffContext.setOldStart(aCur);
			diffContext.setOldEnd(bEnd);
			diffContext.setNewStart(bCur);
			diffContext.setNewEnd(bEnd);
			List<DiffLine> diffLines = Lists.newArrayListWithCapacity(2 * context + 1);
			diffContext.setDiffLines(diffLines);

			while (aCur < aEnd || bCur < bEnd) {
				DiffLine line = new DiffLine();

				if (aCur < curEdit.getBeginA() || endIdx + 1 < curIdx) {
					line.setType(DiffLine.Type.CONTEXT);
					line.setContent(a.getString(aCur));
					diffLines.add(line);
					aCur++;
					bCur++;
				} else if (aCur < curEdit.getEndA()) {
					line.setType(DiffLine.Type.REMOVED);
					line.setContent(a.getString(aCur));
					diffLines.add(line);
					aCur++;
				} else if (bCur < curEdit.getEndB()) {
					line.setType(DiffLine.Type.ADDED);
					line.setContent(b.getString(bCur));
					diffLines.add(line);
					bCur++;
				}

				if (end(curEdit, aCur, bCur) && ++curIdx < edits.size())
					curEdit = edits.get(curIdx);
			}
			
			list.add(diffContext);
		}
	}
	
	public void setContext(int context) {
		this.context = context;
	}
	
	private int findCombinedEnd(final List<Edit> edits, final int i) {
		int end = i + 1;
		while (end < edits.size()
				&& (combineA(edits, end) || combineB(edits, end)))
			end++;
		return end - 1;
	}
	
	private boolean combineA(final List<Edit> e, final int i) {
		return e.get(i).getBeginA() - e.get(i - 1).getEndA() <= 2 * context;
	}

	private boolean combineB(final List<Edit> e, final int i) {
		return e.get(i).getBeginB() - e.get(i - 1).getEndB() <= 2 * context;
	}
	
	private static boolean end(final Edit edit, final int a, final int b) {
		return edit.getEndA() <= a && edit.getEndB() <= b;
	}
	
	private EditList diff(RawText a, RawText b) {
		return diffAlgorithm.diff(comparator, a, b);
	}
	
	private byte[] open(DiffEntry.Side side, DiffEntry entry)
			throws IOException {
		
		if (entry.getMode(side) == FileMode.MISSING)
			return EMPTY;

		if (entry.getMode(side).getObjectType() != Constants.OBJ_BLOB)
			return EMPTY;

		ObjectLoader ldr = source.open(side, entry);
		return ldr.getBytes(binaryFileThreshold);
	}

}
