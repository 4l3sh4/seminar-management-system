package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Submission class - represents a research presentation submission
 * Updated for persistence (Serializable) + safer null handling + supervisor stored in submission
 */
public class Submission implements Serializable {
    private static final long serialVersionUID = 1L;

    private String submissionId;
    private String title;
    private String abstractText;
    private String supervisorName;          // ✅ store supervisor inside submission (so it persists correctly)
    private String presentationType;        // "Oral" or "Poster"
    private String filePath;
    private Student student;
    private List<Evaluation> evaluations;
    private String boardId;                // For poster presentations

    public Submission(String submissionId, String title, String abstractText,
                      String supervisorName, String presentationType,
                      String filePath, Student student) {
        this.submissionId = submissionId;
        this.title = title;
        this.abstractText = abstractText;
        this.supervisorName = supervisorName;
        this.presentationType = presentationType;
        this.filePath = filePath;
        this.student = student;
        this.evaluations = new ArrayList<>();
        this.boardId = "";
    }

    // ✅ Backward-compatible constructor (if your existing code still calls the old one)
    public Submission(String submissionId, String title, String abstractText,
                      String presentationType, String filePath, Student student) {
        this(submissionId, title, abstractText,
                (student != null ? student.getSupervisorName() : ""),
                presentationType, filePath, student);
    }

    // ---------- Getters and Setters ----------
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

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
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

    public void setStudent(Student student) {
        this.student = student;
    }

    // Convenience helpers for UI/reports
    public String getStudentId() {
        return (student != null && student.getUserId() != null) ? student.getUserId() : "";
    }

    public String getStudentName() {
        return (student != null && student.getName() != null) ? student.getName() : "Unknown Student";
    }

    public List<Evaluation> getEvaluations() {
        if (evaluations == null) evaluations = new ArrayList<>();
        return evaluations;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    // ---------- Evaluation ----------
    public void addEvaluation(Evaluation evaluation) {
        if (evaluation == null) return;
        getEvaluations().add(evaluation);
    }

    public void removeEvaluation(Evaluation evaluation) {
        if (evaluation == null) return;
        getEvaluations().remove(evaluation);
    }

    // Calculate average score from all evaluations
    public double getAverageScore() {
        List<Evaluation> evals = getEvaluations();
        if (evals.isEmpty()) return 0.0;

        double total = 0.0;
        int count = 0;

        for (Evaluation eval : evals) {
            if (eval == null) continue;
            total += eval.getTotalScore();
            count++;
        }

        return (count == 0) ? 0.0 : total / count;
    }

    // Helper: did this evaluator already evaluate this submission?
    public boolean hasEvaluationFromEvaluator(String evaluatorId) {
        if (evaluatorId == null) return false;
        for (Evaluation ev : getEvaluations()) {
            if (ev != null && evaluatorId.equals(ev.getEvaluatorId())) return true;
        }
        return false;
    }

    // Helper: get this evaluator's evaluation (if exists)
    public Evaluation getEvaluationByEvaluator(String evaluatorId) {
        if (evaluatorId == null) return null;
        for (Evaluation ev : getEvaluations()) {
            if (ev != null && evaluatorId.equals(ev.getEvaluatorId())) return ev;
        }
        return null;
    }

    // ---------- Details ----------
    public String getDetails() {
        String safeId = (submissionId != null) ? submissionId : "";
        String safeTitle = (title != null) ? title : "";
        String safeAbstract = (abstractText != null) ? abstractText : "";
        String safeSupervisor = (supervisorName != null) ? supervisorName : "";
        String safeType = (presentationType != null) ? presentationType : "";
        String safeFile = (filePath != null && !filePath.isEmpty()) ? filePath : "Not uploaded";

        int evalCount = getEvaluations().size();
        double avg = getAverageScore();

        return String.format(
                "Submission ID: %s\n" +
                "Research Title: %s\n" +
                "Student: %s\n" +
                "Supervisor: %s\n" +
                "Preferred Presentation Type: %s\n\n" +
                "Abstract:\n%s\n\n" +
                "File: %s\n\n" +
                "Average Score: %.2f\n" +
                "#Evaluations: %d",
                safeId,
                safeTitle,
                getStudentName(),
                safeSupervisor,
                safeType,
                safeAbstract,
                safeFile,
                avg,
                evalCount
        );
    }

    @Override
    public String toString() {
        String safeTitle = (title != null) ? title : "";
        String safeType = (presentationType != null) ? presentationType : "";
        return safeTitle + " (" + safeType + ") - " + getStudentName();
    }
}
