package models;

import java.io.Serializable;

/**
 * Evaluation class - represents an evaluation of a submission by an evaluator
 * Updated for persistence (Serializable) + safer null handling
 */
public class Evaluation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String evaluationId;
    private int problemClarity;    // Score out of 10
    private int methodology;       // Score out of 10
    private int results;           // Score out of 10
    private int presentation;      // Score out of 10
    private String comments;
    private Submission submission;
    private Evaluator evaluator;

    public Evaluation(String evaluationId, int problemClarity, int methodology,
                      int results, int presentation, String comments,
                      Submission submission, Evaluator evaluator) {
        this.evaluationId = evaluationId;
        this.problemClarity = problemClarity;
        this.methodology = methodology;
        this.results = results;
        this.presentation = presentation;
        this.comments = comments;
        this.submission = submission;
        this.evaluator = evaluator;
    }

    // Getters and Setters
    public String getEvaluationId() {
        return evaluationId;
    }

    public int getProblemClarity() {
        return problemClarity;
    }

    public void setProblemClarity(int problemClarity) {
        this.problemClarity = problemClarity;
    }

    public int getMethodology() {
        return methodology;
    }

    public void setMethodology(int methodology) {
        this.methodology = methodology;
    }

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    public int getPresentation() {
        return presentation;
    }

    public void setPresentation(int presentation) {
        this.presentation = presentation;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Submission getSubmission() {
        return submission;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    // Convenience getters (cleaner for dashboards/reports)
    public String getEvaluatorId() {
        return evaluator != null ? evaluator.getUserId() : "";
    }

    public String getEvaluatorName() {
        return evaluator != null ? evaluator.getName() : "";
    }

    public String getSubmissionId() {
        return submission != null ? submission.getSubmissionId() : "";
    }

    public String getSubmissionTitle() {
        return submission != null ? submission.getTitle() : "";
    }

    // Calculate total score (out of 40)
    public int getTotalScore() {
        return problemClarity + methodology + results + presentation;
    }

    // Calculate percentage
    public double getPercentage() {
        return (getTotalScore() / 40.0) * 100.0;
    }

     public String getDetails() {
        String evaluatorName = (evaluator != null) ? evaluator.getName() : "Unknown Evaluator";
        String submissionTitle = (submission != null) ? submission.getTitle() : "Unknown Submission";
        String safeComments = (comments != null) ? comments : "";
    
        // wrap long comments so JOptionPane doesn't show 1 super long line
        String wrappedComments = wrapText(safeComments, 80); // 80 chars per line (change if you want)
    
        return String.format(
                "Evaluation by: %s\n" +
                "Submission: %s\n" +
                "Problem Clarity: %d/10\n" +
                "Methodology: %d/10\n" +
                "Results: %d/10\n" +
                "Presentation: %d/10\n" +
                "Total: %d/40 (%.2f%%)\n" +
                "Comments:\n%s",
                evaluatorName,
                submissionTitle,
                problemClarity, methodology, results, presentation,
                getTotalScore(), getPercentage(),
                wrappedComments
        );
    }
    
    private String wrapText(String text, int maxCharsPerLine) {
        if (text == null) return "";
        if (maxCharsPerLine <= 0) return text;

        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            sb.append(ch);

            // reset on manual newline
            if (ch == '\n') {
                count = 0;
                continue;
            }

            count++;

            // force break even if no spaces (fixes KKKKKKKKK...)
            if (count >= maxCharsPerLine) {
                sb.append('\n');
                count = 0;
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        String submissionTitle = (submission != null) ? submission.getTitle() : "Unknown Submission";
        return String.format("%s - Total: %d/40", submissionTitle, getTotalScore());
    }
}
