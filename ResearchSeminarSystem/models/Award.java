package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Award class - represents awards for best presentations

public class Award implements Serializable {
    private static final long serialVersionUID = 1L;

    private String awardId;
    private String awardType; // "Best Oral", "Best Poster", "People's Choice"
    private Submission winner;
    private double winningScore;

    public Award(String awardType) {
        this.awardId = "AWD" + System.currentTimeMillis();
        this.awardType = awardType;
        this.winner = null;
        this.winningScore = 0.0;
    }

    // Getters and Setters
    public String getAwardId() {
        return awardId;
    }

    public String getAwardType() {
        return awardType;
    }

    // Alias used by dashboards
    public String getDetails() {
        return getAwardDetails();
    }

    public void setAwardType(String awardType) {
        this.awardType = awardType;
    }

    public Submission getWinner() {
        return winner;
    }

    public void setWinner(Submission winner) {
        this.winner = winner;
        this.winningScore = (winner != null) ? winner.getAverageScore() : 0.0;
    }

    public double getWinningScore() {
        return winningScore;
    }

    // Determine winner from a list of submissions
    public void determineWinner(List<Submission> submissions) {
        if (submissions == null || submissions.isEmpty()) return;

        // Filter by presentation type for specific awards
        List<Submission> filtered = new ArrayList<>();

        if ("Best Oral".equals(awardType)) {
            for (Submission sub : submissions) {
                if (sub != null && "Oral".equals(sub.getPresentationType())) filtered.add(sub);
            }
        } else if ("Best Poster".equals(awardType)) {
            for (Submission sub : submissions) {
                if (sub != null && "Poster".equals(sub.getPresentationType())) filtered.add(sub);
            }
        } else {
            // People's Choice - all submissions
            for (Submission sub : submissions) {
                if (sub != null) filtered.add(sub);
            }
        }

        Submission bestSubmission = null;
        double highestScore = 0.0;

        for (Submission sub : filtered) {
            // Only consider if there is at least 1 evaluation
            if (sub.getEvaluations() == null || sub.getEvaluations().isEmpty()) continue;

            double avgScore = sub.getAverageScore();
            if (avgScore > highestScore) {
                highestScore = avgScore;
                bestSubmission = sub;
            }
        }

        this.winner = bestSubmission;
        this.winningScore = highestScore;
    }

    // Get award details
    public String getAwardDetails() {
        if (winner == null) {
            return String.format("Award: %s\nWinner: Not yet determined", awardType);
        }

        String studentName = winner.getStudentName(); // safe helper from Submission
        return String.format(
                "Award: %s\n" +
                "Winner Submission ID: %s\n" +
                "Submission: %s\n" +
                "Student: %s\n" +
                "Average Score: %.2f",
                awardType,
                winner.getSubmissionId(),
                winner.getTitle(),
                (studentName.isEmpty() ? "Unknown" : studentName),
                winningScore
        );
    }

    @Override
    public String toString() {
        if (winner == null) return awardType + " - No winner yet";
        return awardType + " - " + winner.getTitle() + " (" + String.format("%.2f", winningScore) + ")";
    }
}
