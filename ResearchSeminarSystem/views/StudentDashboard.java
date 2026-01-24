package views;

import data.DataManager;
import models.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

/**
 * StudentDashboard - Interface for students to register and manage submissions
 */
public class StudentDashboard extends JFrame {
    private final Student student;
    private final DataManager dataManager;

    private JTable submissionTable;
    private DefaultTableModel tableModel;

    // keep these as fields so they exist reliably
    private JTextField titleField;
    private JTextArea abstractArea;
    private JTextField supervisorField;
    private JTextField fileField;
    private JRadioButton oralRadio;
    private JRadioButton posterRadio;

    public StudentDashboard(Student student) {
        this.student = student;
        this.dataManager = DataManager.getInstance();
        initializeUI();
        loadSubmissions();
    }

    private void initializeUI() {
        setTitle("Student Dashboard - " + student.getName());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 152, 219));
        headerPanel.setPreferredSize(new Dimension(900, 60));
        JLabel headerLabel = new JLabel("Student Dashboard - " + student.getName());
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("My Submissions", createSubmissionsPanel());
        tabbedPane.addTab("New Registration", createRegistrationPanel());

        // Bottom
        JPanel bottomPanel = new JPanel();
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        bottomPanel.add(logoutButton);

        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createSubmissionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "Title", "Type", "File", "Avg Score", "Evaluations"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        submissionTable = new JTable(tableModel);
        panel.add(new JScrollPane(submissionTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton viewButton = new JButton("View Details");
        JButton refreshButton = new JButton("Refresh");

        viewButton.addActionListener(e -> viewSubmissionDetails());
        refreshButton.addActionListener(e -> loadSubmissions());

        buttonPanel.add(viewButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Research Title:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        titleField = new JTextField(30);
        panel.add(titleField, gbc);

        // Abstract
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("Abstract:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        abstractArea = new JTextArea(5, 30);
        abstractArea.setLineWrap(true);
        abstractArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(abstractArea), gbc);

        // Supervisor
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("Supervisor:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        supervisorField = new JTextField(student.getSupervisorName() == null ? "" : student.getSupervisorName());
        panel.add(supervisorField, gbc);

        // Presentation Type
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("Presentation Type:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        oralRadio = new JRadioButton("Oral");
        posterRadio = new JRadioButton("Poster");
        oralRadio.setSelected(true);

        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(oralRadio);
        typeGroup.add(posterRadio);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(oralRadio);
        radioPanel.add(posterRadio);
        panel.add(radioPanel, gbc);

        // File Upload
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("Upload File:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 1;
        fileField = new JTextField(22);
        fileField.setEditable(false);
        panel.add(fileField, gbc);

        gbc.gridx = 2; gbc.gridwidth = 1;
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> browseFile());
        panel.add(browseButton, gbc);

        // Submit Button
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 2;
        JButton submitButton = new JButton("Register Submission");
        submitButton.setBackground(new Color(46, 204, 113));
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(e -> registerSubmission());
        panel.add(submitButton, gbc);

        return panel;
    }

    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // optional: filter common document types
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Documents (PDF/DOC/DOCX)", "pdf", "doc", "docx"));

        int result = chooser.showOpenDialog(StudentDashboard.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            if (selected != null) {
                fileField.setText(selected.getAbsolutePath());
            }
        }
    }

    private void registerSubmission() {
        String title = titleField.getText().trim();
        String abstractText = abstractArea.getText().trim();
        String supervisor = supervisorField.getText().trim();
        String type = oralRadio.isSelected() ? "Oral" : "Poster";
        String filePath = fileField.getText().trim();

        if (title.isEmpty() || abstractText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields (Title + Abstract).",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ✅ IMPORTANT: supervisor is stored in Submission now
        Submission submission = student.registerSubmission(title, abstractText, supervisor, type, filePath);

        // (optional) also update student's default supervisor name for next time
        student.setSupervisorName(supervisor);

        dataManager.addSubmission(submission); // saves to disk

        JOptionPane.showMessageDialog(this,
                "Submission registered successfully!\nID: " + submission.getSubmissionId(),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

        // Clear fields
        titleField.setText("");
        abstractArea.setText("");
        fileField.setText("");
        oralRadio.setSelected(true);

        loadSubmissions();
    }

    private void loadSubmissions() {
        tableModel.setRowCount(0);
        for (Submission sub : student.getSubmissions()) {
            tableModel.addRow(new Object[]{
                    sub.getSubmissionId(),
                    sub.getTitle(),
                    sub.getPresentationType(),
                    (sub.getFilePath() == null || sub.getFilePath().isEmpty()) ? "Not uploaded" : "Uploaded",
                    String.format("%.2f", sub.getAverageScore()),
                    (sub.getEvaluations() == null) ? 0 : sub.getEvaluations().size()
            });
        }
    }

    private void viewSubmissionDetails() {
        int selectedRow = submissionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a submission",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String submissionId = (String) tableModel.getValueAt(selectedRow, 0);
        Submission submission = null;

        for (Submission sub : student.getSubmissions()) {
            if (sub != null && submissionId.equals(sub.getSubmissionId())) {
                submission = sub;
                break;
            }
        }

        if (submission != null) {
            JOptionPane.showMessageDialog(this,
                    submission.getDetails(),
                    "Submission Details",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
