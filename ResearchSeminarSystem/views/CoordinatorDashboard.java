package views;

import data.DataManager;
import models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

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
    
    private JComboBox<String> scheduleSessionCombo;
    private JComboBox<String> evaluationSessionCombo;
    private JComboBox<String> awardsSessionCombo;
    
    private JPanel sessionsWrap;
    private JPanel submissionsWrap;
    private JPanel evaluatorsWrap;


    public CoordinatorDashboard(Coordinator coordinator) {
        this.coordinator = coordinator;
        this.dataManager = DataManager.getInstance();
        
        // Set window icon
        setWindowIcon();
        
        initializeUI();
        loadSessions();
        loadSubmissions();
        loadEvaluators();
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
        setTitle("Coordinator Dashboard - " + coordinator.getName());
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(192, 57, 43));
        headerPanel.setPreferredSize(new Dimension(1100, 70));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("Coordinator Dashboard - " + coordinator.getName());
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 13));
        tabbedPane.addTab("Session Management", createSessionManagementPanel());
        tabbedPane.addTab("Reports", createReportsPanel());
        tabbedPane.addTab("Awards", createAwardsPanel());

        // Bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomPanel.setBackground(new Color(236, 240, 241));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)));
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 12));
        refreshButton.setBackground(new Color(52, 152, 219));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setPreferredSize(new Dimension(100, 35));
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> {
            loadSessions();
            loadSubmissions();
            loadEvaluators();
        });

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setPreferredSize(new Dimension(100, 35));
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        form.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)), "Create New Session", 0, 0, new Font("Arial", Font.BOLD, 12)));
        form.setBackground(new Color(245, 245, 245));

        dateField = new JTextField(10);   
        timeField = new JTextField(8);    
        venueField = new JTextField(12);
        typeBox = new JComboBox<>(new String[]{"Oral", "Poster"});
        
        for (JTextField tf : new JTextField[]{dateField, timeField, venueField}) {
            tf.setFont(new Font("Arial", Font.PLAIN, 12));
            tf.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        }
        
        typeBox.setFont(new Font("Arial", Font.PLAIN, 12));

        dateField.setToolTipText("Format: DD/MM/YYYY (e.g., 31/01/2026)");
        timeField.setToolTipText("Example: 10AM, 12PM");
        venueField.setToolTipText("Example: Hall 1");

        JButton createBtn = new JButton("Create");
        createBtn.setBackground(new Color(46, 204, 113));
        createBtn.setForeground(Color.WHITE);
        createBtn.setFont(new Font("Arial", Font.BOLD, 12));
        createBtn.setFocusPainted(false);
        createBtn.setBorderPainted(false);
        createBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        sessionTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sessionTable.getColumnModel().getColumn(0).setPreferredWidth(110); // Session ID
        sessionTable.getColumnModel().getColumn(1).setPreferredWidth(90);  // Date
        sessionTable.getColumnModel().getColumn(2).setPreferredWidth(60);  // Time
        sessionTable.getColumnModel().getColumn(3).setPreferredWidth(140); // Venue
        sessionTable.getColumnModel().getColumn(4).setPreferredWidth(60);  // Type

        sessionsWrap = wrap("Sessions", sessionTable);
        center.add(sessionsWrap);
        
        sessionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openDetailsDialog();
            }
        });

        // Submissions table
        String[] subCols = {"Submission ID", "Title", "Type", "Assigned Session", "Board ID", "Avg Score", "File"};
        submissionModel = new DefaultTableModel(subCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        submissionTable = new JTable(submissionModel);
        submissionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        submissionTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        submissionTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        submissionTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        submissionTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        submissionTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        submissionTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        submissionTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        submissionTable.getColumnModel().getColumn(6).setPreferredWidth(80); // File

        submissionsWrap = wrap("Submissions", submissionTable);
        center.add(submissionsWrap);
        submissionSorter = new TableRowSorter<>(submissionModel);
        submissionTable.setRowSorter(submissionSorter);
        
        submissionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openDetailsDialog();
            }
        });
        
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
        evaluatorTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        evaluatorTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        evaluatorTable.getColumnModel().getColumn(1).setPreferredWidth(140);

        evaluatorsWrap = wrap("Evaluators", evaluatorTable);
        center.add(evaluatorsWrap);
        
        evaluatorTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openDetailsDialog();
            }
        });

        panel.add(center, BorderLayout.CENTER);

        // Bottom: assign buttons
        JPanel btnPanel = new JPanel(new BorderLayout(8, 8));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btnPanel.setBackground(new Color(236, 240, 241));
        
        // Top row: primary actions
        JPanel primary = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        primary.setOpaque(false);
        JButton assignSubmissionBtn = new JButton("Assign Submission to Session");
        JButton assignEvaluatorBtn = new JButton("Assign Evaluator to Session");
        
        for (JButton btn : new JButton[]{assignSubmissionBtn, assignEvaluatorBtn}) {
            btn.setFont(new Font("Arial", Font.BOLD, 13));
            btn.setBackground(new Color(52, 152, 219));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setPreferredSize(new Dimension(200, 35));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        primary.add(assignSubmissionBtn);
        primary.add(assignEvaluatorBtn);
        
        // Bottom row: secondary actions
        JPanel secondary = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        secondary.setOpaque(false);
        
        JButton viewDetailsBtn = new JButton("View Details");
        viewDetailsBtn.setFont(new Font("Arial", Font.BOLD, 13));
        viewDetailsBtn.setBackground(new Color(149, 165, 166));
        viewDetailsBtn.setForeground(Color.WHITE);
        viewDetailsBtn.setFocusPainted(false);
        viewDetailsBtn.setBorderPainted(false);
        viewDetailsBtn.setPreferredSize(new Dimension(140, 35));
        viewDetailsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        secondary.add(viewDetailsBtn);
        
        btnPanel.add(primary, BorderLayout.NORTH);
        btnPanel.add(secondary, BorderLayout.SOUTH);
        
        // listeners
        assignSubmissionBtn.addActionListener(e -> assignSubmissionToSession());
        assignEvaluatorBtn.addActionListener(e -> assignEvaluatorToSession());
        viewDetailsBtn.addActionListener(e -> openDetailsDialog());
        
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

        private JPanel createReportsPanel() {
            JPanel panel = new JPanel(new BorderLayout(10,10));
            panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    
            outputArea = new JTextArea();
            outputArea.setLineWrap(true);
            outputArea.setWrapStyleWord(true);
    
            // Schedule Report Section
            JPanel schedulePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            schedulePanel.setBorder(BorderFactory.createTitledBorder("Schedule Report"));
            scheduleSessionCombo = new JComboBox<>();
            JButton scheduleBtn = new JButton("Generate");
            scheduleBtn.addActionListener(e -> generateScheduleReport());
            schedulePanel.add(new JLabel("Session:"));
            schedulePanel.add(scheduleSessionCombo);
            schedulePanel.add(scheduleBtn);

            // Evaluation Report Section
            JPanel evalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            evalPanel.setBorder(BorderFactory.createTitledBorder("Evaluation Report"));
            evaluationSessionCombo = new JComboBox<>();
            JButton evalBtn = new JButton("Generate");
            evalBtn.addActionListener(e -> generateEvaluationReport());
            evalPanel.add(new JLabel("Session:"));
            evalPanel.add(evaluationSessionCombo);
            evalPanel.add(evalBtn);

            // Export
            JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            JButton exportBtn = new JButton("Export Output to File");
            exportBtn.addActionListener(e -> exportOutput());
            exportPanel.add(exportBtn);

            // Top controls
            JPanel topPanel = new JPanel(new BorderLayout(10, 10));
            topPanel.add(schedulePanel, BorderLayout.NORTH);
            JPanel middle = new JPanel(new BorderLayout());
            middle.add(evalPanel, BorderLayout.NORTH);
            middle.add(exportPanel, BorderLayout.CENTER);
            topPanel.add(middle, BorderLayout.CENTER);

            panel.add(topPanel, BorderLayout.NORTH);
            panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
    
            return panel;
        }

        private JPanel createAwardsPanel() {
            JPanel panel = new JPanel(new BorderLayout(12, 12));
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
            // Top toolbar
            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            JLabel title = new JLabel("Awards Results");
            title.setFont(new Font("Arial", Font.BOLD, 16));
            top.add(title);
        
            awardsSessionCombo = new JComboBox<>();
            JButton computeBtn = new JButton("Compute Awards");
            computeBtn.setBackground(new Color(46, 204, 113));
            computeBtn.setForeground(Color.WHITE);
            computeBtn.setFocusPainted(false);
        
            top.add(new JLabel("Session:"));
            top.add(awardsSessionCombo);
            top.add(computeBtn);
            panel.add(top, BorderLayout.NORTH);
        
            // Cards container
            JPanel cards = new JPanel(new GridLayout(1, 3, 12, 12));
        
            AwardCard oralCard = new AwardCard("Best Oral");
            AwardCard posterCard = new AwardCard("Best Poster");
            AwardCard peopleCard = new AwardCard("People's Choice");
        
            cards.add(oralCard);
            cards.add(posterCard);
            cards.add(peopleCard);
        
            panel.add(cards, BorderLayout.CENTER);
        
            // Button action
            computeBtn.addActionListener(e -> {
                List<Session> sessions = dataManager.getSessions();
                if (sessions.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No sessions available.",
                            "No Data", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                try {
                    int selectedIndex = awardsSessionCombo.getSelectedIndex();
                    List<Session> selectedSessions = new ArrayList<>();
                    if (selectedIndex == 0) {
                        selectedSessions.addAll(sessions);
                    } else {
                        selectedSessions.add(sessions.get(selectedIndex - 1));
                    }

                    // Get eligible submissions from selected sessions
                    java.util.List<Submission> eligible = new ArrayList<>();
                    for (Session s : selectedSessions) {
                        for (Submission sub : s.getSubmissions()) {
                            if (sub != null && sub.getEvaluations() != null && !sub.getEvaluations().isEmpty()) {
                                eligible.add(sub);
                            }
                        }
                    }
        
                    if (eligible.isEmpty()) {
                        oralCard.setEmpty("No eligible submissions in selected session(s).");
                        posterCard.setEmpty("No eligible submissions in selected session(s).");
                        peopleCard.setEmpty("No eligible submissions in selected session(s).");
                        return;
                    }
        
                    java.util.List<Award> awards = coordinator.computeAwards(eligible);
        
                    // reset all first
                    oralCard.setEmpty("Not computed.");
                    posterCard.setEmpty("Not computed.");
                    peopleCard.setEmpty("Not computed.");
        
                    // fill based on award type
                    for (Award a : awards) {
                        if (a == null) continue;
                        String type = a.getAwardType();
        
                        if ("Best Oral".equalsIgnoreCase(type)) {
                            oralCard.setAward(a, false); // false = average metric
                        } else if ("Best Poster".equalsIgnoreCase(type)) {
                            posterCard.setAward(a, false);
                        } else if ("People's Choice".equalsIgnoreCase(type)) {
                            peopleCard.setAward(a, true); // true = total marks metric
                        }
                    }
        
                } catch (Exception ex) {
                    oralCard.setEmpty("Failed: " + ex.getMessage());
                    posterCard.setEmpty("Failed: " + ex.getMessage());
                    peopleCard.setEmpty("Failed: " + ex.getMessage());
                }
            });
        
            return panel;
        }
        
        /** Simple UI card for one award */
        private static class AwardCard extends JPanel {
            private final JLabel awardTitle;
            private final JLabel winnerName;
            private final JLabel submissionTitle;
            private final JLabel submissionId;
            private final JLabel scoreLabel;
        
            public AwardCard(String title) {
                setLayout(new BorderLayout(8, 8));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(210, 210, 210)),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12)
                ));
                setBackground(Color.WHITE);
        
                awardTitle = new JLabel(title);
                awardTitle.setFont(new Font("Arial", Font.BOLD, 14));
        
                winnerName = new JLabel("Winner: -");
                submissionTitle = new JLabel("Submission: -");
                submissionId = new JLabel("ID: -");
                scoreLabel = new JLabel("Score: -");
        
                winnerName.setFont(new Font("Arial", Font.PLAIN, 13));
                submissionTitle.setFont(new Font("Arial", Font.PLAIN, 13));
                submissionId.setFont(new Font("Arial", Font.PLAIN, 12));
                scoreLabel.setFont(new Font("Arial", Font.BOLD, 13));
        
                JPanel body = new JPanel();
                body.setOpaque(false);
                body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        
                body.add(winnerName);
                body.add(Box.createVerticalStrut(6));
                body.add(submissionTitle);
                body.add(Box.createVerticalStrut(6));
                body.add(submissionId);
                body.add(Box.createVerticalStrut(12));
                body.add(scoreLabel);
        
                add(awardTitle, BorderLayout.NORTH);
                add(body, BorderLayout.CENTER);
            }
        
            public void setEmpty(String msg) {
                winnerName.setText(msg);
                submissionTitle.setText("");
                submissionId.setText("");
                scoreLabel.setText("");
            }
        
            /** isPeopleChoice=true => show Total Marks label */
            public void setAward(Award a, boolean isPeopleChoice) {
                if (a == null || a.getWinner() == null) {
                    setEmpty("Winner: Not yet determined");
                    return;
                }
        
                Submission w = a.getWinner();
                String student = (w.getStudentName() == null) ? "Unknown" : w.getStudentName();
                String title = (w.getTitle() == null) ? "" : w.getTitle();
        
                winnerName.setText("Winner: " + student);
                submissionTitle.setText("Submission: " + title);
                submissionId.setText("ID: " + w.getSubmissionId());
        
                String label = isPeopleChoice ? "Total Marks" : "Average Score";
                scoreLabel.setText(label + ": " + String.format("%.2f", a.getWinningScore()));
            }
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

        // Validate date format (DD/MM/YYYY)
        if (!date.matches("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$")) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use DD/MM/YYYY (e.g., 31/01/2026).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate time format if provided (basic validation like 10AM, 2:30PM, 14:30, etc.)
        if (!time.isEmpty() && !isValidTimeFormat(time)) {
            JOptionPane.showMessageDialog(this,
                    "Invalid time format. Examples: 10AM, 2:30PM, 14:30, 9:00",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(this,
                "Create session on " + date + "?\n\nWarning: This action cannot be undone.",
                "Confirm Session Creation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

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

    private boolean isValidTimeFormat(String time) {
        // Accept formats like: 10AM, 10:30AM, 14:30, 2:30PM, 09:00, etc.
        return time.matches("^([0-1]?[0-9]|2[0-3])(:[0-5][0-9])?(AM|PM|am|pm)?$");
    }
    
    private void editSelectedSession() {
        int row = sessionTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a session first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        String sessionId = (String) sessionModel.getValueAt(row, 0);
        Session session = dataManager.findSessionById(sessionId);
        if (session == null) return;
    
        JTextField newDate = new JTextField(session.getDate());
        JTextField newTime = new JTextField(session.getTime());
        JTextField newVenue = new JTextField(session.getVenue());
        JComboBox<String> newType = new JComboBox<>(new String[]{"Oral", "Poster"});
        newType.setSelectedItem(session.getSessionType());
        newType.setEnabled(false); 
        newType.setToolTipText("Session type cannot be changed after creation");
    
        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.add(new JLabel("Date:")); form.add(newDate);
        form.add(new JLabel("Time:")); form.add(newTime);
        form.add(new JLabel("Venue:")); form.add(newVenue);
        form.add(new JLabel("Type:")); form.add(newType);
    
        int result = JOptionPane.showConfirmDialog(this, form,
                "Edit Session " + sessionId, JOptionPane.OK_CANCEL_OPTION);
    
        if (result == JOptionPane.OK_OPTION) {
            String newDateText = newDate.getText().trim();
            String newTimeText = newTime.getText().trim();
            String newVenueText = newVenue.getText().trim();

            // Validate date format (DD/MM/YYYY)
            if (!newDateText.matches("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$")) {
                JOptionPane.showMessageDialog(this,
                        "Invalid date format. Please use DD/MM/YYYY (e.g., 31/01/2026).",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate time format if provided
            if (!newTimeText.isEmpty() && !isValidTimeFormat(newTimeText)) {
                JOptionPane.showMessageDialog(this,
                        "Invalid time format. Examples: 10AM, 2:30PM, 14:30, 9:00",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            session.setDate(newDateText);
            session.setTime(newTimeText);
            session.setVenue(newVenueText);
    
            dataManager.saveToDisk();
            loadSessions();
            loadSubmissions();
            JOptionPane.showMessageDialog(this, "Session updated.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedSession() {
        int row = sessionTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a session first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sessionId = (String) sessionModel.getValueAt(row, 0);
        Session session = dataManager.findSessionById(sessionId);
        if (session == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete session:\n\"" + sessionId + "\" (" + session.getDate() + ")?\n\nThis will unassign all submissions and evaluators from this session.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Unassign all submissions from this session
            for (Submission sub : session.getSubmissions()) {
                sub.setSession(null);
            }
            session.getSubmissions().clear();

            // Unassign all evaluators from this session
            session.getEvaluators().clear();

            // Remove session from datastore
            dataManager.getSessions().remove(session);
            dataManager.saveToDisk();
            loadSessions();
            loadSubmissions();
            loadEvaluators();

            JOptionPane.showMessageDialog(this,
                    "Session deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void unassignSubmissionFromSession() {
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

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to unassign this submission from session " + sessionId + "?",
                "Confirm Unassign",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            session.removeSubmission(submission);
            submission.setSession(null);

            dataManager.saveToDisk();
            loadSessions();
            loadSubmissions();

            JOptionPane.showMessageDialog(this,
                    "Submission unassigned successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void unassignEvaluatorFromSession() {
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

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to unassign this evaluator from session " + sessionId + "?",
                "Confirm Unassign",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            session.removeEvaluator(eval);

            dataManager.saveToDisk();
            loadSessions();

            JOptionPane.showMessageDialog(this,
                    "Evaluator unassigned successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void assignSubmissionToSession() {
        int[] sessionRows = sessionTable.getSelectedRows();
        int[] submissionRows = submissionTable.getSelectedRows();

        if (sessionRows.length == 0 || submissionRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Select at least one session and one submission.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sessionId = (String) sessionModel.getValueAt(sessionRows[0], 0);
        Session session = dataManager.findSessionById(sessionId);
        
        if (session == null) return;

        int assignedCount = 0;
        int failedCount = 0;
        StringBuilder failureReasons = new StringBuilder();
        
        for (int subRow : submissionRows) {
            String submissionId = (String) submissionModel.getValueAt(subRow, 0);
            Submission submission = dataManager.findSubmissionById(submissionId);

            if (submission == null) {
                failedCount++;
                continue;
            }

            try {
                String sType = session.getSessionType();
                String subType = submission.getPresentationType();
                if (sType != null && subType != null && !sType.equalsIgnoreCase(subType)) {
                    failureReasons.append("✗ ").append(submission.getTitle())
                            .append(" (Type mismatch: ").append(subType).append(")\n");
                    failedCount++;
                    continue;
                }
            
                Session already = dataManager.findSessionBySubmissionId(submission.getSubmissionId());
                if (already != null) {
                    failureReasons.append("✗ ").append(submission.getTitle())
                            .append(" (Already assigned to ").append(already.getSessionId()).append(")\n");
                    failedCount++;
                    continue;
                }
                
                // Check if another submission from the same student is already in this session
                Student submissionStudent = submission.getStudent();
                if (submissionStudent != null) {
                    boolean duplicateStudentFound = false;
                    for (Submission sessionSub : session.getSubmissions()) {
                        if (sessionSub.getStudent() != null && 
                            sessionSub.getStudent().getUserId().equals(submissionStudent.getUserId()) &&
                            !sessionSub.getSubmissionId().equals(submission.getSubmissionId())) {
                            failureReasons.append("✗ ").append(submission.getTitle())
                                    .append(" (Student already has submission in this session)\n");
                            failedCount++;
                            duplicateStudentFound = true;
                            break;
                        }
                    }
                    if (duplicateStudentFound) continue;
                }

                boolean ok = coordinator.assignSubmissionToSession(session, submission);
            
                if (!ok) {
                    failureReasons.append("✗ ").append(submission.getTitle()).append(" (Assignment failed)\n");
                    failedCount++;
                    continue;
                }
                
                assignedCount++;
        
            } catch (Exception ex) {
                failureReasons.append("✗ ").append(submission.getTitle()).append(" (").append(ex.getMessage()).append(")\n");
                failedCount++;
            }
        }
        
        dataManager.saveToDisk();
        loadSessions();
        loadSubmissions();
        
        String message = "Assigned: " + assignedCount + " submission(s)\n";
        if (failedCount > 0) {
            message += "Failed: " + failedCount + "\n\n" + failureReasons.toString();
        }
        
        JOptionPane.showMessageDialog(this, message, "Assignment Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    private void assignEvaluatorToSession() {
        int[] sessionRows = sessionTable.getSelectedRows();
        int[] evaluatorRows = evaluatorTable.getSelectedRows();

        if (sessionRows.length == 0 || evaluatorRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Select at least one session and one evaluator.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sessionId = (String) sessionModel.getValueAt(sessionRows[0], 0);
        Session session = dataManager.findSessionById(sessionId);

        if (session == null) return;

        int assignedCount = 0;
        int failedCount = 0;
        StringBuilder failureReasons = new StringBuilder();
        
        for (int eRow : evaluatorRows) {
            String evaluatorId = (String) evaluatorModel.getValueAt(eRow, 0);
            Evaluator eval = dataManager.findEvaluatorById(evaluatorId);

            if (eval == null) {
                failedCount++;
                continue;
            }

            try {
                boolean ok = coordinator.assignEvaluatorToSession(session, eval);
            
                if (!ok) {
                    failureReasons.append("✗ ").append(eval.getName()).append(" (Assignment failed)\n");
                    failedCount++;
                    continue;
                }
                
                assignedCount++;
        
            } catch (Exception ex) {
                failureReasons.append("✗ ").append(eval.getName()).append(" (").append(ex.getMessage()).append(")\n");
                failedCount++;
            }
        }
        
        dataManager.saveToDisk();
        loadSessions();
        loadEvaluators();
        
        String message = "Assigned: " + assignedCount + " evaluator(s)\n";
        if (failedCount > 0) {
            message += "Failed: " + failedCount + "\n\n" + failureReasons.toString();
        }
        
        JOptionPane.showMessageDialog(this, message, "Assignment Summary", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openDetailsDialog() {
        JDialog dialog = new JDialog(this, "Details", true);
        dialog.setSize(650, 450);
        dialog.setLocationRelativeTo(this);
    
        JTabbedPane tabs = new JTabbedPane();
    
        tabs.addTab("Session", buildSessionDetailsPanel());
        tabs.addTab("Submission / Presenter", buildSubmissionDetailsPanel());
        tabs.addTab("Evaluator", buildEvaluatorDetailsPanel());
    
        dialog.add(tabs);
        dialog.setVisible(true);
    }
    
    private JPanel buildSessionDetailsPanel() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    
        int row = sessionTable.getSelectedRow();
        if (row == -1) {
            area.setText("Select a session in the Sessions table first.");
        } else {
            String sessionId = String.valueOf(sessionModel.getValueAt(row, 0));
            Session session = dataManager.findSessionById(sessionId);
            area.setText(session == null ? "Session not found." : session.getDetails());
        }
    
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }
    
    private JPanel buildSubmissionDetailsPanel() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    
        int viewRow = submissionTable.getSelectedRow();
        if (viewRow == -1) {
            area.setText("Select a submission in the Submissions table first.");
        } else {
            int modelRow = submissionTable.convertRowIndexToModel(viewRow);
            String submissionId = String.valueOf(submissionModel.getValueAt(modelRow, 0));
            Submission sub = dataManager.findSubmissionById(submissionId);
    
            if (sub == null) {
                area.setText("Submission not found.");
            } else {
                Student st = sub.getStudent();
                String studentName = (st != null && st.getName() != null) ? st.getName() : "Unknown";
                String studentId   = (st != null && st.getUserId() != null) ? st.getUserId() : "Unknown";
                String email       = (st != null && st.getEmail() != null) ? st.getEmail() : "N/A";
    
                String supervisor = (sub.getSupervisorName() != null && !sub.getSupervisorName().isBlank())
                        ? sub.getSupervisorName() : "N/A";
    
                String type = (sub.getPresentationType() != null) ? sub.getPresentationType() : "N/A";
                String board = "N/A";
                if ("Poster".equalsIgnoreCase(type)) {
                    board = (sub.getBoardId() != null && !sub.getBoardId().isBlank()) ? sub.getBoardId() : "(Not assigned)";
                }
    
                Session assigned = dataManager.findSessionBySubmissionId(sub.getSubmissionId());
                String sessionInfo = (assigned == null)
                        ? "Not assigned"
                        : assigned.getSessionId() + " | " + assigned.getDate() + " " + assigned.getTime() + " | " + assigned.getVenue();
    
                area.setText(
                        "=== PRESENTER ===\n" +
                        "Name: " + studentName + "\n" +
                        "Student ID: " + studentId + "\n" +
                        "Email: " + email + "\n" +
                        "Supervisor: " + supervisor + "\n\n" +
                        "=== SUBMISSION ===\n" +
                        "Submission ID: " + sub.getSubmissionId() + "\n" +
                        "Title: " + (sub.getTitle() == null ? "" : sub.getTitle()) + "\n" +
                        "Type: " + type + "\n" +
                        "Board ID: " + board + "\n" +
                        "Avg Score: " + String.format("%.2f", sub.getAverageScore()) + "\n\n" +
                        "=== SESSION ===\n" +
                        sessionInfo
                );
            }
        }
    
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }
    
    private JPanel buildEvaluatorDetailsPanel() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    
        int row = evaluatorTable.getSelectedRow();
        if (row == -1) {
            area.setText("Select an evaluator in the Evaluators table first.");
        } else {
            String evaluatorId = String.valueOf(evaluatorModel.getValueAt(row, 0));
            Evaluator eval = dataManager.findEvaluatorById(evaluatorId);
    
            if (eval == null) {
                area.setText("Evaluator not found.");
            } else {
                int evalCount = dataManager.getEvaluationsByEvaluator(evaluatorId).size();
                java.util.List<Session> sessions = dataManager.getSessionsByEvaluatorId(evaluatorId);
    
                StringBuilder sb = new StringBuilder();
                sb.append("=== EVALUATOR ===\n");
                sb.append("ID: ").append(eval.getUserId()).append("\n");
                sb.append("Name: ").append(eval.getName()).append("\n");
                sb.append("Evaluations submitted: ").append(evalCount).append("\n\n");
    
                sb.append("=== ASSIGNED SESSIONS ===\n");
                if (sessions.isEmpty()) {
                    sb.append("No sessions assigned.\n");
                } else {
                    for (Session s : sessions) {
                        sb.append("- ")
                          .append(s.getSessionId()).append(" | ")
                          .append(s.getDate()).append(" ")
                          .append(s.getTime()).append(" | ")
                          .append(s.getVenue()).append(" | ")
                          .append(s.getSessionType()).append("\n");
                    }
                }
    
                area.setText(sb.toString());
            }
        }
    
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }


    private void generateScheduleReport() {
        int selectedIndex = scheduleSessionCombo.getSelectedIndex();
        List<Session> sessions = dataManager.getSessions();

        if (sessions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No sessions available.",
                    "No Data", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Report r;
            if (selectedIndex == 0) {
                // All sessions
                r = coordinator.generateScheduleReport(sessions);
            } else {
                // Specific session
                Session selected = sessions.get(selectedIndex - 1);
                List<Session> singleSession = new ArrayList<>();
                singleSession.add(selected);
                r = coordinator.generateScheduleReport(singleSession);
            }
            outputArea.setText(r.getDetails());
        } catch (Exception ex) {
            outputArea.setText("Failed to generate schedule report: " + ex.getMessage());
        }
    }

    private void generateEvaluationReport() {
        int selectedIndex = evaluationSessionCombo.getSelectedIndex();
        List<Session> sessions = dataManager.getSessions();

        if (sessions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No sessions available.",
                    "No Data", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Report r;
            if (selectedIndex == 0) {
                // All sessions
                r = coordinator.generateEvaluationReport(sessions);
            } else {
                // Specific session
                Session selected = sessions.get(selectedIndex - 1);
                List<Session> singleSession = new ArrayList<>();
                singleSession.add(selected);
                r = coordinator.generateEvaluationReport(singleSession);
            }
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
        updateSessionComboboxes();
    }

    private void loadSubmissions() {
        submissionModel.setRowCount(0);
    
        for (Submission sub : dataManager.getSubmissions()) {
            if (sub == null) continue;
    
            // NEW: find which session this submission belongs to (if any)
            Session assigned = dataManager.findSessionBySubmissionId(sub.getSubmissionId());
            String assignedText = (assigned == null) ? "Not assigned" : assigned.getSessionId();
    
            String title = sub.getTitle();
            if (title != null && title.length() > 25) {
                title = title.substring(0, 25) + "...";
            }
 
            submissionModel.addRow(new Object[]{
                    sub.getSubmissionId(),
                    title,
                    sub.getPresentationType(),
                    assignedText, 
                    sub.getBoardId() == null ? "-" : sub.getBoardId(),
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

    private void updateSessionComboboxes() {
        List<Session> sessions = dataManager.getSessions();
        
        // Update schedule report combobox
        scheduleSessionCombo.removeAllItems();
        scheduleSessionCombo.addItem("All Sessions");
        for (Session s : sessions) {
            if (s != null) {
                scheduleSessionCombo.addItem(s.getSessionId() + " - " + s.getDate() + " " + s.getTime() + " (" + s.getVenue() + ")");
            }
        }
        
        // Update evaluation report combobox
        evaluationSessionCombo.removeAllItems();
        evaluationSessionCombo.addItem("All Sessions");
        for (Session s : sessions) {
            if (s != null) {
                evaluationSessionCombo.addItem(s.getSessionId() + " - " + s.getDate() + " " + s.getTime() + " (" + s.getVenue() + ")");
            }
        }

        // Update awards combobox
        awardsSessionCombo.removeAllItems();
        awardsSessionCombo.addItem("All Sessions");
        for (Session s : sessions) {
            if (s != null) {
                awardsSessionCombo.addItem(s.getSessionId() + " - " + s.getDate() + " " + s.getTime() + " (" + s.getVenue() + ")");
            }
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
