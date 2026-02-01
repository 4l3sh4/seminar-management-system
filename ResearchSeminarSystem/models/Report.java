package models;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

// Report class - generates various reports for the seminar

public class Report implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reportId;
    private String reportType; // "Schedule", "Evaluation", "Award"
    private String content;
    private String generatedDate;

    public Report(String reportType, String content) {
        this.reportId = "REP" + System.currentTimeMillis();
        this.reportType = reportType;
        this.content = (content == null) ? "" : content;
        this.generatedDate = LocalDateTime.now().toString();
    }

    // Getters
    public String getReportId() {
        return reportId;
    }

    public String getReportType() {
        return reportType;
    }

    // Alias used by dashboards
    public String getDetails() {
        return content;
    }

    public String getContent() {
        return content;
    }

    public String getGeneratedDate() {
        return generatedDate;
    }

    // Report Generators

    public static Report generateScheduleReport(List<Session> sessions) {
        StringBuilder content = new StringBuilder();
        content.append("=== SEMINAR SCHEDULE REPORT ===\n");
        content.append("Generated: ").append(LocalDateTime.now()).append("\n\n");

        if (sessions == null || sessions.isEmpty()) {
            content.append("No sessions available.\n");
            return new Report("Schedule", content.toString());
        }

        for (Session session : sessions) {
            if (session == null) continue;
            content.append(session.getScheduleDetails()).append("\n");
            content.append("-----------------------------------\n\n");
        }

        return new Report("Schedule", content.toString());
    }

    public static Report generateEvaluationReport(List<Session> sessions) {
        StringBuilder content = new StringBuilder();
        content.append("=== EVALUATION REPORT ===\n");
        content.append("Generated: ").append(LocalDateTime.now()).append("\n\n");

        if (sessions == null || sessions.isEmpty()) {
            content.append("No sessions available.\n");
            return new Report("Evaluation", content.toString());
        }

        for (Session session : sessions) {
            if (session == null) continue;

            content.append("Session: ").append(session.getSessionId())
                    .append(" - ").append(session.getDate()).append("\n");
            content.append("Venue: ").append(session.getVenue()).append("\n\n");

            List<Submission> subs = session.getSubmissions();
            if (subs == null || subs.isEmpty()) {
                content.append("  No submissions assigned.\n");
                content.append("-----------------------------------\n\n");
                continue;
            }

            for (Submission submission : subs) {
                if (submission == null) continue;

                content.append("  Title: ").append(submission.getTitle()).append("\n");
                content.append("  Student: ").append(submission.getStudentName()).append("\n");
                content.append("  Type: ").append(submission.getPresentationType()).append("\n");
                content.append("  Average Score: ").append(String.format("%.2f", submission.getAverageScore())).append("\n");

                List<Evaluation> evals = submission.getEvaluations();
                int evalCount = (evals == null) ? 0 : evals.size();
                content.append("  Number of Evaluations: ").append(evalCount).append("\n");

                if (evalCount > 0) {
                    content.append("  Evaluations:\n");
                    for (Evaluation eval : evals) {
                        if (eval == null) continue;
                        content.append("    - ").append(eval.getEvaluatorName())
                                .append(": ").append(eval.getTotalScore()).append("/40\n");
                    }
                }

                content.append("\n");
            }

            content.append("-----------------------------------\n\n");
        }

        return new Report("Evaluation", content.toString());
    }

    public static Report generateAwardReport(List<Award> awards) {
        StringBuilder content = new StringBuilder();
        content.append("=== AWARD CEREMONY REPORT ===\n");
        content.append("Generated: ").append(LocalDateTime.now()).append("\n\n");

        if (awards == null || awards.isEmpty()) {
            content.append("No awards available.\n");
            return new Report("Award", content.toString());
        }

        for (Award award : awards) {
            if (award == null) continue;
            content.append(award.getAwardDetails()).append("\n");
            content.append("-----------------------------------\n\n");
        }

        return new Report("Award", content.toString());
    }

    // Export Helpers 

    public boolean exportToFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) return false;

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(content);
            return true;
        } catch (IOException e) {
            System.err.println("Error exporting report: " + e.getMessage());
            return false;
        }
    }

    public static boolean exportTextToFile(String content, String filename) {
        Report temp = new Report("Export", content);
        return temp.exportToFile(filename);
    }

    // Statistics

    public static String generateStatistics(List<Session> sessions) {
        if (sessions == null) sessions = java.util.Collections.emptyList();

        int totalSessions = sessions.size();
        int totalSubmissions = 0;
        int totalEvaluators = 0;
        int totalEvaluations = 0;

        for (Session session : sessions) {
            if (session == null) continue;

            List<Submission> subs = session.getSubmissions();
            List<Evaluator> evals = session.getEvaluators();

            totalSubmissions += (subs == null) ? 0 : subs.size();
            totalEvaluators += (evals == null) ? 0 : evals.size();

            if (subs != null) {
                for (Submission sub : subs) {
                    if (sub == null) continue;
                    List<Evaluation> es = sub.getEvaluations();
                    totalEvaluations += (es == null) ? 0 : es.size();
                }
            }
        }

        return String.format(
                "=== SEMINAR STATISTICS ===\n" +
                "Total Sessions: %d\n" +
                "Total Submissions: %d\n" +
                "Total Evaluators: %d\n" +
                "Total Evaluations: %d\n",
                totalSessions, totalSubmissions, totalEvaluators, totalEvaluations
        );
    }

    @Override
    public String toString() {
        return reportType + " Report - " + reportId;
    }
}
