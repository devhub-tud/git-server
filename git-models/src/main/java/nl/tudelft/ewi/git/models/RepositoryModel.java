package nl.tudelft.ewi.git.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

/**
 * This model represents a view of a repository in the Gitolite config.
 *
 * @author michael
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryModel {
	
	public enum Level {
		ADMIN		("RW+"),
		READ_WRITE	("RW"),
		READ_ONLY	("R");
		
		public static Level getLevel(String value) {
			for (Level level : values()) {
				if (level.getLevel().equals(value)) {
					return level;
				}
			}
			return null;
		}
		
		private final String level;
		
		Level(String level) {
			this.level = level;
		}
		
		public String getLevel() {
			return level;
		}
	}

	@NotEmpty
	@Pattern(regexp = "^\\w[\\w._\\@\\/+-]*[\\w._\\@+-]$")
	private String name;

	private String url;
	
	@NotEmpty
	private Map<String, Level> permissions;

}
