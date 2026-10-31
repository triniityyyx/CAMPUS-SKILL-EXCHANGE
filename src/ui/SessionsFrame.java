package ui;

import dao.RequestDAO;
import model.LearningRequest;
import model.Session;
import model.Student;
import service.SessionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Screen to manage Scheduled, Completed, and Cancelled Learning Sessions.
 */
public class SessionsFrame extends JFrame {

    private final Student currentStudent;
    private final SessionService sessionService = new SessionService();
    private final RequestDAO requestDAO = new RequestDAO();
    private final Runnable onDataChanged;
    private final ReviewsCallback reviewsCallback;

    public interface ReviewsCallback {
        void openReviewDialog(int sessionId, String skillName, String partnerName);
    }

    private JPanel sessionsListPanel;
    private JLabel statusSummaryLabel;

    public SessionsFrame(Student student, Runnable onDataChanged, ReviewsCallback reviewsCallback) {
        this.currentStudent = student;
        this.onDataChanged = onDataChanged;
        this.reviewsCallback = reviewsCallback;

        setTitle("Campus Skill Exchange - Learning Sessions");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(980, 680);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);

        initUI();
        loadSessions();
    }

    public SessionsFrame(Student student) {
        this(student, null, null);
    }

    public JPanel createPanel() {
        JPanel container = new JPanel(new BorderLayout(16, 16));
        container.setBackground(Theme.BG_DARK);
        container.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel titleWrap = new JPanel();
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.Y_AXIS));
        titleWrap.setOpaque(false);

        JLabel title = new JLabel("📅 Learning Sessions");
        title.setFont(Theme.FONT_TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Track your scheduled mentoring sessions and complete them to exchange feedback.");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        titleWrap.add(title);
        titleWrap.add(Box.createVerticalStrut(2));
        titleWrap.add(subtitle);

        // Buttons
        JPanel actionWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionWrap.setOpaque(false);

        JButton scheduleNewBtn = Theme.createPrimaryButton("+ Schedule a Session");
        scheduleNewBtn.setPreferredSize(new Dimension(180, 36));
        scheduleNewBtn.addActionListener(e -> showPickRequestAndScheduleDialog());

        JButton refreshBtn = Theme.createSecondaryButton("🔄 Refresh");
        refreshBtn.setPreferredSize(new Dimension(110, 36));
        refreshBtn.addActionListener(e -> loadSessions());

        actionWrap.add(scheduleNewBtn);
        actionWrap.add(refreshBtn);

        topPanel.add(titleWrap, BorderLayout.WEST);
        topPanel.add(actionWrap, BorderLayout.EAST);
        container.add(topPanel, BorderLayout.NORTH);

        // Sessions List Panel
        sessionsListPanel = new JPanel();
        sessionsListPanel.setLayout(new BoxLayout(sessionsListPanel, BoxLayout.Y_AXIS));
        sessionsListPanel.setOpaque(false);

        JScrollPane scrollPane = Theme.createScrollPane(sessionsListPanel);
        container.add(scrollPane, BorderLayout.CENTER);

        // Status Summary Footer
        statusSummaryLabel = new JLabel("Loading sessions...");
        statusSummaryLabel.setFont(Theme.FONT_SMALL);
        statusSummaryLabel.setForeground(Theme.TEXT_MUTED);
        container.add(statusSummaryLabel, BorderLayout.SOUTH);

        return container;
    }

    private void initUI() {
        setContentPane(createPanel());
    }

    public void loadSessions() {
        SwingWorker<List<Session>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Session> doInBackground() throws Exception {
                return sessionService.getSessionsForStudent(currentStudent.getId());
            }

            @Override
            protected void done() {
                try {
                    List<Session> sessions = get();
                    renderSessions(sessions);
                } catch (Exception e) {
                    statusSummaryLabel.setText("Failed to load sessions: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void renderSessions(List<Session> sessions) {
        sessionsListPanel.removeAll();
        statusSummaryLabel.setText("Total sessions: " + sessions.size());

        if (sessions.isEmpty()) {
            JPanel empty = Theme.createCardPanel(12);
            empty.setBorder(new EmptyBorder(35, 20, 35, 20));
            empty.setMaximumSize(new Dimension(800, 110));
            JLabel lbl = new JLabel("No sessions scheduled yet. Accept a request and schedule your first session!", SwingConstants.CENTER);
            lbl.setFont(Theme.FONT_BODY);
            lbl.setForeground(Theme.TEXT_MUTED);
            empty.add(lbl);
            sessionsListPanel.add(empty);
        } else {
            for (Session s : sessions) {
                sessionsListPanel.add(createSessionCard(s));
                sessionsListPanel.add(Box.createVerticalStrut(12));
            }
        }
        sessionsListPanel.revalidate();
        sessionsListPanel.repaint();
    }

    private JPanel createSessionCard(Session s) {
        JPanel card = Theme.createCardPanel(12);
        card.setLayout(new BorderLayout(16, 10));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 110));

        boolean isTeacher = s.getReceiverId() == currentStudent.getId();
        String roleText = isTeacher ? "Teaching: " : "Learning: ";
        String partnerName = isTeacher ? s.getSenderName() : s.getReceiverName();

        // Left details
        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setOpaque(false);

        JLabel title = new JLabel(roleText + s.getSkillName() + " with " + partnerName);
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
        String dateStr = s.getSessionDate() != null ? df.format(s.getSessionDate()) : "Date TBD";
        String timeStr = s.getSessionTime() != null ? s.getSessionTime().toString().substring(0, 5) : "";

        JLabel info = new JLabel("🗓 " + dateStr + " at " + timeStr + "  •  Mode: " + s.getMode() + "  •  Location / Link: " + s.getLocationOrLink());
        info.setFont(Theme.FONT_SMALL);
        info.setForeground(Theme.TEXT_SECONDARY);

        details.add(title);
        details.add(Box.createVerticalStrut(4));
        details.add(info);

        // Right actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.setOpaque(false);

        JLabel statusBadge = Theme.createStatusBadge(s.getStatus());
        actions.add(statusBadge);

        if (Session.STATUS_SCHEDULED.equalsIgnoreCase(s.getStatus())) {
            JButton completeBtn = Theme.createSuccessButton("✓ Complete");
            completeBtn.setPreferredSize(new Dimension(110, 32));
            completeBtn.setFont(Theme.FONT_SMALL_BOLD);
            completeBtn.addActionListener(e -> {
                boolean confirm = Theme.showConfirm(this,
                        "Mark this session for '" + s.getSkillName() + "' as Completed?",
                        "Complete Session");
                if (confirm) {
                    try {
                        sessionService.completeSession(s.getSessionId(), currentStudent.getId());
                        Theme.showSuccess(this, "Session marked as completed! You can now write a review.");
                        loadSessions();
                        if (onDataChanged != null) onDataChanged.run();

                        // Offer review immediately
                        boolean doReview = Theme.showConfirm(this,
                                "Would you like to write a review for " + partnerName + " now?",
                                "Leave Review");
                        if (doReview && reviewsCallback != null) {
                            reviewsCallback.openReviewDialog(s.getSessionId(), s.getSkillName(), partnerName);
                        }
                    } catch (Exception ex) {
                        Theme.showError(this, ex.getMessage());
                    }
                }
            });

            JButton cancelBtn = Theme.createDangerButton("✕ Cancel");
            cancelBtn.setPreferredSize(new Dimension(95, 32));
            cancelBtn.setFont(Theme.FONT_SMALL_BOLD);
            cancelBtn.addActionListener(e -> {
                boolean confirm = Theme.showConfirm(this, "Are you sure you want to cancel this session?", "Cancel Session");
                if (confirm) {
                    try {
                        sessionService.cancelSession(s.getSessionId(), currentStudent.getId());
                        Theme.showSuccess(this, "Session cancelled.");
                        loadSessions();
                        if (onDataChanged != null) onDataChanged.run();
                    } catch (Exception ex) {
                        Theme.showError(this, ex.getMessage());
                    }
                }
            });

            actions.add(completeBtn);
            actions.add(cancelBtn);

        } else if (Session.STATUS_COMPLETED.equalsIgnoreCase(s.getStatus())) {
            if (!s.isReviewed()) {
                JButton reviewBtn = Theme.createPrimaryButton("⭐ Write Review");
                reviewBtn.setPreferredSize(new Dimension(130, 32));
                reviewBtn.setFont(Theme.FONT_SMALL_BOLD);
                reviewBtn.addActionListener(e -> {
                    if (reviewsCallback != null) {
                        reviewsCallback.openReviewDialog(s.getSessionId(), s.getSkillName(), partnerName);
                    }
                });
                actions.add(reviewBtn);
            } else {
                JLabel reviewedBadge = Theme.createStatusBadge("REVIEWED");
                actions.add(reviewedBadge);
            }
        }

        card.add(details, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    /**
     * Show dialog to schedule session for a specific request.
     */
    public void showScheduleDialog(int requestId, String skillName, String partnerName) {
        JDialog dialog = new JDialog(this, "Schedule Learning Session", true);
        dialog.setSize(440, 420);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Theme.BG_DARK);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Schedule Session: " + skillName);
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("With peer: " + partnerName);
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);

        // Date input (YYYY-MM-DD)
        JLabel dateLbl = new JLabel("Date (YYYY-MM-DD, e.g. 2026-10-15)");
        dateLbl.setFont(Theme.FONT_SMALL_BOLD);
        dateLbl.setForeground(Theme.TEXT_SECONDARY);
        JTextField dateField = Theme.createTextField("2026-10-15");

        // Time input (HH:MM:SS or HH:MM)
        JLabel timeLbl = new JLabel("Time (HH:MM:SS, e.g. 15:30:00)");
        timeLbl.setFont(Theme.FONT_SMALL_BOLD);
        timeLbl.setForeground(Theme.TEXT_SECONDARY);
        JTextField timeField = Theme.createTextField("15:00:00");

        // Mode dropdown
        JLabel modeLbl = new JLabel("Session Mode");
        modeLbl.setFont(Theme.FONT_SMALL_BOLD);
        modeLbl.setForeground(Theme.TEXT_SECONDARY);
        JComboBox<String> modeCombo = Theme.createComboBox(new String[]{"Online", "Offline"});

        // Location / Link
        JLabel locLbl = new JLabel("Meeting Location or Online Link");
        locLbl.setFont(Theme.FONT_SMALL_BOLD);
        locLbl.setForeground(Theme.TEXT_SECONDARY);
        JTextField locField = Theme.createTextField("e.g. Google Meet link or Library Room 102");

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(100, 36));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton scheduleBtn = Theme.createPrimaryButton("Confirm & Schedule");
        scheduleBtn.setPreferredSize(new Dimension(160, 36));
        scheduleBtn.addActionListener(e -> {
            try {
                String dStr = dateField.getText().trim();
                String tStr = timeField.getText().trim();
                String mode = (String) modeCombo.getSelectedItem();
                String loc = locField.getText().trim();

                Date sqlDate = Date.valueOf(dStr);
                if (tStr.length() == 5) tStr += ":00";
                Time sqlTime = Time.valueOf(tStr);

                sessionService.scheduleSession(requestId, currentStudent.getId(), sqlDate, sqlTime, mode, loc);
                Theme.showSuccess(dialog, "Session successfully scheduled for " + dStr + "!");
                dialog.dispose();
                loadSessions();
                if (onDataChanged != null) onDataChanged.run();
            } catch (IllegalArgumentException ex) {
                Theme.showError(dialog, "Invalid date/time format. Use YYYY-MM-DD for date and HH:MM:SS for time.\nError: " + ex.getMessage());
            } catch (Exception ex) {
                Theme.showError(dialog, "Failed to schedule session: " + ex.getMessage());
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(scheduleBtn);

        panel.add(title);
        panel.add(sub);
        panel.add(Box.createVerticalStrut(14));
        panel.add(dateLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(dateField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(timeLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(timeField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(modeLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(modeCombo);
        panel.add(Box.createVerticalStrut(10));
        panel.add(locLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(locField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnPanel);

        dialog.setContentPane(panel);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                SwingUtilities.invokeLater(() -> dateField.requestFocusInWindow());
            }
        });
        dialog.setVisible(true);
    }

    private void showPickRequestAndScheduleDialog() {
        try {
            List<LearningRequest> received = requestDAO.getReceivedRequests(currentStudent.getId());
            List<LearningRequest> sent = requestDAO.getSentRequests(currentStudent.getId());

            // Filter requests that are in ACCEPTED status
            List<LearningRequest> accepted = new java.util.ArrayList<>();
            for (LearningRequest r : received) {
                if (LearningRequest.STATUS_ACCEPTED.equalsIgnoreCase(r.getStatus())) accepted.add(r);
            }
            for (LearningRequest r : sent) {
                if (LearningRequest.STATUS_ACCEPTED.equalsIgnoreCase(r.getStatus())) accepted.add(r);
            }

            if (accepted.isEmpty()) {
                Theme.showError(this, "No accepted requests ready to be scheduled.\nPlease accept an incoming request or wait for your peer to accept your request first.");
                return;
            }

            String[] options = new String[accepted.size()];
            for (int i = 0; i < accepted.size(); i++) {
                LearningRequest r = accepted.get(i);
                boolean isMeSender = r.getSenderId() == currentStudent.getId();
                String partner = isMeSender ? r.getReceiverName() : r.getSenderName();
                options[i] = String.format("#%d - %s with %s", r.getRequestId(), r.getSkillName(), partner);
            }

            String chosen = (String) JOptionPane.showInputDialog(
                    this,
                    "Select an accepted learning request to schedule:",
                    "Schedule Session",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (chosen != null) {
                int idx = java.util.Arrays.asList(options).indexOf(chosen);
                LearningRequest r = accepted.get(idx);
                boolean isMeSender = r.getSenderId() == currentStudent.getId();
                String partner = isMeSender ? r.getReceiverName() : r.getSenderName();
                showScheduleDialog(r.getRequestId(), r.getSkillName(), partner);
            }
        } catch (Exception ex) {
            Theme.showError(this, "Error fetching accepted requests: " + ex.getMessage());
        }
    }
}
