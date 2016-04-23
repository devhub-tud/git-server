package nl.tudelft.ewi.git.models;

import lombok.Data;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
public class Version {

    private String gitServerVersion;

    private String gitServerCommit;

}
