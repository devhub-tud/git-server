package nl.tudelft.ewi.git.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class DetailedCommitModel extends CommitModel {
	
	private String fullMessage;
	
	public void setFullMessage(String fullMessage) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(fullMessage));
		this.fullMessage = fullMessage;
		
		String firstLine;
		int index;
		
		if((index = fullMessage.indexOf('\n')) != -1) {
			firstLine = fullMessage.substring(0, index);
		}
		else {
			firstLine = fullMessage;
		}
		
		this.setMessage(firstLine);
	}
	
	@JsonIgnore
	public String getMessageTail() {
		int index;
		String substring;
		
		if((index = fullMessage.indexOf('\n')) != -1) {
			substring = fullMessage.substring(index);
			substring = removePrecedingNewLines(substring);
		}
		else {
			substring = "";
		}
		
		return substring;
	}
	
	private static String removePrecedingNewLines(String substring) {
		while(substring.charAt(0) == '\n') {
			if(substring.length() == 1 ) {
				substring = "";
				break;
			}
			substring = substring.substring(1);
		}
		return substring;
	}

}
