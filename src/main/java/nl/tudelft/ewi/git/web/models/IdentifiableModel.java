package nl.tudelft.ewi.git.web.models;

import javax.validation.constraints.Pattern;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.validator.constraints.NotEmpty;

@Data
@EqualsAndHashCode(callSuper = true)
public class IdentifiableModel extends BaseModel {

	@NotEmpty
	@Pattern(regexp = "^[a-zA-Z0-9]+$")
	private String name;
	
}
