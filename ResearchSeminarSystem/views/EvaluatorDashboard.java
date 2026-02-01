package views;

import data.DataManager;
import models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * EvaluatorDashboard - Interface for evaluators to view assigned submissions and submit evaluations
 */
public class EvaluatorDashboard extends JFrame {
    private Evaluator evaluator;
    private DataManager dataManager;

    private JTable assignedTable;
    private DefaultTableModel assignedModel;

    private JTable myEvalTable;
    private DefaultTableModel myEvalModel;

    // Evaluate tab fields
    private JComboBox<Submission> submissionCombo;
    private JSlider claritySlider, methodologySlider, resultsSlider, presentationSlider;
    private JTextArea commentArea;

    public EvaluatorDashboard(Evaluator evaluator) {
        this.evaluator = evaluator;
        this.dataManager = DataManager.getInstance();
        initializeUI();
        loadAssignedSubmissions();
        loadMyEvaluations();
    }

    private void initializeUI() {
        setTitle("Evaluator Dashboard - " + evaluator.getName());
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(155, 89, 182)); // purple-ish
        headerPanel.setPreferredSize(new Dimension(950, 60));
        JLabel headerLabel = new JLabel("Evaluator Dashboard - " + evaluator.getName());
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Assigned Presentations", createAssignedPanel());
        tabbedPane.addTab("Evaluate", createEvaluatePanel());
        tabbedPane.addTab("My Evaluations", createMyEvaluationsPanel());

        // Bottom
        JPanel bottomPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadAssignedSubmissions();
            loadMyEvaluations();
            reloadSubmissionCombo();
        });

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        bottomPanel.add(refreshButton);
        bottomPanel.add(logoutButton);

        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createAssignedPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        String[] columns = {"ID", "Title", "Type", "Student", "File", "Avg Score", "Evaluations"};
        assignedModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        assignedTable = new JTable(assignedModel);
        panel.add(new JScrollPane(assignedTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton viewBtn = new JButton("View Details");
        viewBtn.addActionListener(e -> viewAssignedDetails());

        JButton evaluateBtn = new JButton("Evaluate Selected");
        evaluateBtn.addActionListener(e -> moveSelectedToEvaluateTab());

        btnPanel.add(viewBtn);
        btnPanel.add(evaluateBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createEvaluatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10,10,10,10);

        // Select submission
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Select Submission:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        submissionCombo = new JComboBox<>();
        reloadSubmissionCombo();
        panel.add(submissionCombo, gbc);

        // Sliders
        claritySlider = createRubricSlider();
        methodologySlider = createRubricSlider();
        resultsSlider = createRubricSlider();
        presentationSlider = createRubricSlider();

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Problem Clarity:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(claritySlider, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("Methodology:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(methodologySlider, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("Results:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(resultsSlider, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("Presentation:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(presentationSlider, gbc);

        // Comment
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        panel.add(new JLabel("Comments:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        commentArea = new JTextArea(5, 30);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(commentArea), gbc);

        // Submit
        gbc.gridx = 1; gbc.gridy = 6; gbc.gridwidth = 2;
        JButton submitBtn = new JButton("Submit Evaluation");
        submitBtn.setBackground(new Color(46, 204, 113));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(e -> submitEvaluation());
        panel.add(submitBtn, gbc);

        return panel;
    }

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
        btnPanel.add(viewBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadAssignedSubmissions() {
        assignedModel.setRowCount(0);

        for (Submission sub : dataManager.getSubmissions()) {
            assignedModel.addRow(new Object[]{
                    sub.getSubmissionId(),
                    sub.getTitle(),
                    sub.getPresentationType(),
                    safeStudentName(sub),
                    sub.getFilePath().isEmpty() ? "Not uploaded" : "Uploaded",
                    String.format("%.2f", sub.getAverageScore()),
                    sub.getEvaluations().size()
            });
        }
    }

    private void reloadSubmissionCombo() {
        if (submissionCombo == null) return;
        submissionCombo.removeAllItems();

        for (Submission sub : dataManager.getSubmissions()) {
            submissionCombo.addItem(sub);
        }

        submissionCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Submission) {
                    Submission s = (Submission) value;
                    setText(s.getSubmissionId() + " - " + s.getTitle() + " (" + s.getPresentationType() + ")");
                }
                return this;
            }
        });
    }

    private void loadMyEvaluations() {
        myEvalModel.setRowCount(0);

        String evalId = evaluator.getUserId();
        for (Submission sub : dataManager.getSubmissions()) {
            for (Evaluation ev : sub.getEvaluations()) {
                if (safeEvaluatorId(ev).equals(evalId)) {
                    myEvalModel.addRow(new Object[]{
                            sub.getSubmissionId(),
                            sub.getTitle(),
                            safeTotal(ev),
                            safeComment(ev)
                    });
                }
            }
        }
    }

    private void submitEvaluation() {
        Submission selected = (Submission) submissionCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a submission first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int clarity = claritySlider.getValue();
        int methodology = methodologySlider.getValue();
        int results = resultsSlider.getValue();
        int presentation = presentationSlider.getValue();
        String comments = commentArea.getText().trim();

        try {
            Evaluation evaluation = evaluator.evaluateSubmission(
                    selected, clarity, methodology, results, presentation, comments
            );

            dataManager.addEvaluation(evaluation);

            JOptionPane.showMessageDialog(this, "Evaluation submitted successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            // reset
            claritySlider.setValue(5);
            methodologySlider.setValue(5);
            resultsSlider.setValue(5);
            presentationSlider.setValue(5);
            commentArea.setText("");

            // refresh
            loadAssignedSubmissions();
            loadMyEvaluations();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to submit evaluation: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewAssignedDetails() {
        int row = assignedTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a submission.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String subId = (String) assignedModel.getValueAt(row, 0);
        Submission target = findSubmissionById(subId);

        if (target != null) {
            JOptionPane.showMessageDialog(this, target.getDetails(),
                    "Submission Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void moveSelectedToEvaluateTab() {
        int row = assignedTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a submission.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String subId = (String) assignedModel.getValueAt(row, 0);
        Submission target = findSubmissionById(subId);

        if (target != null) {
            submissionCombo.setSelectedItem(target);

            // jump to Evaluate tab
            Container c = getContentPane();
            for (Component comp : c.getComponents()) {
                if (comp instanceof JTabbedPane) {
                    ((JTabbedPane) comp).setSelectedIndex(1);
                    break;
                }
            }
        }
    }

    private void viewMyEvaluationDetails() {
        int row = myEvalTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an evaluation row.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String subId = (String) myEvalModel.getValueAt(row, 0);
        Submission sub = findSubmissionById(subId);
        if (sub == null) return;

        String evalId = evaluator.getUserId();
        Evaluation match = null;

        for (Evaluation ev : sub.getEvaluations()) {
            if (safeEvaluatorId(ev).equals(evalId)) {
                match = ev;
                break;
            }
        }

        if (match != null) {
            JOptionPane.showMessageDialog(this, match.getDetails(),
                    "Evaluation Details", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Could not find the evaluation object.",
                    "Not Found", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Submission findSubmissionById(String id) {
        for (Submission s : dataManager.getSubmissions()) {
            if (s.getSubmissionId().equals(id)) return s;
        }
        return null;
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
        try {
            return sub.getStudent().getName();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String safeEvaluatorId(Evaluation ev) {
        try { return ev.getEvaluatorId(); }
        catch (Exception e) { return ""; }
    }

    private int safeTotal(Evaluation ev) {
        try { return ev.getTotalScore(); }
        catch (Exception e) { return 0; }
    }

    private String safeComment(Evaluation ev) {
        try { return ev.getComments(); }
        catch (Exception e) { return ""; }
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
