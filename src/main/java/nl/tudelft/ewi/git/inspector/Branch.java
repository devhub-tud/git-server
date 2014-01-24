package nl.tudelft.ewi.git.inspector;

import java.util.LinkedHashMap;


public class Branch extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = -1975590477657732911L;
	
	public void setName(String name) {
		put("name", name);
	}
	
	public void setCommit(String commit) {
		put("commit", commit);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("name: " + get("name") + "\n");
		builder.append("commit: " + get("commit") + "\n");
		return builder.toString();
	}
	
}
