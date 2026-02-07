package views;

import data.DataManager;
import models.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

// StudentDashboard - Interface for students to register and manage submissions

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
        
        // Set window icon
        setWindowIcon();
        
        initializeUI();
        loadSubmissions();
    }

    private void setWindowIcon() {
        try {
            BufferedImage img = ImageIO.read(new File("img/mmu.png"));
            setIconImage(img);
        } catch (Exception e) {
            // Icon file not found, continue without icon
        }
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
        tabbedPane.addTab("Submissions", createSubmissionsPanel());
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

        String[] columns = {"ID", "Title", "Type", "Session", "File", "Avg Score", "Evaluations"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        submissionTable = new JTable(tableModel);
        panel.add(new JScrollPane(submissionTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton viewButton = new JButton("View Details");
        JButton refreshButton = new JButton("Refresh");
        JButton feedbackButton = new JButton("View Feedback");
        JButton editButton = new  JButton("Edit Submission");
        JButton deleteButton = new JButton("Delete Submission");
        
        viewButton.addActionListener(e -> viewSubmissionDetails());
        refreshButton.addActionListener(e -> loadSubmissions());
        feedbackButton.addActionListener(e -> viewFeedback());
        editButton.addActionListener(e -> editSubmission());
        deleteButton.addActionListener(e -> deleteSubmission());

        buttonPanel.add(viewButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(feedbackButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        Font labelFont = new Font("Arial", Font.PLAIN, 14);
        Font textFont = new Font("Arial", Font.PLAIN, 14);
        Font buttonFont = new Font("Arial", Font.BOLD, 14);

        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel titleLabel = new JLabel("Research Title:");
        titleLabel.setFont(labelFont);
        panel.add(titleLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        titleField = new JTextField(30);
        titleField.setFont(textFont);
        panel.add(titleField, gbc);

        // Abstract
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel abstractLabel = new JLabel("Abstract:");
        abstractLabel.setFont(labelFont);
        panel.add(abstractLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        abstractArea = new JTextArea(5, 30);
        abstractArea.setLineWrap(true);
        abstractArea.setWrapStyleWord(true);
        abstractArea.setFont(textFont);
        panel.add(new JScrollPane(abstractArea), gbc);

        // Supervisor
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel supervisorLabel = new JLabel("Supervisor:");
        supervisorLabel.setFont(labelFont);
        panel.add(supervisorLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        supervisorField = new JTextField(student.getSupervisorName() == null ? "" : student.getSupervisorName());
        supervisorField.setFont(textFont);
        panel.add(supervisorField, gbc);

        // Presentation Type
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        JLabel typeLabel = new JLabel("Presentation Type:");
        typeLabel.setFont(labelFont);
        panel.add(typeLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        oralRadio = new JRadioButton("Oral");
        posterRadio = new JRadioButton("Poster");
        oralRadio.setFont(textFont);
        posterRadio.setFont(textFont);
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
        JLabel fileLabel = new JLabel("Upload File:");
        fileLabel.setFont(labelFont);
        panel.add(fileLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 1;
        fileField = new JTextField(22);
        fileField.setEditable(false);
        fileField.setFont(textFont);
        panel.add(fileField, gbc);

        gbc.gridx = 2; gbc.gridwidth = 1;
        JButton browseButton = new JButton("Browse");
        browseButton.setFont(buttonFont);
        browseButton.addActionListener(e -> browseFile());
        panel.add(browseButton, gbc);

        // Submit Button
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 2;
        JButton submitButton = new JButton("Register Submission");
        submitButton.setFont(buttonFont);
        submitButton.setBackground(new Color(46, 204, 113));
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(e -> registerSubmission());
        panel.add(submitButton, gbc);
        
        return panel;
    }

    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Filter for presentation files only
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Presentation Files (PPTX/PDF/ODP)", "pptx", "pdf", "odp"));

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

        // Validate title length
        if (title.length() < 10) {
            JOptionPane.showMessageDialog(this,
                    "Research Title must be at least 10 characters long.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate abstract length
        if (abstractText.length() < 50) {
            JOptionPane.showMessageDialog(this,
                    "Abstract must be at least 50 characters long.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate supervisor
        if (supervisor.isEmpty() || supervisor.length() < 5) {
            JOptionPane.showMessageDialog(this,
                    "Supervisor name must be at least 5 characters long.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate file type if provided
        if (!filePath.isEmpty()) {
            String extension = getFileExtension(filePath).toLowerCase();
            if (!isValidFileType(extension)) {
                JOptionPane.showMessageDialog(this,
                        "Invalid file type. Only .pptx, .pdf, and .odp files are allowed.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Submission submission = student.registerSubmission(title, abstractText, supervisor, type, filePath);

        // Update student's default supervisor name for next time
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

    private String getFileExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filePath.length() - 1) {
            return filePath.substring(lastDot + 1);
        }
        return "";
    }

    private boolean isValidFileType(String extension) {
        return extension.equals("pptx") || extension.equals("pdf") || extension.equals("odp");
    }

    private void loadSubmissions() {
        tableModel.setRowCount(0);
        for (Submission sub : student.getSubmissions()) {
            Session s = sub.getSession();
            String sessionInfo = (s == null)
                    ? "Not Assigned"
                    : s.getSessionId();

            tableModel.addRow(new Object[]{
                    sub.getSubmissionId(),
                    sub.getTitle(),
                    sub.getPresentationType(),
                    sessionInfo,
                    (sub.getFilePath() == null || sub.getFilePath().isEmpty()) ? "Not uploaded" : "Uploaded",
                    String.format("%.2f", sub.getAverageScore()),
                    sub.getEvaluations().size()
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

    private void viewFeedback() {
        int selectedRow = submissionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a submission", "No Selection", JOptionPane.WARNING_MESSAGE);
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
        
        if(submission == null || submission.getEvaluations().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No evaluation yet for this submission.", "No Feedback", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Create a pagination dialog for feedback
        showFeedbackPaginationDialog(submission);
    }

    private void showFeedbackPaginationDialog(Submission submission) {
        JDialog dialog = new JDialog(this, "Evaluation Feedback", true);
        dialog.setSize(650, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        List<Evaluation> evaluations = submission.getEvaluations();
        final int[] currentIndex = {0};

        JTextArea feedbackArea = new JTextArea();
        feedbackArea.setEditable(false);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel pageLabel = new JLabel("Evaluation 1 of " + evaluations.size());
        pageLabel.setFont(new Font("Arial", Font.BOLD, 12));
        pageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Display first evaluation
        if (!evaluations.isEmpty()) {
            feedbackArea.setText(evaluations.get(0).getDetails());
        }

        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        JButton closeButton = new JButton("Close");

        prevButton.addActionListener(e -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                feedbackArea.setText(evaluations.get(currentIndex[0]).getDetails());
                pageLabel.setText("Evaluation " + (currentIndex[0] + 1) + " of " + evaluations.size());
                prevButton.setEnabled(currentIndex[0] > 0);
                nextButton.setEnabled(currentIndex[0] < evaluations.size() - 1);
            }
        });

        nextButton.addActionListener(e -> {
            if (currentIndex[0] < evaluations.size() - 1) {
                currentIndex[0]++;
                feedbackArea.setText(evaluations.get(currentIndex[0]).getDetails());
                pageLabel.setText("Evaluation " + (currentIndex[0] + 1) + " of " + evaluations.size());
                prevButton.setEnabled(currentIndex[0] > 0);
                nextButton.setEnabled(currentIndex[0] < evaluations.size() - 1);
            }
        });

        closeButton.addActionListener(e -> dialog.dispose());

        // Disable previous button initially
        prevButton.setEnabled(false);
        // Disable next button if only one evaluation
        nextButton.setEnabled(evaluations.size() > 1);

        navigationPanel.add(prevButton);
        navigationPanel.add(pageLabel);
        navigationPanel.add(nextButton);
        navigationPanel.add(closeButton);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);
        mainPanel.add(navigationPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void editSubmission() {
        int selectedRow = submissionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a submission to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
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

        if (submission == null) return;

        //Block editing afte evaluation
        if (!submission.getEvaluations().isEmpty()) {
            JOptionPane.showMessageDialog(this, "This submission has already been evaluated and cannot be edited.", "Edit Not Allowed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //edit form
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15,20,15,20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        int row = 0;

        // ===== Research Title =====
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Research Title:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField titleField = new JTextField(submission.getTitle(), 25);
        panel.add(titleField, gbc);

        // ===== Abstract =====
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Abstract:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextArea abstractArea = new JTextArea(submission.getAbstractText(), 4, 25);
        abstractArea.setLineWrap(true);
        abstractArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(abstractArea), gbc);

        // ===== Supervisor =====
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Supervisor:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField supervisorField = new JTextField(submission.getSupervisorName(), 25);
        panel.add(supervisorField, gbc);

        // ===== Presentation Type =====
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Presentation Type:"), gbc);

        gbc.gridx = 1;
        JRadioButton oralRadio = new JRadioButton("Oral");
        JRadioButton posterRadio = new JRadioButton("Poster");

        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(oralRadio);
        typeGroup.add(posterRadio);

        if ("Poster".equalsIgnoreCase(submission.getPresentationType())) {
            posterRadio.setSelected(true);
        } else {
            oralRadio.setSelected(true);
        }

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        radioPanel.add(oralRadio);
        radioPanel.add(posterRadio);
        panel.add(radioPanel, gbc);

        // ===== File Upload =====
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Upload File:"), gbc);

        gbc.gridx = 1;
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JTextField fileField = new JTextField(
                submission.getFilePath() == null ? "" : submission.getFilePath(),
                18
        );
        fileField.setEditable(false);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setAcceptAllFileFilterUsed(true);
            chooser.addChoosableFileFilter(
                    new FileNameExtensionFilter("Presentation Files (PPTX/PDF/ODP)", "pptx", "pdf", "odp")
            );

            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File selected = chooser.getSelectedFile();
                fileField.setText(selected.getAbsolutePath());
            }
        });

        filePanel.add(fileField);
        filePanel.add(browseButton);
        panel.add(filePanel, gbc);

        // ===== Dialog =====
        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Submission",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String newTitle = titleField.getText().trim();
            String newAbstract = abstractArea.getText().trim();
            String newSupervisor = supervisorField.getText().trim();
            String newFilePath = fileField.getText().trim();

            // Validate title length
            if (newTitle.length() < 10) {
                JOptionPane.showMessageDialog(this,
                        "Research Title must be at least 10 characters long.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate abstract length
            if (newAbstract.length() < 50) {
                JOptionPane.showMessageDialog(this,
                        "Abstract must be at least 50 characters long.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate supervisor
            if (newSupervisor.isEmpty() || newSupervisor.length() < 5) {
                JOptionPane.showMessageDialog(this,
                        "Supervisor name must be at least 5 characters long.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate file type if provided
            if (!newFilePath.isEmpty()) {
                String extension = getFileExtension(newFilePath).toLowerCase();
                if (!isValidFileType(extension)) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid file type. Only .pptx, .pdf, and .odp files are allowed.",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            submission.setTitle(newTitle);
            submission.setAbstractText(newAbstract);
            submission.setSupervisorName(newSupervisor);
            submission.setPresentationType(oralRadio.isSelected() ? "Oral" : "Poster");
            submission.setFilePath(newFilePath);

            dataManager.saveToDisk();
            loadSubmissions();

            JOptionPane.showMessageDialog(this,
                    "Submission updated successfully.",
                    "Update Complete",
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

    private void deleteSubmission() {
        int selectedRow = submissionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a submission to delete.",
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

        if (submission == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete submission:\n\"" + submission.getTitle() + "\"?\n\nThis action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            student.getSubmissions().remove(submission);
            dataManager.getSubmissions().remove(submission);
            
            // Remove any evaluations associated with this submission
            List<Evaluation> evaluationsToRemove = new ArrayList<>(submission.getEvaluations());
            for (Evaluation eval : evaluationsToRemove) {
                dataManager.removeEvaluation(eval);
            }
            
            dataManager.saveToDisk();
            loadSubmissions();

            JOptionPane.showMessageDialog(this,
                    "Submission deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
