package views;

import data.DataManager;
import models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * EvaluatorDashboard (Wizard Flow)
 * Step 1: Select Session
 * Step 2: Select Presentation under session
 * Step 3: Evaluate selected presentation
 *
 * Rules:
 * - Evaluator sees only sessions they are assigned to
 * - Evaluator can evaluate only once per submission (must delete to re-evaluate)
 */
public class EvaluatorDashboard extends JFrame {
    private final Evaluator evaluator;
    private final DataManager dataManager;

    // Wizard (Sessions) cards
    private CardLayout wizardLayout;
    private JPanel wizardPanel;

    // Step 1
    private JTable sessionTable;
    private DefaultTableModel sessionModel;

    // Step 2
    private JTable presentationTable;
    private DefaultTableModel presentationModel;

    // Step 3
    private JLabel selectedSubmissionLabel;
    private JSlider claritySlider, methodologySlider, resultsSlider, presentationSlider;
    private JTextArea commentArea;

    // State
    private Session selectedSession;
    private Submission selectedSubmission;

    // Evaluations tab
    private JTable myEvalTable;
    private DefaultTableModel myEvalModel;

    public EvaluatorDashboard(Evaluator evaluator) {
        this.evaluator = evaluator;
        this.dataManager = DataManager.getInstance();

        initializeUI();

        loadMySessions();
        loadMyEvaluations();

        showStep("STEP1");
    }

    private void initializeUI() {
        setTitle("Evaluator Dashboard - " + evaluator.getName());
        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(155, 89, 182));
        headerPanel.setPreferredSize(new Dimension(1050, 60));
        JLabel headerLabel = new JLabel("Evaluator Dashboard - " + evaluator.getName());
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sessions", createWizardMySessionsPanel());
        tabs.addTab("Evaluations", createMyEvaluationsPanel());

        // Bottom
        JPanel bottomPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadMySessions();
            loadMyEvaluations();

            if (selectedSession != null) {
                loadPresentationsForSession(selectedSession);
            }
        });

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        bottomPanel.add(refreshButton);
        bottomPanel.add(logoutButton);

        add(headerPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // =========================================================
    // WIZARD PANEL (Sessions)
    // =========================================================

    private JPanel createWizardMySessionsPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        wizardLayout = new CardLayout();
        wizardPanel = new JPanel(wizardLayout);

        wizardPanel.add(createStep1Panel(), "STEP1");
        wizardPanel.add(createStep2Panel(), "STEP2");
        wizardPanel.add(createStep3Panel(), "STEP3");

        root.add(wizardPanel, BorderLayout.CENTER);
        return root;
    }

    private JPanel createStep1Panel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Select a Session"));

        String[] cols = {"Session ID", "Date", "Venue", "Type", "No. Presentations"};
        sessionModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        sessionTable = new JTable(sessionModel);
        panel.add(new JScrollPane(sessionTable), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton nextBtn = new JButton("Next ➜");
        nextBtn.setBackground(new Color(52, 152, 219));
        nextBtn.setForeground(Color.WHITE);

        nextBtn.addActionListener(e -> {
            int row = sessionTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this,
                        "Please select a session first.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sessionId = (String) sessionModel.getValueAt(row, 0);
            selectedSession = dataManager.findSessionById(sessionId);
            selectedSubmission = null;

            if (selectedSession == null) return;

            loadPresentationsForSession(selectedSession);
            showStep("STEP2");
        });

        btnPanel.add(nextBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStep2Panel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Choose a Presentation"));

        String[] cols = {"Submission ID", "Title", "Type", "Student", "File", "Avg Score", " No. Evaluations"};
        presentationModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        presentationTable = new JTable(presentationModel);
        panel.add(new JScrollPane(presentationTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton backBtn = new JButton("⬅ Back");
        backBtn.addActionListener(e -> showStep("STEP1"));

        JButton viewBtn = new JButton("View Details");
        viewBtn.addActionListener(e -> viewPresentationDetails());

        JButton openFileBtn = new JButton("Open File");
        openFileBtn.addActionListener(e -> openSelectedPresentationFile());

        JButton nextBtn = new JButton("Next ➜");
        nextBtn.setBackground(new Color(52, 152, 219));
        nextBtn.setForeground(Color.WHITE);
        nextBtn.addActionListener(e -> {
            int row = presentationTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this,
                        "Please select a presentation first.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String subId = (String) presentationModel.getValueAt(row, 0);
            selectedSubmission = dataManager.findSubmissionById(subId);

            if (selectedSubmission == null) return;

            // block if already evaluated by this evaluator
            if (findMyEvaluationForSubmission(selectedSubmission) != null) {
                JOptionPane.showMessageDialog(this,
                        "You already evaluated this submission.\n" +
                                "Delete your evaluation first (Evaluations tab) to re-evaluate.",
                        "Already Evaluated", JOptionPane.WARNING_MESSAGE);
                return;
            }

            updateSelectedSubmissionLabel();
            clearEvaluateForm();
            showStep("STEP3");
        });

        btnPanel.add(backBtn);
        btnPanel.add(viewBtn);
        btnPanel.add(openFileBtn);
        btnPanel.add(nextBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStep3Panel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Evaluate"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Selected Submission:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        selectedSubmissionLabel = new JLabel("(none)");
        selectedSubmissionLabel.setFont(new Font("Arial", Font.BOLD, 12));
        form.add(selectedSubmissionLabel, gbc);

        claritySlider = createRubricSlider();
        methodologySlider = createRubricSlider();
        resultsSlider = createRubricSlider();
        presentationSlider = createRubricSlider();

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Problem Clarity:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        form.add(claritySlider, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        form.add(new JLabel("Methodology:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        form.add(methodologySlider, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        form.add(new JLabel("Results:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        form.add(resultsSlider, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        form.add(new JLabel("Presentation:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        form.add(presentationSlider, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        form.add(new JLabel("Comments:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        commentArea = new JTextArea(5, 30);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        form.add(new JScrollPane(commentArea), gbc);

        panel.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton backBtn = new JButton("⬅ Back");
        backBtn.addActionListener(e -> showStep("STEP2"));

        JButton submitBtn = new JButton("Submit Evaluation");
        submitBtn.setBackground(new Color(46, 204, 113));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(e -> submitEvaluation());

        btnPanel.add(backBtn);
        btnPanel.add(submitBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showStep(String stepName) {
        wizardLayout.show(wizardPanel, stepName);
    }

    // =========================================================
    // DATA LOADING
    // =========================================================

    private void loadMySessions() {
        sessionModel.setRowCount(0);
        for (Session s : getMySessionsForEvaluator()) {
            int count = (s.getSubmissions() == null) ? 0 : s.getSubmissions().size();
            sessionModel.addRow(new Object[]{
                    s.getSessionId(), s.getDate(), s.getVenue(), s.getSessionType(), count
            });
        }
    }

    private List<Session> getMySessionsForEvaluator() {
        List<Session> result = new ArrayList<>();
        String myId = evaluator.getUserId();

        for (Session s : dataManager.getSessions()) {
            if (s == null) continue;
            if (isEvaluatorInSession(s, myId)) result.add(s);
        }
        return result;
    }

    private boolean isEvaluatorInSession(Session session, String evaluatorId) {
        if (session == null || evaluatorId == null) return false;
        List<Evaluator> evals = session.getEvaluators();
        if (evals == null) return false;

        for (Evaluator e : evals) {
            if (e != null && evaluatorId.equals(e.getUserId())) return true;
        }
        return false;
    }

    private void loadPresentationsForSession(Session session) {
        presentationModel.setRowCount(0);
        if (session == null || session.getSubmissions() == null) return;

        for (Submission sub : session.getSubmissions()) {
            if (sub == null) continue;

            String fileStatus = "Not uploaded";
            String path = sub.getFilePath();
            if (path != null && !path.isEmpty()) {
                fileStatus = new File(path).exists() ? "Uploaded" : "Missing file";
            }

            presentationModel.addRow(new Object[]{
                    sub.getSubmissionId(),
                    sub.getTitle(),
                    sub.getPresentationType(),
                    safeStudentName(sub),
                    fileStatus,
                    String.format("%.2f", sub.getAverageScore()),
                    (sub.getEvaluations() == null ? 0 : sub.getEvaluations().size())
            });
        }
    }

    private void viewPresentationDetails() {
        int row = presentationTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Select a presentation first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String subId = (String) presentationModel.getValueAt(row, 0);
        Submission sub = dataManager.findSubmissionById(subId);
        if (sub == null) return;

        JTextArea area = new JTextArea(sub.getDetails());
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(520, 420));

        JOptionPane.showMessageDialog(this, scroll,
                "Presentation Details", JOptionPane.INFORMATION_MESSAGE);
    }


    private void openSelectedPresentationFile() {
        int row = presentationTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Select a presentation first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String subId = (String) presentationModel.getValueAt(row, 0);
        Submission sub = dataManager.findSubmissionById(subId);
        if (sub == null) return;

        String filePath = sub.getFilePath();
        if (filePath == null || filePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No file uploaded for this submission.",
                    "No File", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this,
                    "File not found:\n" + file.getAbsolutePath() + "\n\n" +
                            "This usually happens if the file was moved/deleted, or the path is from another computer.\n" +
                            "Fix: copy uploads into a shared project folder (e.g., uploads/) when students upload.",
                    "Missing File", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(this,
                    "Desktop open is not supported on this system.",
                    "Not Supported", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Desktop.getDesktop().open(file);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open file:\n" + ex.getMessage(),
                    "Open Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================
    // EVALUATION RULE: ONE EVALUATION PER EVALUATOR PER SUBMISSION
    // =========================================================

    private Evaluation findMyEvaluationForSubmission(Submission sub) {
        if (sub == null || sub.getEvaluations() == null) return null;
        String myId = evaluator.getUserId();

        for (Evaluation ev : sub.getEvaluations()) {
            if (ev != null && myId.equals(ev.getEvaluatorId())) return ev;
        }
        return null;
    }

    private void submitEvaluation() {
        if (selectedSession == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a session first.",
                    "No Session", JOptionPane.WARNING_MESSAGE);
            showStep("STEP1");
            return;
        }

        if (selectedSubmission == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a presentation first.",
                    "No Presentation", JOptionPane.WARNING_MESSAGE);
            showStep("STEP2");
            return;
        }

        if (findMyEvaluationForSubmission(selectedSubmission) != null) {
            JOptionPane.showMessageDialog(this,
                    "You already evaluated this submission.\n" +
                            "Delete your evaluation first (Evaluations tab) to re-evaluate.",
                    "Already Evaluated", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int clarity = claritySlider.getValue();
        int methodology = methodologySlider.getValue();
        int results = resultsSlider.getValue();
        int presentation = presentationSlider.getValue();
        String comments = commentArea.getText().trim();

        try {
            Evaluation evaluation = evaluator.evaluateSubmission(
                    selectedSubmission, clarity, methodology, results, presentation, comments
            );

            dataManager.addEvaluation(evaluation);

            JOptionPane.showMessageDialog(this,
                    "Evaluation submitted successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            clearEvaluateForm();
            loadMyEvaluations();

            // After submit, go back to step 2
            loadPresentationsForSession(selectedSession);
            selectedSubmission = null;
            updateSelectedSubmissionLabel();
            showStep("STEP2");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to submit evaluation: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedSubmissionLabel() {
        if (selectedSubmission == null) {
            selectedSubmissionLabel.setText("(none)");
        } else {
            selectedSubmissionLabel.setText(
                    selectedSubmission.getSubmissionId() + " - " + selectedSubmission.getTitle()
                            + " (" + selectedSubmission.getPresentationType() + ")"
            );
        }
    }

    private void clearEvaluateForm() {
        if (claritySlider != null) claritySlider.setValue(5);
        if (methodologySlider != null) methodologySlider.setValue(5);
        if (resultsSlider != null) resultsSlider.setValue(5);
        if (presentationSlider != null) presentationSlider.setValue(5);
        if (commentArea != null) commentArea.setText("");
    }

    private JSlider createRubricSlider() {
        JSlider s = new JSlider(0, 10, 5);
        s.setMajorTickSpacing(2);
        s.setMinorTickSpacing(1);
        s.setPaintTicks(true);
        s.setPaintLabels(true);
        return s;
    }

    private String safeStudentName(Submission sub) {
        try { return sub.getStudent().getName(); }
        catch (Exception e) { return "Unknown"; }
    }

    // =========================================================
    // EVALUATIONS TAB
    // =========================================================

    private JPanel createMyEvaluationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        String[] columns = {"Submission ID", "Title", "Total Score", "Comment"};
        myEvalModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        myEvalTable = new JTable(myEvalModel);
        panel.add(new JScrollPane(myEvalTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();

        JButton viewBtn = new JButton("View Evaluation Details");
        viewBtn.addActionListener(e -> viewMyEvaluationDetails());

        JButton deleteBtn = new JButton("Delete Evaluation");
        deleteBtn.setBackground(new Color(231, 76, 60));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> deleteMyEvaluation());

        btnPanel.add(viewBtn);
        btnPanel.add(deleteBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadMyEvaluations() {
        myEvalModel.setRowCount(0);

        String myId = evaluator.getUserId();
        for (Submission sub : dataManager.getSubmissions()) {
            if (sub == null || sub.getEvaluations() == null) continue;

            for (Evaluation ev : sub.getEvaluations()) {
                if (ev != null && myId.equals(ev.getEvaluatorId())) {
                    myEvalModel.addRow(new Object[]{
                            sub.getSubmissionId(),
                            sub.getTitle(),
                            ev.getTotalScore(),
                            shorten(ev.getComments(), 35)
                    });
                }
            }
        }
    }

    private void viewMyEvaluationDetails() {
        int row = myEvalTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an evaluation row first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String subId = (String) myEvalModel.getValueAt(row, 0);
        Submission sub = dataManager.findSubmissionById(subId);
        if (sub == null) return;

        Evaluation match = findMyEvaluationForSubmission(sub);
        if (match != null) {
            JOptionPane.showMessageDialog(this, match.getDetails(),
                    "Evaluation Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteMyEvaluation() {
        int row = myEvalTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an evaluation row first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String subId = (String) myEvalModel.getValueAt(row, 0);
        Submission sub = dataManager.findSubmissionById(subId);
        if (sub == null) return;

        Evaluation myEval = findMyEvaluationForSubmission(sub);
        if (myEval == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete your evaluation for " + subId + "?\nYou can evaluate again after deletion.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        sub.getEvaluations().remove(myEval);
        dataManager.removeEvaluation(myEval);
        dataManager.saveToDisk();

        loadMyEvaluations();

        if (selectedSession != null) loadPresentationsForSession(selectedSession);
    }

    private String shorten(String text, int max) {
        if (text == null) return "";
        String t = text.trim();
        if (t.length() <= max) return t;
        return t.substring(0, max) + "...";
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
