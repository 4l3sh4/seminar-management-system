package data;

import models.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DataManager - central storage + persistence manager for the Seminar Management System
 * Saves/loads a DataStore object to seminar_data.ser using Java serialization.
 */
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

    // Persistence 
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

    // User management 
    public void addStudent(Student student) {
        if (student == null) return;
        store.students.add(student);
        store.users.add(student);
        saveToDisk();
    }

    public void addEvaluator(Evaluator evaluator) {
        if (evaluator == null) return;
        store.evaluators.add(evaluator);
        store.users.add(evaluator);
        saveToDisk();
    }

    public void addCoordinator(Coordinator coordinator) {
        if (coordinator == null) return;
        store.coordinators.add(coordinator);
        store.users.add(coordinator);
        saveToDisk();
    }

    public User authenticateUser(String userId, String password, String role) {
        if (userId == null || password == null || role == null) return null;

        for (User user : store.users) {
            if (user == null) continue;
            if (user.getUserId() == null || user.getPassword() == null || user.getRole() == null) continue;

            if (user.getUserId().equals(userId) &&
                user.getPassword().equals(password) &&
                user.getRole().equals(role)) {
                return user;
            }
        }
        return null;
    }

    public boolean userIdExists(String userId) {
        if (userId == null) return false;
        for (User u : store.users) {
            if (u != null && u.getUserId() != null && u.getUserId().equalsIgnoreCase(userId)) return true;
        }
        return false;
    }

    // Getters
    public List<User> getUsers() { return store.users; }
    public List<Student> getStudents() { return store.students; }
    public List<Evaluator> getEvaluators() { return store.evaluators; }
    public List<Coordinator> getCoordinators() { return store.coordinators; }
    public List<Submission> getSubmissions() { return store.submissions; }
    public List<Session> getSessions() { return store.sessions; }
    public List<Evaluation> getEvaluations() { return store.evaluations; }
    public List<Award> getAwards() { return store.awards; }

    // Add entities
    public void addSubmission(Submission submission) {
        if (submission == null) return;
        store.submissions.add(submission);
        saveToDisk();
    }

    public void addSession(Session session) {
        if (session == null) return;
        store.sessions.add(session);
        saveToDisk();
    }

    public void addEvaluation(Evaluation evaluation) {
        if (evaluation == null) return;
        store.evaluations.add(evaluation);
        saveToDisk();
    }
    
    public void removeEvaluation(Evaluation evaluation) {
        if (evaluation == null) return;
        store.evaluations.remove(evaluation);
        saveToDisk();
    }

    public void addAward(Award award) {
        if (award == null) return;
        store.awards.add(award);
        saveToDisk();
    }

    // Current user
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }

    // Find helpers 
    public Student findStudentById(String studentId) {
        if (studentId == null) return null;
        for (Student s : store.students) {
            if (s != null && studentId.equals(s.getUserId())) return s;
        }
        return null;
    }

    public Evaluator findEvaluatorById(String evaluatorId) {
        if (evaluatorId == null) return null;
        for (Evaluator e : store.evaluators) {
            if (e != null && evaluatorId.equals(e.getUserId())) return e;
        }
        return null;
    }

    public Submission findSubmissionById(String submissionId) {
        if (submissionId == null) return null;
        for (Submission sub : store.submissions) {
            if (sub != null && submissionId.equals(sub.getSubmissionId())) return sub;
        }
        return null;
    }

    public Session findSessionById(String sessionId) {
        if (sessionId == null) return null;
        for (Session s : store.sessions) {
            if (s != null && sessionId.equals(s.getSessionId())) return s;
        }
        return null;
    }

    // Auto ID generation 
    private String nextId(String prefix) {
        int max = 0;

        for (User u : store.users) {
            if (u == null) continue;

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

    // Useful filters 
    // If your Evaluation stores an Evaluator object, use this.
    public List<Evaluation> getEvaluationsByEvaluator(String evaluatorId) {
        List<Evaluation> result = new ArrayList<>();
        if (evaluatorId == null) return result;

        for (Evaluation ev : store.evaluations) {
            if (ev == null) continue;

            // Some projects store evaluator object; some store evaluatorId string
            try {
                if (ev.getEvaluator() != null && evaluatorId.equals(ev.getEvaluator().getUserId())) {
                    result.add(ev);
                    continue;
                }
            } catch (Exception ignored) {}

            try {
                if (evaluatorId.equals(ev.getEvaluatorId())) {
                    result.add(ev);
                }
            } catch (Exception ignored) {}
        }

        return result;
    }
}
