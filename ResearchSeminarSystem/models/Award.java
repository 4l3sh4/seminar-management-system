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

    public void setWinner(Submission winner, double score) {
        this.winner = winner;
        this.winningScore = (winner != null) ? score : 0.0;
    }

    public double getWinningScore() {
        return winningScore;
    }

    // Determine winner from a list of submissions
    public void determineWinner(List<Submission> submissions) {
        if (submissions == null || submissions.isEmpty()) return;
    
        Submission best = null;
        double bestScore = 0.0;
    
        for (Submission sub : submissions) {
            if (sub == null) continue;
            if (sub.getEvaluations() == null || sub.getEvaluations().isEmpty()) continue;
    
            double score;
    
            if ("Best Oral".equals(awardType)) {
                if (sub.getPresentationType() == null || !"Oral".equalsIgnoreCase(sub.getPresentationType())) continue;
                score = sub.getAverageScore();
    
            } else if ("Best Poster".equals(awardType)) {
                if (sub.getPresentationType() == null || !"Poster".equalsIgnoreCase(sub.getPresentationType())) continue;
                score = sub.getAverageScore();
    
            } else if ("People's Choice".equals(awardType)) {
                double total = 0.0;
            for (Evaluation ev : sub.getEvaluations()) {
                if (ev == null) continue;
                total += ev.getTotalScore();
            }
            score = total;

            } else {
                continue;
            }
    
            if (score > bestScore) {
                bestScore = score;
                best = sub;
            }
        }
    
        setWinner(best, bestScore);
    }

    // Get award details
    public String getAwardDetails() {
        if (winner == null) {
            return String.format("Award: %s\nWinner: Not yet determined", awardType);
        }
    
        String studentName = winner.getStudentName();
        String label = "Average Score";
        if ("People's Choice".equals(awardType)) {
            label = "Total Marks";
        }
    
        return String.format(
                "Award: %s\n" +
                "Winner Submission ID: %s\n" +
                "Submission: %s\n" +
                "Student: %s\n" +
                "%s: %.2f",
                awardType,
                winner.getSubmissionId(),
                winner.getTitle(),
                (studentName.isEmpty() ? "Unknown" : studentName),
                label,
                winningScore
        );
    }

    @Override
    public String toString() {
        if (winner == null) return awardType + " - No winner yet";
        return awardType + " - " + winner.getTitle() + " (" + String.format("%.2f", winningScore) + ")";
    }
}
