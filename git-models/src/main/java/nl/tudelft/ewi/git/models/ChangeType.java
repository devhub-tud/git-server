package nl.tudelft.ewi.git.models;

/**
 * Created by jgmeligmeyling on 25/03/15.
 */
public enum ChangeType {

    /** Add a new file to the project */
    ADD,

    /** Modify an existing file in the project (content and/or mode) */
    MODIFY,

    /** Delete an existing file from the project */
    DELETE,

    /** Rename an existing file to a new location */
    RENAME,

    /** Copy an existing file to a new location, keeping the original */
    COPY;

}
