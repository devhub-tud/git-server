package nl.tudelft.ewi.git.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This class is a data class which represents a branch in a Git repository.
 * 
 * @author michael
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchModel extends BaseModel {

	private String name;
	private String commit;
	
	@JsonIgnore
	public String getSimpleName() {
		return name.substring(name.lastIndexOf('/') + 1);
	}

}
