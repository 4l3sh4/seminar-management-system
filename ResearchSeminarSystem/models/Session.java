package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Session class - represents a seminar session
 * Updated for persistence (Serializable) + safer null handling
 */
public class Session implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sessionId;
    private String date;
    private String venue;
    private String sessionType; // "Oral" or "Poster"
    private List<Submission> submissions;
    private List<Evaluator> evaluators;

    public Session(String sessionId, String date, String venue, String sessionType) {
        this.sessionId = sessionId;
        this.date = date;
        this.venue = venue;
        this.sessionType = sessionType;
        this.submissions = new ArrayList<>();
        this.evaluators = new ArrayList<>();
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public String getDate() {
        return date;
    }

    // Alias used by CoordinatorDashboard
    public String getDetails() {
        return getScheduleDetails();
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public List<Evaluator> getEvaluators() {
        return evaluators;
    }

    // Add submission to session
    public void addSubmission(Submission submission) {
        if (submission == null) return;
        if (!submissions.contains(submission)) {
            submissions.add(submission);
        }
    }

    // Remove submission from session
    public void removeSubmission(Submission submission) {
        submissions.remove(submission);
    }

    // Add evaluator to session
    public void addEvaluator(Evaluator evaluator) {
        if (evaluator == null) return;
        if (!evaluators.contains(evaluator)) {
            evaluators.add(evaluator);
        }
    }

    // Remove evaluator from session
    public void removeEvaluator(Evaluator evaluator) {
        evaluators.remove(evaluator);
    }

    // Get schedule details
    public String getScheduleDetails() {
        StringBuilder details = new StringBuilder();
        details.append("Session ID: ").append(sessionId).append("\n");
        details.append("Date: ").append(date).append("\n");
        details.append("Venue: ").append(venue).append("\n");
        details.append("Type: ").append(sessionType).append("\n");
        details.append("Number of Presentations: ").append(submissions.size()).append("\n");
        details.append("Number of Evaluators: ").append(evaluators.size()).append("\n");

        details.append("\nPresentations:\n");
        for (int i = 0; i < submissions.size(); i++) {
            Submission sub = submissions.get(i);
            String title = (sub != null) ? sub.getTitle() : "Unknown Title";
            String type = (sub != null) ? sub.getPresentationType() : "Unknown Type";
            String studentName = (sub != null) ? sub.getStudentName() : "Unknown Student";

            details.append(String.format("  %d. %s - %s (%s)\n",
                    i + 1, title, studentName, type));
        }

        details.append("\nEvaluators:\n");
        for (int i = 0; i < evaluators.size(); i++) {
            Evaluator eval = evaluators.get(i);
            String name = (eval != null) ? eval.getName() : "Unknown Evaluator";
            String exp = (eval != null) ? eval.getExpertise() : "Unknown";

            details.append(String.format("  %d. %s (%s)\n",
                    i + 1, name, exp));
        }

        return details.toString();
    }

    @Override
    public String toString() {
        return sessionId + " - " + date + " (" + sessionType + ") - " + venue;
    }
}
