package nl.tudelft.ewi.git.web.models;

import lombok.Data;

/**
 * This class is the top-level data class which must be extended by all models returned through the REST API. Every
 * model is required to return the <code>path</code> field which describes the location of the model in the REST API.
 * 
 * @author michael
 */
@Data
public class BaseModel {

	private String path;

}
