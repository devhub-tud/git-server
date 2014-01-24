package nl.tudelft.ewi.git.inspector;

import java.util.Date;
import java.util.LinkedHashMap;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;


public class Commit extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = -1975590477657732911L;
	
	public void setCommit(String commit) {
		put("commit", commit);
	}
	
	public void setParents(String... parents) {
		put("parents", parents);
	}
	
	public void setAuthor(String author, String email) {
		if (Strings.isNullOrEmpty(email)) {
			put("author", author);
		}
		else {
			put("author", author + " <" + email + ">");
		}
	}
	
	public void setTime(long time) {
		put("time", time);
	}
	
	public void setMessage(String message) {
		put("message", message);
	}
	
	@Override
	public String toString() {
		String[] parents = (String[]) get("parents");
		
		StringBuilder builder = new StringBuilder();
		builder.append("commit: " + get("commit") + "\n");
		
		if (parents != null && parents.length > 0) {
			builder.append("parents: " + Joiner.on(",").join((String[]) get("parents")) + "\n");
		}
		
		builder.append("author: " + get("author") + "\n");
		builder.append("time: " + new Date((long) get("time")) + "\n\n");
		builder.append(get("message") + "\n");
		return builder.toString();
	}
	
}
