package nl.tudelft.ewi.git.web.models;

import java.util.Collection;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.validator.constraints.NotEmpty;

@Data
@EqualsAndHashCode(callSuper = true)
public class GroupModel extends IdentifiableModel {

	@NotEmpty
	private Collection<IdentifiableModel> members;
	
}
