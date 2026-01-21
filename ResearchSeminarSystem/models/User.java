package models;

import java.io.Serializable;

/**
 * User class - parent class for Student, Evaluator, and Coordinator
 * Updated for persistence (Serializable)
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String userId;
    protected String name;
    protected String email;
    protected String password;
    protected String role; // "Student", "Evaluator", "Coordinator"

    public User(String userId, String name, String email, String password, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * NOTE: Password is stored in plain text for assignment simplicity.
     * In real systems, always hash passwords.
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    protected void setRole(String role) { // protect role from random UI edits
        this.role = role;
    }

    // Abstract method to be implemented by subclasses
    public abstract void displayDashboard();

    // Login validation
    public boolean login(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
