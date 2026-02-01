package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Session class - represents a seminar session
 * Updated for persistence (Serializable) + safer null handling + includes time
 */
public class Session implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sessionId;
    private String date;
    private String time;          // ✅ NEW: session time (e.g. "10:00", "14:30")
    private String venue;
    private String sessionType;   // "Oral" or "Poster"
    private List<Submission> submissions;
    private List<Evaluator> evaluators;

    // ✅ NEW constructor with time
    public Session(String sessionId, String date, String time, String venue, String sessionType) {
        this.sessionId = sessionId;
        this.date = date;
        this.time = time;
        this.venue = venue;
        this.sessionType = sessionType;
        this.submissions = new ArrayList<>();
        this.evaluators = new ArrayList<>();
    }

    // ✅ Backward-compatible constructor (old code still works)
    public Session(String sessionId, String date, String venue, String sessionType) {
        this(sessionId, date, "", venue, sessionType);
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // ✅ NEW
    public String getTime() {
        return time;
    }

    // ✅ NEW
    public void setTime(String time) {
        this.time = time;
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
        if (submissions == null) submissions = new ArrayList<>();
        return submissions;
    }

    public List<Evaluator> getEvaluators() {
        if (evaluators == null) evaluators = new ArrayList<>();
        return evaluators;
    }

    // Alias used by CoordinatorDashboard
    public String getDetails() {
        return getScheduleDetails();
    }

    // Add submission to session
    public void addSubmission(Submission submission) {
        if (submission == null) return;
        if (!getSubmissions().contains(submission)) {
            getSubmissions().add(submission);
        }
    }

    // Remove submission from session
    public void removeSubmission(Submission submission) {
        getSubmissions().remove(submission);
    }

    // Add evaluator to session
    public void addEvaluator(Evaluator evaluator) {
        if (evaluator == null) return;
        if (!getEvaluators().contains(evaluator)) {
            getEvaluators().add(evaluator);
        }
    }

    // Remove evaluator from session
    public void removeEvaluator(Evaluator evaluator) {
        getEvaluators().remove(evaluator);
    }

    // Get schedule details
    public String getScheduleDetails() {
        StringBuilder details = new StringBuilder();

        String safeId = (sessionId != null) ? sessionId : "";
        String safeDate = (date != null) ? date : "";
        String safeTime = (time != null && !time.trim().isEmpty()) ? time : "(Not set)";
        String safeVenue = (venue != null) ? venue : "";
        String safeType = (sessionType != null) ? sessionType : "";

        details.append("Session ID: ").append(safeId).append("\n");
        details.append("Date: ").append(safeDate).append("\n");
        details.append("Time: ").append(safeTime).append("\n");   // ✅ NEW
        details.append("Venue: ").append(safeVenue).append("\n");
        details.append("Type: ").append(safeType).append("\n");
        details.append("Number of Presentations: ").append(getSubmissions().size()).append("\n");
        details.append("Number of Evaluators: ").append(getEvaluators().size()).append("\n");

        details.append("\nPresentations:\n");
        for (int i = 0; i < getSubmissions().size(); i++) {
            Submission sub = getSubmissions().get(i);
            String title = (sub != null && sub.getTitle() != null) ? sub.getTitle() : "Unknown Title";
            String type = (sub != null && sub.getPresentationType() != null) ? sub.getPresentationType() : "Unknown Type";
            String studentName = (sub != null) ? sub.getStudentName() : "Unknown Student";

            details.append(String.format("  %d. %s - %s (%s)\n",
                    i + 1, title, studentName, type));
        }

        details.append("\nEvaluators:\n");
        for (int i = 0; i < getEvaluators().size(); i++) {
            Evaluator eval = getEvaluators().get(i);
            String name = (eval != null && eval.getName() != null) ? eval.getName() : "Unknown Evaluator";
            String exp = (eval != null && eval.getExpertise() != null) ? eval.getExpertise() : "Unknown";

            details.append(String.format("  %d. %s (%s)\n",
                    i + 1, name, exp));
        }

        return details.toString();
    }

    @Override
    public String toString() {
        String safeTime = (time != null && !time.trim().isEmpty()) ? time : "";
        return sessionId + " - " + date + (safeTime.isEmpty() ? "" : (" " + safeTime))
                + " (" + sessionType + ") - " + venue;
    }
}
