package views;

import data.DataManager;
import models.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.List;

/**
 * LoginFrame - Main login interface for the Seminar Management System
 */
public class LoginFrame extends JFrame {
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

    private JButton loginButton;
    private JButton registerButton;

    private final DataManager dataManager;

    public LoginFrame() {
        dataManager = DataManager.getInstance();

        // If you added persistence methods in DataManager (loadData / saveData),
        // this will auto-load saved data without breaking compilation if you haven't.
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

    /**
     * Registration dialog:
     * - User does NOT enter ID (system generates it)
     * - Adds to DataManager lists
     * - Attempts to save via DataManager.saveData() if you implemented it
     */
    private void openRegisterDialog() {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();

        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Student", "Evaluator", "Coordinator"});

        JLabel extraLabel = new JLabel("Supervisor (Student):");
        JTextField extraField = new JTextField();

        roleBox.addActionListener(e -> {
            String role = (String) roleBox.getSelectedItem();
            if ("Student".equals(role)) extraLabel.setText("Supervisor (Student):");
            else if ("Evaluator".equals(role)) extraLabel.setText("Expertise (Evaluator):");
            else extraLabel.setText("Department (Coordinator):");
        });

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.add(new JLabel("Name:")); panel.add(nameField);
        panel.add(new JLabel("Email:")); panel.add(emailField);
        panel.add(new JLabel("Password:")); panel.add(passField);
        panel.add(new JLabel("Confirm Password:")); panel.add(confirmField);
        panel.add(new JLabel("Role:")); panel.add(roleBox);
        panel.add(extraLabel); panel.add(extraField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Register New User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passField.getPassword()).trim();
        String confirm = new String(confirmField.getPassword()).trim();
        String role = (String) roleBox.getSelectedItem();
        String extra = extraField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Generate NEW user ID automatically
        String newId = generateNextUserId(role);

        try {
            if ("Student".equals(role)) {
                if (extra.isEmpty()) extra = "Unknown Supervisor";
                dataManager.addStudent(new Student(newId, name, email, password, extra));

            } else if ("Evaluator".equals(role)) {
                if (extra.isEmpty()) extra = "General";
                dataManager.addEvaluator(new Evaluator(newId, name, email, password, extra));

            } else { // Coordinator
                if (extra.isEmpty()) extra = "Unknown Dept";
                dataManager.addCoordinator(new Coordinator(newId, name, email, password, extra));
            }

            // Try to persist if DataManager.saveData() exists
            invokeIfExists(dataManager, "saveData");

            JOptionPane.showMessageDialog(this,
                    "Registration successful!\nYour User ID is: " + newId + "\nYou can now log in.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Auto-fill login fields
            userIdField.setText(newId);
            passwordField.setText(password);
            roleComboBox.setSelectedItem(role);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Registration failed: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
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
                    String numPart = id.substring(1); // after prefix
                    try {
                        int n = Integer.parseInt(numPart);
                        if (n > max) max = n;
                    } catch (NumberFormatException ignored) {
                        // ignore non-numeric IDs
                    }
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
     * Calls a no-arg method if it exists (so your project still compiles even if you haven't added it yet).
     * Example: DataManager.saveData(), DataManager.loadData()
     */
    private static void invokeIfExists(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            m.invoke(target);
        } catch (NoSuchMethodException ignored) {
            // method doesn't exist (yet) -> do nothing
        } catch (Exception ex) {
            // method exists but failed -> show in console only
            System.err.println("Failed calling " + methodName + "(): " + ex.getMessage());
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
