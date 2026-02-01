package views;

import data.DataManager;
import models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// CoordinatorDashboard - Interface for coordinators to manage sessions, assignments, reports and awards
public class CoordinatorDashboard extends JFrame {
    private Coordinator coordinator;
    private DataManager dataManager;

    private JTable sessionTable;
    private DefaultTableModel sessionModel;

    private JTable submissionTable;
    private DefaultTableModel submissionModel;

    private JTable evaluatorTable;
    private DefaultTableModel evaluatorModel;

    private JTextArea outputArea;

    // Create session form fields
    private JTextField dateField;
    private JTextField venueField;
    private JComboBox<String> typeBox;

    public CoordinatorDashboard(Coordinator coordinator) {
        this.coordinator = coordinator;
        this.dataManager = DataManager.getInstance();
        initializeUI();
        loadSessions();
        loadSubmissions();
        loadEvaluators();
    }

    private void initializeUI() {
        setTitle("Coordinator Dashboard - " + coordinator.getName());
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(231, 76, 60)); // red
        headerPanel.setPreferredSize(new Dimension(1100, 60));
        JLabel headerLabel = new JLabel("Coordinator Dashboard - " + coordinator.getName());
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Session Management", createSessionManagementPanel());
        tabbedPane.addTab("Reports", createReportsPanel());
        tabbedPane.addTab("Awards", createAwardsPanel());

        // Bottom
        JPanel bottomPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadSessions();
            loadSubmissions();
            loadEvaluators();
        });

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        bottomPanel.add(refreshButton);
        bottomPanel.add(logoutButton);

        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createSessionManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Top form: create session
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.setBorder(BorderFactory.createTitledBorder("Create New Session"));

        dateField = new JTextField(10);   // e.g. 2026-02-01
        venueField = new JTextField(12);
        typeBox = new JComboBox<>(new String[]{"Oral", "Poster"});

        JButton createBtn = new JButton("Create");
        createBtn.setBackground(new Color(52, 152, 219));
        createBtn.setForeground(Color.WHITE);
        createBtn.addActionListener(e -> createSession());

        form.add(new JLabel("Date:"));
        form.add(dateField);
        form.add(new JLabel("Venue:"));
        form.add(venueField);
        form.add(new JLabel("Type:"));
        form.add(typeBox);
        form.add(createBtn);

        panel.add(form, BorderLayout.NORTH);

        // Center: 3 tables (sessions / submissions / evaluators)
        JPanel center = new JPanel(new GridLayout(1,3,10,10));

        // Sessions table
        String[] sCols = {"Session ID", "Date", "Venue", "Type"};
        sessionModel = new DefaultTableModel(sCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        sessionTable = new JTable(sessionModel);
        center.add(wrap("Sessions", sessionTable));

        // Submissions table
        String[] subCols = {"Submission ID", "Title", "Type", "Avg Score", "File"};
        submissionModel = new DefaultTableModel(subCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        submissionTable = new JTable(submissionModel);
        center.add(wrap("Submissions", submissionTable));

        // Evaluators table
        String[] eCols = {"Evaluator ID", "Name"};
        evaluatorModel = new DefaultTableModel(eCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        evaluatorTable = new JTable(evaluatorModel);
        center.add(wrap("Evaluators", evaluatorTable));

        panel.add(center, BorderLayout.CENTER);

        // Bottom: assign buttons
        JPanel btnPanel = new JPanel();

        JButton assignSubmissionBtn = new JButton("Assign Submission to Session");
        assignSubmissionBtn.addActionListener(e -> assignSubmissionToSession());

        JButton assignEvaluatorBtn = new JButton("Assign Evaluator to Session");
        assignEvaluatorBtn.addActionListener(e -> assignEvaluatorToSession());

        JButton viewSessionBtn = new JButton("View Session Details");
        viewSessionBtn.addActionListener(e -> viewSessionDetails());

        btnPanel.add(viewSessionBtn);
        btnPanel.add(assignSubmissionBtn);
        btnPanel.add(assignEvaluatorBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        outputArea = new JTextArea();
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton scheduleBtn = new JButton("Generate Schedule Report");
        scheduleBtn.addActionListener(e -> generateScheduleReport());

        JButton evalBtn = new JButton("Generate Evaluation Report");
        evalBtn.addActionListener(e -> generateEvaluationReport());

        JButton exportBtn = new JButton("Export Output to File");
        exportBtn.addActionListener(e -> exportOutput());

        btnPanel.add(scheduleBtn);
        btnPanel.add(evalBtn);
        btnPanel.add(exportBtn);

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAwardsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JTextArea awardsArea = new JTextArea();
        awardsArea.setEditable(false);
        awardsArea.setLineWrap(true);
        awardsArea.setWrapStyleWord(true);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton computeBtn = new JButton("Compute Awards");
        computeBtn.setBackground(new Color(46, 204, 113));
        computeBtn.setForeground(Color.WHITE);

        computeBtn.addActionListener(e -> {
            try {
                java.util.List<Award> awards = coordinator.computeAwards(dataManager.getSubmissions());

                StringBuilder sb = new StringBuilder();
                for (Award a : awards) {
                    sb.append(a.getDetails()).append("\n\n");
                }
                awardsArea.setText(sb.toString());

            } catch (Exception ex) {
                awardsArea.setText("Failed to compute awards: " + ex.getMessage());
            }
        });

        btnPanel.add(computeBtn);
        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(awardsArea), BorderLayout.CENTER);

        return panel;
    }

    // ---------- Actions ----------
    private void createSession() {
        String date = dateField.getText().trim();
        String venue = venueField.getText().trim();
        String type = (String) typeBox.getSelectedItem();

        if (date.isEmpty() || venue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill date and venue.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Session session = coordinator.createSession(date, venue, type);
            dataManager.addSession(session);

            JOptionPane.showMessageDialog(this, "Session created!\nID: " + session.getSessionId(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            dateField.setText("");
            venueField.setText("");
            loadSessions();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to create session: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void assignSubmissionToSession() {
        int sRow = sessionTable.getSelectedRow();
        int subRow = submissionTable.getSelectedRow();

        if (sRow == -1 || subRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a session and a submission first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sessionId = (String) sessionModel.getValueAt(sRow, 0);
        String submissionId = (String) submissionModel.getValueAt(subRow, 0);

        Session session = dataManager.findSessionById(sessionId);
        Submission submission = dataManager.findSubmissionById(submissionId);

        if (session == null || submission == null) return;

        try {
            coordinator.assignSubmissionToSession(session, submission);

            // ✅ IMPORTANT: persist updated session assignments
            dataManager.saveToDisk();

            JOptionPane.showMessageDialog(this, "Submission assigned to session!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            // refresh
            loadSessions();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to assign submission: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void assignEvaluatorToSession() {
        int sRow = sessionTable.getSelectedRow();
        int eRow = evaluatorTable.getSelectedRow();

        if (sRow == -1 || eRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a session and an evaluator first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sessionId = (String) sessionModel.getValueAt(sRow, 0);
        String evaluatorId = (String) evaluatorModel.getValueAt(eRow, 0);

        Session session = dataManager.findSessionById(sessionId);
        Evaluator eval = dataManager.findEvaluatorById(evaluatorId);

        if (session == null || eval == null) return;

        try {
            coordinator.assignEvaluatorToSession(session, eval);

            // ✅ IMPORTANT: persist updated session assignments
            dataManager.saveToDisk();

            JOptionPane.showMessageDialog(this, "Evaluator assigned to session!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            // refresh
            loadSessions();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to assign evaluator: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewSessionDetails() {
        int row = sessionTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a session.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sessionId = (String) sessionModel.getValueAt(row, 0);
        Session session = dataManager.findSessionById(sessionId);

        if (session != null) {
            JOptionPane.showMessageDialog(this, session.getDetails(),
                    "Session Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void generateScheduleReport() {
        try {
            Report r = coordinator.generateScheduleReport(dataManager.getSessions());
            outputArea.setText(r.getDetails());
        } catch (Exception ex) {
            outputArea.setText("Failed to generate schedule report: " + ex.getMessage());
        }
    }

    private void generateEvaluationReport() {
        try {
            Report r = coordinator.generateEvaluationReport(dataManager.getSessions());
            outputArea.setText(r.getDetails());
        } catch (Exception ex) {
            outputArea.setText("Failed to generate evaluation report: " + ex.getMessage());
        }
    }

    private void exportOutput() {
        String content = outputArea.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nothing to export yet.",
                    "Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String filename = JOptionPane.showInputDialog(this, "Enter filename:", "report.txt");
        if (filename == null || filename.trim().isEmpty()) return;

        boolean ok = Report.exportTextToFile(content, filename);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Exported to " + filename,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Export failed.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Load data ----------
    private void loadSessions() {
        sessionModel.setRowCount(0);
        for (Session s : dataManager.getSessions()) {
            sessionModel.addRow(new Object[]{
                    s.getSessionId(),
                    s.getDate(),
                    s.getVenue(),
                    s.getSessionType()
            });
        }
    }

    private void loadSubmissions() {
        submissionModel.setRowCount(0);
        for (Submission sub : dataManager.getSubmissions()) {
            submissionModel.addRow(new Object[]{
                    sub.getSubmissionId(),
                    sub.getTitle(),
                    sub.getPresentationType(),
                    String.format("%.2f", sub.getAverageScore()),
                    (sub.getFilePath() == null || sub.getFilePath().isEmpty()) ? "Not uploaded" : "Uploaded"
            });
        }
    }

    private void loadEvaluators() {
        evaluatorModel.setRowCount(0);
        for (Evaluator e : dataManager.getEvaluators()) {
            evaluatorModel.addRow(new Object[]{
                    e.getUserId(),
                    e.getName()
            });
        }
    }

    private JPanel wrap(String title, JTable table) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
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






