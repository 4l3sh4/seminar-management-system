package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Submission class - represents a research presentation submission
 * Updated for persistence (Serializable)
 */
public class Submission implements Serializable {
    private static final long serialVersionUID = 1L;

    private String submissionId;
    private String title;
    private String abstractText;
    private String presentationType; // "Oral" or "Poster"
    private String filePath;
    private Student student;
    private List<Evaluation> evaluations;
    private String boardId; // For poster presentations

    public Submission(String submissionId, String title, String abstractText,
                      String presentationType, String filePath, Student student) {
        this.submissionId = submissionId;
        this.title = title;
        this.abstractText = abstractText;
        this.presentationType = presentationType;
        this.filePath = filePath;
        this.student = student;
        this.evaluations = new ArrayList<>();
        this.boardId = "";
    }

    // Getters and Setters
    public String getSubmissionId() {
        return submissionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getPresentationType() {
        return presentationType;
    }

    public void setPresentationType(String presentationType) {
        this.presentationType = presentationType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Student getStudent() {
        return student;
    }

    // Convenience helpers for UI/reports
    public String getStudentId() {
        return student != null ? student.getUserId() : "";
    }

    public String getStudentName() {
        return student != null ? student.getName() : "";
    }

    public List<Evaluation> getEvaluations() {
        return evaluations;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    // Add evaluation to this submission
    public void addEvaluation(Evaluation evaluation) {
        if (evaluation == null) return;
        this.evaluations.add(evaluation);
    }

    // Calculate average score from all evaluations
    public double getAverageScore() {
        if (evaluations.isEmpty()) return 0.0;

        double total = 0.0;
        for (Evaluation eval : evaluations) {
            total += eval.getTotalScore();
        }
        return total / evaluations.size();
    }

    // Get details (safe even if student is null)
    public String getDetails() {
        return String.format(
                "Submission: %s\nTitle: %s\nType: %s\nStudent: %s\nAverage Score: %.2f\n#Evaluations: %d",
                submissionId,
                title,
                presentationType,
                (student != null ? student.getName() : "Unknown"),
                getAverageScore(),
                evaluations.size()
        );
    }

    @Override
    public String toString() {
        String studentName = (student != null) ? student.getName() : "Unknown";
        return title + " (" + presentationType + ") - " + studentName;
    }
}
