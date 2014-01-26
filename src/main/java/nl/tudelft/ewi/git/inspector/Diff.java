package nl.tudelft.ewi.git.inspector;

import java.util.LinkedHashMap;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;


public class Diff extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = 9126651653400925605L;

	public void setChangeType(ChangeType changeType) {
		put("changeType", changeType);
	}

	public void setOldPath(String oldPath) {
		put("oldPath", oldPath);
	}

	public void setNewPath(String newPath) {
		put("newPath", newPath);
	}

	public void setRaw(String raw) {
		put("raw", raw);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("changeType: " + get("changeType") + "\n");
		builder.append("oldPath: " + get("oldPath") + "\n");
		builder.append("newPath: " + get("newPath") + "\n");
		builder.append("raw: " + get("raw") + "\n");
		return builder.toString();
	}
	
}
