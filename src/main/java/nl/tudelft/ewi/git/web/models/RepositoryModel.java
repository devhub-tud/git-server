package nl.tudelft.ewi.git.web.models;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.validator.constraints.NotEmpty;

@Data
@EqualsAndHashCode(callSuper = true)
public class RepositoryModel extends BaseModel {

	@NotEmpty
	private String name;
	
	private String url;
	
	private Map<String, String> permissions;
	
}
