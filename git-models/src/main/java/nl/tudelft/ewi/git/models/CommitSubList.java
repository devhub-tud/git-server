package nl.tudelft.ewi.git.models;

import lombok.Data;
import nl.tudelft.ewi.git.models.CommitModel;

import java.util.List;

/**
 * Created by jgmeligmeyling on 26/03/15.
 */
@Data
public class CommitSubList {

    private List<CommitModel> commits;

    private int skip;

    private int limit;

    private int total;

}
