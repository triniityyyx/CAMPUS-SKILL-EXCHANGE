package ui;

import model.LearningRequest;
import model.Student;
import service.RequestService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Screen to manage Sent and Received Learning Requests.
 */
public class RequestsFrame extends JFrame {

    private final Student currentStudent;
    private final RequestService requestService = new RequestService();
    private final Runnable onDataChanged;
    private final SessionsFrameCallback sessionsCallback;

    public interface SessionsFrameCallback {
        void openSessionScheduler(int requestId, String skillName, String partnerName);
    }

    private JPanel receivedListPanel;
    private JPanel sentListPanel;
    private JButton tabReceivedBtn;
    private JButton tabSentBtn;
    private CardLayout cardLayout;
    private JPanel cardsPanel;

    public RequestsFrame(Student student, Runnable onDataChanged, SessionsFrameCallback sessionsCallback) {
        this.currentStudent = student;
        this.onDataChanged = onDataChanged;
        this.sessionsCallback = sessionsCallback;

        setTitle("Campus Skill Exchange - Learning Requests");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(960, 680);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);

        initUI();
        loadRequests();
    }

    public RequestsFrame(Student student) {
        this(student, null, null);
    }

    public JPanel createPanel() {
        JPanel container = new JPanel(new BorderLayout(16, 16));
        container.setBackground(Theme.BG_DARK);
        container.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel titleWrap = new JPanel();
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.Y_AXIS));
        titleWrap.setOpaque(false);

        JLabel title = new JLabel("📩 Learning Requests");
        title.setFont(Theme.FONT_TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Manage incoming peer learning invitations and monitor requests you've sent.");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        titleWrap.add(title);
        titleWrap.add(Box.createVerticalStrut(2));
        titleWrap.add(subtitle);

        // Tab switcher
        JPanel tabButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tabButtons.setOpaque(false);

        tabReceivedBtn = Theme.createPrimaryButton("📥 Received Requests");
        tabReceivedBtn.setPreferredSize(new Dimension(190, 36));

        tabSentBtn = Theme.createSecondaryButton("📤 Sent Requests");
        tabSentBtn.setPreferredSize(new Dimension(170, 36));

        tabReceivedBtn.addActionListener(e -> {
            cardLayout.show(cardsPanel, "RECEIVED");
            tabReceivedBtn.setBackground(Theme.PRIMARY);
            loadRequests();
        });

        tabSentBtn.addActionListener(e -> {
            cardLayout.show(cardsPanel, "SENT");
            loadRequests();
        });

        tabButtons.add(tabReceivedBtn);
        tabButtons.add(tabSentBtn);

        topPanel.add(titleWrap, BorderLayout.WEST);
        topPanel.add(tabButtons, BorderLayout.EAST);
        container.add(topPanel, BorderLayout.NORTH);

        // Cards Panel
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setOpaque(false);

        // 1. Received Requests View
        receivedListPanel = new JPanel();
        receivedListPanel.setLayout(new BoxLayout(receivedListPanel, BoxLayout.Y_AXIS));
        receivedListPanel.setOpaque(false);
        JScrollPane rcvScroll = Theme.createScrollPane(receivedListPanel);

        // 2. Sent Requests View
        sentListPanel = new JPanel();
        sentListPanel.setLayout(new BoxLayout(sentListPanel, BoxLayout.Y_AXIS));
        sentListPanel.setOpaque(false);
        JScrollPane sentScroll = Theme.createScrollPane(sentListPanel);

        cardsPanel.add(rcvScroll, "RECEIVED");
        cardsPanel.add(sentScroll, "SENT");

        container.add(cardsPanel, BorderLayout.CENTER);
        return container;
    }

    private void initUI() {
        setContentPane(createPanel());
    }

    public void loadRequests() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<LearningRequest> receivedList;
            List<LearningRequest> sentList;

            @Override
            protected Void doInBackground() throws Exception {
                receivedList = requestService.getReceivedRequests(currentStudent.getId());
                sentList = requestService.getSentRequests(currentStudent.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    renderReceivedRequests(receivedList);
                    renderSentRequests(sentList);
                } catch (Exception e) {
                    Theme.showError(RequestsFrame.this, "Failed to load requests: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void renderReceivedRequests(List<LearningRequest> list) {
        receivedListPanel.removeAll();
        tabReceivedBtn.setText("📥 Received (" + list.size() + ")");

        if (list.isEmpty()) {
            JPanel empty = Theme.createCardPanel(12);
            empty.setBorder(new EmptyBorder(30, 20, 30, 20));
            empty.setMaximumSize(new Dimension(800, 100));
            JLabel lbl = new JLabel("No received requests yet. When peers request your skills, they will appear here!", SwingConstants.CENTER);
            lbl.setFont(Theme.FONT_BODY);
            lbl.setForeground(Theme.TEXT_MUTED);
            empty.add(lbl);
            receivedListPanel.add(empty);
        } else {
            for (LearningRequest req : list) {
                receivedListPanel.add(createReceivedCard(req));
                receivedListPanel.add(Box.createVerticalStrut(12));
            }
        }
        receivedListPanel.revalidate();
        receivedListPanel.repaint();
    }

    private void renderSentRequests(List<LearningRequest> list) {
        sentListPanel.removeAll();
        tabSentBtn.setText("📤 Sent (" + list.size() + ")");

        if (list.isEmpty()) {
            JPanel empty = Theme.createCardPanel(12);
            empty.setBorder(new EmptyBorder(30, 20, 30, 20));
            empty.setMaximumSize(new Dimension(800, 100));
            JLabel lbl = new JLabel("You have not sent any learning requests yet. Go to 'Find Skills' to connect!", SwingConstants.CENTER);
            lbl.setFont(Theme.FONT_BODY);
            lbl.setForeground(Theme.TEXT_MUTED);
            empty.add(lbl);
            sentListPanel.add(empty);
        } else {
            for (LearningRequest req : list) {
                sentListPanel.add(createSentCard(req));
                sentListPanel.add(Box.createVerticalStrut(12));
            }
        }
        sentListPanel.revalidate();
        sentListPanel.repaint();
    }

    private JPanel createReceivedCard(LearningRequest req) {
        JPanel card = Theme.createCardPanel(12);
        card.setLayout(new BorderLayout(16, 8));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 90));

        // Details
        JPanel details = new JPanel(new GridLayout(2, 1, 0, 4));
        details.setOpaque(false);

        JLabel title = new JLabel(req.getSenderName() + " wants to learn " + req.getSkillName() + " from you");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel(req.getSenderDepartment() + " (Year " + req.getSenderYear() + ") • Status: " + req.getStatus());
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);

        details.add(title);
        details.add(sub);

        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.setOpaque(false);

        JLabel statusBadge = Theme.createStatusBadge(req.getStatus());
        actions.add(statusBadge);

        if (LearningRequest.STATUS_PENDING.equalsIgnoreCase(req.getStatus())) {
            JButton acceptBtn = Theme.createSuccessButton("✓ Accept");
            acceptBtn.setPreferredSize(new Dimension(100, 32));
            acceptBtn.setFont(Theme.FONT_SMALL_BOLD);

            JButton rejectBtn = Theme.createDangerButton("✕ Reject");
            rejectBtn.setPreferredSize(new Dimension(100, 32));
            rejectBtn.setFont(Theme.FONT_SMALL_BOLD);

            acceptBtn.addActionListener(e -> {
                try {
                    requestService.acceptRequest(req.getRequestId(), currentStudent.getId());
                    Theme.showSuccess(this, "Request from " + req.getSenderName() + " accepted!");
                    loadRequests();
                    if (onDataChanged != null) onDataChanged.run();

                    // Prompt to schedule session immediately
                    boolean scheduleNow = Theme.showConfirm(this,
                            "Request accepted! Would you like to schedule the learning session now?",
                            "Schedule Session");
                    if (scheduleNow && sessionsCallback != null) {
                        sessionsCallback.openSessionScheduler(req.getRequestId(), req.getSkillName(), req.getSenderName());
                    }
                } catch (Exception ex) {
                    Theme.showError(this, ex.getMessage());
                }
            });

            rejectBtn.addActionListener(e -> {
                boolean confirm = Theme.showConfirm(this, "Are you sure you want to reject this request?", "Reject Request");
                if (confirm) {
                    try {
                        requestService.rejectRequest(req.getRequestId(), currentStudent.getId());
                        Theme.showSuccess(this, "Request rejected.");
                        loadRequests();
                        if (onDataChanged != null) onDataChanged.run();
                    } catch (Exception ex) {
                        Theme.showError(this, ex.getMessage());
                    }
                }
            });

            actions.add(acceptBtn);
            actions.add(rejectBtn);
        } else if (LearningRequest.STATUS_ACCEPTED.equalsIgnoreCase(req.getStatus())) {
            JButton scheduleBtn = Theme.createPrimaryButton("📅 Schedule Session");
            scheduleBtn.setPreferredSize(new Dimension(150, 32));
            scheduleBtn.setFont(Theme.FONT_SMALL_BOLD);
            scheduleBtn.addActionListener(e -> {
                if (sessionsCallback != null) {
                    sessionsCallback.openSessionScheduler(req.getRequestId(), req.getSkillName(), req.getSenderName());
                }
            });
            actions.add(scheduleBtn);
        }

        card.add(details, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    private JPanel createSentCard(LearningRequest req) {
        JPanel card = Theme.createCardPanel(12);
        card.setLayout(new BorderLayout(16, 8));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 85));

        JPanel details = new JPanel(new GridLayout(2, 1, 0, 4));
        details.setOpaque(false);

        JLabel title = new JLabel("Requested " + req.getSkillName() + " from " + req.getReceiverName());
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Mentor: " + req.getReceiverName() + " (" + req.getReceiverDepartment() + ")");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);

        details.add(title);
        details.add(sub);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        actions.setOpaque(false);

        JLabel statusBadge = Theme.createStatusBadge(req.getStatus());
        actions.add(statusBadge);

        if (LearningRequest.STATUS_ACCEPTED.equalsIgnoreCase(req.getStatus())) {
            JButton scheduleBtn = Theme.createPrimaryButton("📅 Schedule Session");
            scheduleBtn.setPreferredSize(new Dimension(150, 32));
            scheduleBtn.setFont(Theme.FONT_SMALL_BOLD);
            scheduleBtn.addActionListener(e -> {
                if (sessionsCallback != null) {
                    sessionsCallback.openSessionScheduler(req.getRequestId(), req.getSkillName(), req.getReceiverName());
                }
            });
            actions.add(scheduleBtn);
        }

        card.add(details, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        return card;
    }
}
