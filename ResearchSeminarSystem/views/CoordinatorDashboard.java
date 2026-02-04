package views;

import data.DataManager;
import models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
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
    
    private TableRowSorter<DefaultTableModel> submissionSorter;

    // Create session form fields
    private JTextField dateField;
    private JTextField timeField;     
    private JTextField venueField;
    private JComboBox<String> typeBox;
    
    private JPanel sessionsWrap;
    private JPanel submissionsWrap;
    private JPanel evaluatorsWrap;


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

        dateField = new JTextField(10);   
        timeField = new JTextField(8);    
        venueField = new JTextField(12);
        typeBox = new JComboBox<>(new String[]{"Oral", "Poster"});

        dateField.setToolTipText("Format: DD/MM/YYYY (e.g., 31/01/2026)");
        timeField.setToolTipText("Example: 10AM, 12PM");
        venueField.setToolTipText("Example: Hall 1");

        JButton createBtn = new JButton("Create");
        createBtn.setBackground(new Color(52, 152, 219));
        createBtn.setForeground(Color.WHITE);
        createBtn.addActionListener(e -> createSession());

        form.add(new JLabel("Date:"));
        form.add(dateField);

        form.add(new JLabel("Time:"));         
        form.add(timeField);                    
        form.add(new JLabel("Venue:"));
        form.add(venueField);
        form.add(new JLabel("Type:"));
        form.add(typeBox);
        form.add(createBtn);

        panel.add(form, BorderLayout.NORTH);

        // Center: 3 tables (sessions / submissions / evaluators)
        JPanel center = new JPanel(new GridLayout(1,3,10,10));

        // Sessions table
        String[] sCols = {"Session ID", "Date", "Time", "Venue", "Type"}; 
        sessionModel = new DefaultTableModel(sCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        sessionTable = new JTable(sessionModel);
        sessionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        sessionTable.getColumnModel().getColumn(0).setPreferredWidth(110); // Session ID
        sessionTable.getColumnModel().getColumn(1).setPreferredWidth(90);  // Date
        sessionTable.getColumnModel().getColumn(2).setPreferredWidth(60);  // Time
        sessionTable.getColumnModel().getColumn(3).setPreferredWidth(140); // Venue
        sessionTable.getColumnModel().getColumn(4).setPreferredWidth(60);  // Type

        sessionsWrap = wrap("Sessions", sessionTable);
        center.add(sessionsWrap);

        // Submissions table
        String[] subCols = {"Submission ID", "Title", "Type", "Assigned Session", "Avg Score", "File"};
        submissionModel = new DefaultTableModel(subCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        submissionTable = new JTable(submissionModel);
        submissionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        submissionTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        submissionTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        submissionTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        submissionTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        submissionTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        submissionTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        submissionsWrap = wrap("Submissions", submissionTable);
        center.add(submissionsWrap);
        submissionSorter = new TableRowSorter<>(submissionModel);
        submissionTable.setRowSorter(submissionSorter);
        
        submissionTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
                int modelRow = table.convertRowIndexToModel(row);
                Object assignedVal = submissionModel.getValueAt(modelRow, 3); // "Assigned Session"
        
                boolean notAssigned = assignedVal == null
                        || assignedVal.toString().equalsIgnoreCase("Not assigned");
        
                if (!isSelected) {
                    if (notAssigned) {
                        c.setBackground(new Color(255, 255, 180)); // 🌕 light yellow
                    } else {
                        c.setBackground(Color.WHITE); // no colour if already assigned
                    }
                }
        
                return c;
            }
        });


        // Evaluators table
        String[] eCols = {"Evaluator ID", "Name"};
        evaluatorModel = new DefaultTableModel(eCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        evaluatorTable = new JTable(evaluatorModel);
        evaluatorTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        evaluatorTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        evaluatorTable.getColumnModel().getColumn(1).setPreferredWidth(140);

        evaluatorsWrap = wrap("Evaluators", evaluatorTable);
        center.add(evaluatorsWrap);

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

    // Actions 
    private void createSession() {
        String date = dateField.getText().trim();
        String time = timeField.getText().trim();       
        String venue = venueField.getText().trim();
        String type = (String) typeBox.getSelectedItem();

        if (date.isEmpty() || venue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill date and venue.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Session session = coordinator.createSession(date, venue, type);

            session.setTime(time);

            dataManager.addSession(session);

            dataManager.saveToDisk();

            JOptionPane.showMessageDialog(this, "Session created!\nID: " + session.getSessionId(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            dateField.setText("");
            timeField.setText("");        
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
            String sType = session.getSessionType();
            String subType = submission.getPresentationType();
            if (sType != null && subType != null && !sType.equalsIgnoreCase(subType)) {
                JOptionPane.showMessageDialog(this,
                        "Type mismatch!\nSession: " + sType + "\nSubmission: " + subType,
                        "Cannot Assign", JOptionPane.ERROR_MESSAGE);
                return;
            }
        
            boolean ok = coordinator.assignSubmissionToSession(session, submission);
        
            if (!ok) {
                JOptionPane.showMessageDialog(this,
                        "Assignment failed. This session may not be managed by this coordinator.",
                        "Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
        
            dataManager.saveToDisk();
        
            JOptionPane.showMessageDialog(this,
                    "Assigned submission:\n" + submission.getTitle() +
                    "\n→ Session " + session.getSessionId() +
                    " (" + session.getDate() + " " + session.getTime() + ")",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                    
            loadSessions();
            loadSubmissions();  
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
            boolean ok = coordinator.assignEvaluatorToSession(session, eval);
        
            if (!ok) {
                JOptionPane.showMessageDialog(this,
                        "Assignment failed. This session may not be managed by this coordinator.",
                        "Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
        
            dataManager.saveToDisk();
        
            JOptionPane.showMessageDialog(this, "Evaluator assigned to session!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        
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

    // Load data 
    private void loadSessions() {
        sessionModel.setRowCount(0);
        for (Session s : dataManager.getSessions()) {
            if (s == null) continue;
            sessionModel.addRow(new Object[]{
                    s.getSessionId(),
                    s.getDate(),
                    s.getTime(),          // ✅ NEW
                    s.getVenue(),
                    s.getSessionType()
            });
        }
        if (sessionsWrap != null) {
            sessionsWrap.setBorder(BorderFactory.createTitledBorder("Sessions (" + sessionModel.getRowCount() + ")"));
        }
    }

    private void loadSubmissions() {
        submissionModel.setRowCount(0);
    
        for (Submission sub : dataManager.getSubmissions()) {
            if (sub == null) continue;
    
            // NEW: find which session this submission belongs to (if any)
            Session assigned = dataManager.findSessionBySubmissionId(sub.getSubmissionId());
            String assignedText = (assigned == null) ? "Not assigned" : assigned.getSessionId();
    
            submissionModel.addRow(new Object[]{
                    sub.getSubmissionId(),
                    sub.getTitle(),
                    sub.getPresentationType(),
                    assignedText, // NEW column
                    String.format("%.2f", sub.getAverageScore()),
                    (sub.getFilePath() == null || sub.getFilePath().isEmpty()) ? "Not uploaded" : "Uploaded"
            });
        }
        
        int total = submissionModel.getRowCount();
        int unassigned = 0;
        
        for (int i = 0; i < total; i++) {
            String assigned = String.valueOf(submissionModel.getValueAt(i, 3));
            if ("Not assigned".equalsIgnoreCase(assigned)) unassigned++;
        }
        
        if (submissionsWrap != null) {
            submissionsWrap.setBorder(
                BorderFactory.createTitledBorder("Submissions (" + total + ", Unassigned: " + unassigned + ")")
            );
        }

    }

    private void loadEvaluators() {
        evaluatorModel.setRowCount(0);
        for (Evaluator e : dataManager.getEvaluators()) {
            if (e == null) continue;
            evaluatorModel.addRow(new Object[]{
                    e.getUserId(),
                    e.getName()
            });
        }
        if (evaluatorsWrap != null) {
            evaluatorsWrap.setBorder(BorderFactory.createTitledBorder("Evaluators (" + evaluatorModel.getRowCount() + ")"));
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
