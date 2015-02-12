package nl.tudelft.ewi.git.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class DiffResponse {

	private final static List<DiffModel> EMPTY_DIFFS = ImmutableList.<DiffModel> of();
	private final static List<CommitModel> EMPTY_COMMITS = ImmutableList.<CommitModel> of();
	
	private List<DiffModel> diffs = EMPTY_DIFFS;
	
	private List<CommitModel> commits = EMPTY_COMMITS;
	
	@JsonIgnore
	public boolean isAhead() {
		return !commits.isEmpty();
	}
	
	@JsonIgnore
	public int getAhead() {
		return commits.size();
	}
	
}
