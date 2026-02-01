package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Evaluator class - represents a panel member who evaluates presentations

public class Evaluator extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String expertise;
    private List<Evaluation> evaluations;
    private List<Session> assignedSessions;

    public Evaluator(String userId, String name, String email, String password, String expertise) {
        super(userId, name, email, password, "Evaluator");
        this.expertise = expertise;
        this.evaluations = new ArrayList<>();
        this.assignedSessions = new ArrayList<>();
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public List<Evaluation> getEvaluations() {
        return evaluations;
    }

    public List<Session> getAssignedSessions() {
        return assignedSessions;
    }

    // Assign evaluator to a session
    public void assignToSession(Session session) {
        if (session == null) return;
        if (!assignedSessions.contains(session)) {
            assignedSessions.add(session);
        }
    }

    // Create evaluation for a submission
    public Evaluation evaluateSubmission(Submission submission, int problemClarity,
                                         int methodology, int results, int presentation,
                                         String comments) {
        if (submission == null) {
            throw new IllegalArgumentException("Submission cannot be null.");
        }

        String evalId = "EVAL" + System.currentTimeMillis();
        Evaluation evaluation = new Evaluation(evalId, problemClarity, methodology,
                results, presentation, comments, submission, this);

        evaluations.add(evaluation);

        // Keep Submission as the single source of truth for its evaluation list
        submission.addEvaluation(evaluation);

        return evaluation;
    }

    // Get all submissions assigned to this evaluator through sessions
    public List<Submission> getAssignedSubmissions() {
        List<Submission> allSubmissions = new ArrayList<>();
        for (Session session : assignedSessions) {
            if (session != null && session.getSubmissions() != null) {
                allSubmissions.addAll(session.getSubmissions());
            }
        }
        return allSubmissions;
    }

    @Override
    public void displayDashboard() {
        System.out.println("Evaluator Dashboard for: " + getName());
        System.out.println("Expertise: " + expertise);
        System.out.println("Assigned Sessions: " + assignedSessions.size());
        System.out.println("Evaluations Completed: " + evaluations.size());
    }

    @Override
    public String toString() {
        return "Evaluator{" +
                "name='" + getName() + '\'' +
                ", userId='" + getUserId() + '\'' +
                ", expertise='" + expertise + '\'' +
                ", evaluations=" + evaluations.size() +
                '}';
    }
}
