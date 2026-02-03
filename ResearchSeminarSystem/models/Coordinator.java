package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Coordinator class - represents faculty staff managing the seminar

public class Coordinator extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String department;
    private List<Session> managedSessions;

    public Coordinator(String userId, String name, String email, String password, String department) {
        super(userId, name, email, password, "Coordinator");
        this.department = department;
        this.managedSessions = new ArrayList<>();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<Session> getManagedSessions() {
        return managedSessions;
    }

    // Create and manage seminar session
    public Session createSession(String date, String venue, String sessionType) {
        String sessionId = "SES" + System.currentTimeMillis();
        Session session = new Session(sessionId, date, venue, sessionType);
        managedSessions.add(session);
        return session;
    }

    public boolean assignEvaluatorToSession(Session session, Evaluator evaluator) {
        if (session == null || evaluator == null) return false;
    
        // Ensure this coordinator manages the session
        if (!managedSessions.contains(session)) {
            managedSessions.add(session);
        }
    
        session.addEvaluator(evaluator);
        evaluator.assignToSession(session);
        return true;
    }
    
    public boolean assignSubmissionToSession(Session session, Submission submission) {
        if (session == null || submission == null) return false;
    
        if (!managedSessions.contains(session)) {
            managedSessions.add(session);
        }
    
        session.addSubmission(submission);
    
        if ("Poster".equalsIgnoreCase(session.getSessionType())) {
            if (submission.getBoardId() == null || submission.getBoardId().trim().isEmpty()) {
                String v = (session.getVenue() == null)
                        ? "VENUE"
                        : session.getVenue().replaceAll("\\s+", "").toUpperCase();
                int n = session.getSubmissions().size();
                submission.setBoardId(v + "-" + n);
            }
        }
    
        return true;
    }

    // Generate seminar schedule (String version)
    public String generateSchedule() {
        StringBuilder schedule = new StringBuilder();
        schedule.append("=== SEMINAR SCHEDULE ===\n\n");

        for (Session session : managedSessions) {
            schedule.append(session.getScheduleDetails()).append("\n\n");
        }

        return schedule.toString();
    }

    // Generate evaluation report (String version)
    public String generateEvaluationReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== EVALUATION REPORT ===\n\n");

        for (Session session : managedSessions) {
            report.append("Session: ").append(session.getSessionId())
                  .append(" (").append(session.getDate()).append(")\n");

            for (Submission submission : session.getSubmissions()) {
                report.append("\n  Submission: ").append(submission.getTitle())
                      .append("\n  Student: ").append(submission.getStudent().getName())
                      .append("\n  Average Score: ").append(String.format("%.2f", submission.getAverageScore()))
                      .append("\n");
            }
            report.append("\n");
        }

        return report.toString();
    }

    // Dashboard-friendly wrapper returning Report object
    public Report generateScheduleReport(List<Session> sessions) {
        return Report.generateScheduleReport(sessions);
    }

    // Dashboard-friendly wrapper returning Report object
    public Report generateEvaluationReport(List<Session> sessions) {
        return Report.generateEvaluationReport(sessions);
    }

    // Compute awards (returns a LIST of 3 awards)
    public List<Award> computeAwards(List<Submission> submissions) {
        List<Award> result = new ArrayList<>();

        Award bestOral = new Award("Best Oral");
        bestOral.determineWinner(submissions);

        Award bestPoster = new Award("Best Poster");
        bestPoster.determineWinner(submissions);

        Award peoplesChoice = new Award("People's Choice");
        peoplesChoice.determineWinner(submissions);

        result.add(bestOral);
        result.add(bestPoster);
        result.add(peoplesChoice);

        return result;
    }

    @Override
    public void displayDashboard() {
        System.out.println("Coordinator Dashboard for: " + getName());
        System.out.println("Department: " + department);
        System.out.println("Managed Sessions: " + managedSessions.size());
    }

    @Override
    public String toString() {
        return "Coordinator{" +
                "name='" + getName() + '\'' +
                ", userId='" + getUserId() + '\'' +
                ", department='" + department + '\'' +
                ", sessions=" + managedSessions.size() +
                '}';
    }
}
