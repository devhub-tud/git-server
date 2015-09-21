package nl.tudelft.ewi.git.models;

import lombok.*;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffContext;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffFile;
import nl.tudelft.ewi.git.models.DiffBlameModel.DiffBlameLine;

/**
 * Created by jgmeligmeyling on 25/03/15.
 */
public class DiffBlameModel extends AbstractDiffModel<DiffFile<DiffContext<DiffBlameLine>>> {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DiffBlameLine extends AbstractDiffModel.DiffLine {

        private String sourceCommitId;

        private String sourceFilePath;

        private int sourceLineNumber;

    }

}
