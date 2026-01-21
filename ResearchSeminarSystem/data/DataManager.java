package data;

import models.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static DataManager instance;

    private static final String DATA_FILE = "seminar_data.ser";
    private DataStore store;

    private User currentUser;

    private DataManager() {
        store = loadFromDisk();
        if (store == null) {
            store = new DataStore(); // empty (no sample data)
            saveToDisk();            // create the file the first time
        }
    }

    public static DataManager getInstance() {
        if (instance == null) instance = new DataManager();
        return instance;
    }

    // ---------- Persistence ----------
    private DataStore loadFromDisk() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return null;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            return (DataStore) in.readObject();
        } catch (Exception e) {
            System.err.println("Failed to load data: " + e.getMessage());
            return null;
        }
    }

    public void saveToDisk() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(store);
        } catch (Exception e) {
            System.err.println("Failed to save data: " + e.getMessage());
        }
    }

    // ---------- User management ----------
    public void addStudent(Student student) {
        store.students.add(student);
        store.users.add(student);
        saveToDisk();
    }

    public void addEvaluator(Evaluator evaluator) {
        store.evaluators.add(evaluator);
        store.users.add(evaluator);
        saveToDisk();
    }

    public void addCoordinator(Coordinator coordinator) {
        store.coordinators.add(coordinator);
        store.users.add(coordinator);
        saveToDisk();
    }

    public User authenticateUser(String userId, String password, String role) {
        for (User user : store.users) {
            if (user.getUserId().equals(userId) &&
                user.getPassword().equals(password) &&
                user.getRole().equals(role)) {
                return user;
            }
        }
        return null;
    }

    public boolean userIdExists(String userId) {
        for (User u : store.users) {
            if (u.getUserId().equalsIgnoreCase(userId)) return true;
        }
        return false;
    }

    // ---------- Getters ----------
    public List<User> getUsers() { return store.users; }
    public List<Student> getStudents() { return store.students; }
    public List<Evaluator> getEvaluators() { return store.evaluators; }
    public List<Coordinator> getCoordinators() { return store.coordinators; }
    public List<Submission> getSubmissions() { return store.submissions; }
    public List<Session> getSessions() { return store.sessions; }
    public List<Evaluation> getEvaluations() { return store.evaluations; }
    public List<Award> getAwards() { return store.awards; }

    public void addSubmission(Submission submission) {
        store.submissions.add(submission);
        saveToDisk();
    }

    public void addSession(Session session) {
        store.sessions.add(session);
        saveToDisk();
    }

    public void addEvaluation(Evaluation evaluation) {
        store.evaluations.add(evaluation);
        saveToDisk();
    }

    public void addAward(Award award) {
        store.awards.add(award);
        saveToDisk();
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }

    // ---------- Find helpers ----------
    public Student findStudentById(String studentId) {
        for (Student s : store.students) if (s.getUserId().equals(studentId)) return s;
        return null;
    }

    public Evaluator findEvaluatorById(String evaluatorId) {
        for (Evaluator e : store.evaluators) if (e.getUserId().equals(evaluatorId)) return e;
        return null;
    }

    public Submission findSubmissionById(String submissionId) {
        for (Submission sub : store.submissions)
            if (sub.getSubmissionId().equals(submissionId)) return sub;
        return null;
    }

    public Session findSessionById(String sessionId) {
        for (Session s : store.sessions)
            if (s.getSessionId().equals(sessionId)) return s;
        return null;
    }

    // ---------- Auto ID generation ----------
    private String nextId(String prefix) {
        int max = 0;
        for (User u : store.users) {
            String id = u.getUserId();
            if (id != null && id.startsWith(prefix)) {
                try {
                    int n = Integer.parseInt(id.substring(prefix.length()));
                    if (n > max) max = n;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("%s%03d", prefix, max + 1);
    }

    public String generateStudentId() { return nextId("S"); }
    public String generateEvaluatorId() { return nextId("E"); }
    public String generateCoordinatorId() { return nextId("C"); }

    // Useful filters (your Evaluation uses Evaluator object)
    public List<Evaluation> getEvaluationsByEvaluator(String evaluatorId) {
        List<Evaluation> result = new ArrayList<>();
        for (Evaluation ev : store.evaluations) {
            if (ev.getEvaluator() != null && ev.getEvaluator().getUserId().equals(evaluatorId)) {
                result.add(ev);
            }
        }
        return result;
    }
}
