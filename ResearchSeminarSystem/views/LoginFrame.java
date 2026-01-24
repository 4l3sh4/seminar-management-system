package views;

import data.DataManager;
import models.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.List;

// LoginFrame - Main login interface for the Seminar Management System
public class LoginFrame extends JFrame {
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

    private JButton loginButton;
    private JButton registerButton;

    private final DataManager dataManager;

    public LoginFrame() {
        dataManager = DataManager.getInstance();

        // Auto-load saved data if DataManager.loadData() exists
        invokeIfExists(dataManager, "loadData");

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Seminar Management System - Login");
        setSize(520, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(520, 80));
        JLabel titleLabel = new JLabel("Seminar Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Main
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // User ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("User ID:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        userIdField = new JTextField(20);
        mainPanel.add(userIdField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);

        // Role
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Role:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        roleComboBox = new JComboBox<>(new String[]{"Student", "Evaluator", "Coordinator"});
        mainPanel.add(roleComboBox, gbc);

        // Buttons
        gbc.gridy = 3;
        gbc.gridx = 1;
        gbc.gridwidth = 1;

        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(46, 204, 113));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.addActionListener(e -> performLogin());
        mainPanel.add(loginButton, gbc);

        gbc.gridx = 2;
        registerButton = new JButton("Register");
        registerButton.setBackground(new Color(52, 152, 219));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.addActionListener(e -> openRegisterDialog());
        mainPanel.add(registerButton, gbc);

        // Small note
        JPanel notePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel note = new JLabel("New user? Click Register.");
        note.setFont(new Font("Arial", Font.PLAIN, 12));
        notePanel.add(note);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(notePanel, BorderLayout.SOUTH);

        // Enter key to login
        passwordField.addActionListener(e -> performLogin());
    }

    private void performLogin() {
        String userId = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        if (userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter User ID and Password",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = dataManager.authenticateUser(userId, password, role);

        if (user != null) {
            dataManager.setCurrentUser(user);

            JOptionPane.showMessageDialog(this,
                    "Login Successful! Welcome, " + user.getName(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            openDashboard(user);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid credentials or role mismatch",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Registration dialog
    private void openRegisterDialog() {
        JDialog dialog = new JDialog(this, "Register New User", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField contactField = new JTextField(); // NEW
        JPasswordField passField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Student", "Evaluator", "Coordinator"});

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        form.add(new JLabel("Name:")); form.add(nameField);
        form.add(new JLabel("Email:")); form.add(emailField);
        form.add(new JLabel("Contact No:")); form.add(contactField); // NEW
        form.add(new JLabel("Password:")); form.add(passField);
        form.add(new JLabel("Confirm Password:")); form.add(confirmField);
        form.add(new JLabel("Role:")); form.add(roleBox);

        JButton okBtn = new JButton("OK");
        okBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String contact = contactField.getText().trim(); // NEW
            String password = new String(passField.getPassword()).trim();
            String confirm = new String(confirmField.getPassword()).trim();
            String role = (String) roleBox.getSelectedItem();

            if (name.isEmpty() || email.isEmpty() || contact.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please fill in all required fields.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Simple phone validation (adjust if you want stricter rules)
            // Allow digits, spaces, +, -, parentheses
            if (!contact.matches("[0-9+()\\-\\s]{7,20}")) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid contact number (digits and + - ( ) only).",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog,
                        "Passwords do not match.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String newId = generateNextUserId(role);

            try {
                User createdUser;

                if ("Student".equals(role)) {
                    Student s = new Student(newId, name, email, password, "");
                    createdUser = s;
                    dataManager.addStudent(s);

                } else if ("Evaluator".equals(role)) {
                    Evaluator ev = new Evaluator(newId, name, email, password, "");
                    createdUser = ev;
                    dataManager.addEvaluator(ev);

                } else { // Coordinator
                    Coordinator c = new Coordinator(newId, name, email, password, "");
                    createdUser = c;
                    dataManager.addCoordinator(c);
                }

                // Save contact number into the object if your model supports it
                // (e.g., setContactNumber / setPhoneNumber / setContactNo)
                setContactIfPossible(createdUser, contact);

                invokeIfExists(dataManager, "saveData");

                JOptionPane.showMessageDialog(this,
                        "Registration successful!\nYour User ID is: " + newId + "\nYou can now log in.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Auto-fill login fields
                userIdField.setText(newId);
                passwordField.setText(password);
                roleComboBox.setSelectedItem(role);

                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Registration failed: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(okBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.getRootPane().setDefaultButton(okBtn);
        dialog.setVisible(true);
    }

    // Tries to set contact number on the user object without breaking compilation
    private static void setContactIfPossible(Object userObj, String contact) {
        if (userObj == null) return;

        String[] setterNames = {"setContactNumber", "setPhoneNumber", "setContactNo", "setPhone"};
        for (String setter : setterNames) {
            if (invokeIfExistsWithArg(userObj, setter, String.class, contact)) {
                return; // stop after first successful setter
            }
        }
        // If none exists, do nothing (safe fallback).
    }

    /**
     * Generates IDs like:
     * Student -> S001, S002, ...
     * Evaluator -> E001, E002, ...
     * Coordinator -> C001, C002, ...
     */
    private String generateNextUserId(String role) {
        String prefix;
        if ("Student".equals(role)) prefix = "S";
        else if ("Evaluator".equals(role)) prefix = "E";
        else prefix = "C";

        int max = 0;
        List<User> users = dataManager.getUsers();
        if (users != null) {
            for (User u : users) {
                if (u == null || u.getUserId() == null) continue;
                String id = u.getUserId().trim().toUpperCase();

                if (id.startsWith(prefix)) {
                    String numPart = id.substring(1);
                    try {
                        int n = Integer.parseInt(numPart);
                        if (n > max) max = n;
                    } catch (NumberFormatException ignored) { }
                }
            }
        }

        int next = max + 1;
        return prefix + String.format("%03d", next);
    }

    private void openDashboard(User user) {
        if (user instanceof Student) {
            new StudentDashboard((Student) user).setVisible(true);
        } else if (user instanceof Evaluator) {
            new EvaluatorDashboard((Evaluator) user).setVisible(true);
        } else if (user instanceof Coordinator) {
            new CoordinatorDashboard((Coordinator) user).setVisible(true);
        }
    }

    /**
     * Calls a no-arg method if it exists.
     * Example: DataManager.saveData(), DataManager.loadData()
     */
    private static void invokeIfExists(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            m.invoke(target);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception ex) {
            System.err.println("Failed calling " + methodName + "(): " + ex.getMessage());
        }
    }

    /**
     * Calls a 1-arg method if it exists; returns true if it was found and called successfully.
     */
    private static boolean invokeIfExistsWithArg(Object target, String methodName, Class<?> argType, Object argValue) {
        try {
            Method m = target.getClass().getMethod(methodName, argType);
            m.invoke(target, argValue);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        } catch (Exception ex) {
            System.err.println("Failed calling " + methodName + "(): " + ex.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginFrame().setVisible(true);
        });
    }
}
