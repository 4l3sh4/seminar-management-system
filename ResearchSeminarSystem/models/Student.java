package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Student class - represents a presenter in the seminar
public class Student extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String supervisorName;
    private List<Submission> submissions;

    public Student(String userId, String name, String email, String password, String supervisorName) {
        super(userId, name, email, password, "Student");
        this.supervisorName = supervisorName;
        this.submissions = new ArrayList<>();
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public List<Submission> getSubmissions() {
        if (submissions == null) submissions = new ArrayList<>();
        return submissions;
    }

    public Submission registerSubmission(String title, String abstractText, String supervisorName,
                                         String presentationType, String filePath) {

        String submissionId = "SUB" + System.currentTimeMillis();

        Submission submission = new Submission(
                submissionId,
                title,
                abstractText,
                supervisorName,      // ✅ stored in Submission now
                presentationType,
                filePath,
                this
        );

        getSubmissions().add(submission);

        // Optional: also update student's default supervisor for next registrations
        if (supervisorName != null && !supervisorName.trim().isEmpty()) {
            this.supervisorName = supervisorName.trim();
        }

        return submission;
    }

    public Submission registerSubmission(String title, String abstractText,
                                         String presentationType, String filePath) {
        return registerSubmission(title, abstractText, this.supervisorName, presentationType, filePath);
    }

    // Upload presentation materials
    public boolean uploadMaterial(Submission submission, String filePath) {
        if (submission == null) return false;

        if (getSubmissions().contains(submission)) {
            submission.setFilePath(filePath);
            return true;
        }
        return false;
    }

    @Override
    public void displayDashboard() {
        System.out.println("Student Dashboard for: " + getName());
        System.out.println("Supervisor: " + (supervisorName == null ? "" : supervisorName));
        System.out.println("Total Submissions: " + getSubmissions().size());
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + getName() + '\'' +
                ", userId='" + getUserId() + '\'' +
                ", supervisor='" + (supervisorName == null ? "" : supervisorName) + '\'' +
                ", submissions=" + getSubmissions().size() +
                '}';
    }
}
