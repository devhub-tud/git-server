package nl.tudelft.ewi.git.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;

/**
 * Created by jgmeligmeyling on 25/03/15.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class DiffBlameModel {

    private CommitModel newCommit;

    private CommitModel oldCommit;

    /**
     * The changed files between newCommit and oldCommit
     */
    private List<DiffBlameFile> diffs;

    /**
     * The commits between newCommit and oldCommit
     */
    private List<CommitModel> commits;

    @JsonIgnore
    public boolean isAhead() {
        return !commits.isEmpty();
    }

    @JsonIgnore
    public int getAhead() {
        return commits.size();
    }


    /**
     * This class is a data class which represents a diff between two commits in a Git repository.
     *
     * @author michael
     */
    @Data
    @EqualsAndHashCode
    public static class DiffBlameFile {

        private ChangeType type;
        private String oldPath;
        private String newPath;
        private List<DiffBlameContext> contexts;

        private int amountOfLinesWithType(final LineType type) {
            int amount = 0;
            for(DiffBlameContext context : contexts)
                amount += context.amountOfLinesWithType(type);
            return amount;
        }

        /**
         * @return the amount of added lines in this {@code DiffModel}
         */
        @JsonIgnore
        public int getLinesAdded() {
            return amountOfLinesWithType(LineType.ADDED);
        }

        /**
         * @return the amount of removed lines in this {@code DiffModel}
         */
        @JsonIgnore
        public int getLinesRemoved() {
            return amountOfLinesWithType(LineType.REMOVED);
        }

        @JsonIgnore
        public boolean isDeleted() {
            return type.equals(ChangeType.DELETE);
        }

        @JsonIgnore
        public boolean isAdded() {
            return type.equals(ChangeType.ADD);
        }

        @JsonIgnore
        public boolean isModified() {
            return type.equals(ChangeType.MODIFY);
        }

        @JsonIgnore
        public boolean isCopied() {
            return type.equals(ChangeType.COPY);
        }

        @JsonIgnore
        public boolean isMoved() {
            return type.equals(ChangeType.RENAME);
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiffBlameContext {

        private List<DiffBlameLine> lines;

        /**
         * @param type
         *            for the lines
         * @return the amount of lines with a specific type (eg. only additions)
         */
        public int amountOfLinesWithType(final LineType type) {
            int amount = 0;
            for(DiffBlameLine line : lines) {
                switch (type){
                    case ADDED:
                        if(line.isAdded()) amount++;
                        break;
                    case CONTEXT:
                        if(line.isUnchanged()) amount++;
                        break;
                    case REMOVED:
                        if(line.isRemoved()) amount++;
                        break;
                }
            }
            return amount;
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiffBlameLine extends DiffModel.DiffLine {

        private String sourceCommitId;

        private String sourceFilePath;

        private int sourceLineNumber;

    }

}
