package ui;

import model.Review;
import model.Session;
import model.Student;
import service.SessionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Screen to view peer reviews and submit reviews for completed sessions.
 */
public class ReviewsFrame extends JFrame {

    private final Student currentStudent;
    private final SessionService sessionService = new SessionService();
    private final Runnable onDataChanged;

    private JPanel receivedListPanel;
    private JPanel givenListPanel;
    private JLabel avgRatingBadge;
    private JButton tabReceivedBtn;
    private JButton tabGivenBtn;
    private CardLayout cardLayout;
    private JPanel cardsPanel;

    public ReviewsFrame(Student student, Runnable onDataChanged) {
        this.currentStudent = student;
        this.onDataChanged = onDataChanged;

        setTitle("Campus Skill Exchange - Ratings & Reviews");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(960, 680);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);

        initUI();
        loadReviews();
    }

    public ReviewsFrame(Student student) {
        this(student, null);
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

        JLabel title = new JLabel("⭐ Ratings & Reviews");
        title.setFont(Theme.FONT_TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Honest feedback from peers maintains the trust and quality of our learning community.");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        titleWrap.add(title);
        titleWrap.add(Box.createVerticalStrut(2));
        titleWrap.add(subtitle);

        // Header Actions & Score
        JPanel actionWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionWrap.setOpaque(false);

        avgRatingBadge = Theme.createStatusBadge("★ " + currentStudent.getFormattedRating());
        avgRatingBadge.setFont(Theme.FONT_BODY_BOLD);
        avgRatingBadge.setForeground(Theme.STAR_GOLD);

        tabReceivedBtn = Theme.createPrimaryButton("🌟 Reviews Received");
        tabReceivedBtn.setPreferredSize(new Dimension(170, 36));

        tabGivenBtn = Theme.createSecondaryButton("✍ Reviews Given");
        tabGivenBtn.setPreferredSize(new Dimension(160, 36));

        JButton writeBtn = Theme.createSuccessButton("+ Review Session");
        writeBtn.setPreferredSize(new Dimension(150, 36));
        writeBtn.addActionListener(e -> showPickSessionAndReviewDialog());

        tabReceivedBtn.addActionListener(e -> {
            cardLayout.show(cardsPanel, "RECEIVED");
            loadReviews();
        });

        tabGivenBtn.addActionListener(e -> {
            cardLayout.show(cardsPanel, "GIVEN");
            loadReviews();
        });

        actionWrap.add(avgRatingBadge);
        actionWrap.add(tabReceivedBtn);
        actionWrap.add(tabGivenBtn);
        actionWrap.add(writeBtn);

        topPanel.add(titleWrap, BorderLayout.WEST);
        topPanel.add(actionWrap, BorderLayout.EAST);
        container.add(topPanel, BorderLayout.NORTH);

        // Center Cards
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setOpaque(false);

        receivedListPanel = new JPanel();
        receivedListPanel.setLayout(new BoxLayout(receivedListPanel, BoxLayout.Y_AXIS));
        receivedListPanel.setOpaque(false);
        JScrollPane rcvScroll = Theme.createScrollPane(receivedListPanel);

        givenListPanel = new JPanel();
        givenListPanel.setLayout(new BoxLayout(givenListPanel, BoxLayout.Y_AXIS));
        givenListPanel.setOpaque(false);
        JScrollPane givenScroll = Theme.createScrollPane(givenListPanel);

        cardsPanel.add(rcvScroll, "RECEIVED");
        cardsPanel.add(givenScroll, "GIVEN");

        container.add(cardsPanel, BorderLayout.CENTER);
        return container;
    }

    private void initUI() {
        setContentPane(createPanel());
    }

    public void loadReviews() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<Review> receivedList;
            List<Review> givenList;

            @Override
            protected Void doInBackground() throws Exception {
                receivedList = sessionService.getReviewsReceived(currentStudent.getId());
                givenList = sessionService.getReviewsGiven(currentStudent.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    renderReceivedReviews(receivedList);
                    renderGivenReviews(givenList);
                } catch (Exception e) {
                    Theme.showError(ReviewsFrame.this, "Failed to load reviews: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void renderReceivedReviews(List<Review> list) {
        receivedListPanel.removeAll();
        tabReceivedBtn.setText("🌟 Reviews Received (" + list.size() + ")");

        if (list.isEmpty()) {
            JPanel empty = Theme.createCardPanel(12);
            empty.setBorder(new EmptyBorder(35, 20, 35, 20));
            empty.setMaximumSize(new Dimension(800, 110));
            JLabel lbl = new JLabel("No reviews received yet. Teach skills and complete sessions to earn peer reviews!", SwingConstants.CENTER);
            lbl.setFont(Theme.FONT_BODY);
            lbl.setForeground(Theme.TEXT_MUTED);
            empty.add(lbl);
            receivedListPanel.add(empty);
        } else {
            for (Review r : list) {
                receivedListPanel.add(createReviewCard(r, false));
                receivedListPanel.add(Box.createVerticalStrut(12));
            }
        }
        receivedListPanel.revalidate();
        receivedListPanel.repaint();
    }

    private void renderGivenReviews(List<Review> list) {
        givenListPanel.removeAll();
        tabGivenBtn.setText("✍ Reviews Given (" + list.size() + ")");

        if (list.isEmpty()) {
            JPanel empty = Theme.createCardPanel(12);
            empty.setBorder(new EmptyBorder(35, 20, 35, 20));
            empty.setMaximumSize(new Dimension(800, 110));
            JLabel lbl = new JLabel("You have not reviewed any sessions yet.", SwingConstants.CENTER);
            lbl.setFont(Theme.FONT_BODY);
            lbl.setForeground(Theme.TEXT_MUTED);
            empty.add(lbl);
            givenListPanel.add(empty);
        } else {
            for (Review r : list) {
                givenListPanel.add(createReviewCard(r, true));
                givenListPanel.add(Box.createVerticalStrut(12));
            }
        }
        givenListPanel.revalidate();
        givenListPanel.repaint();
    }

    private JPanel createReviewCard(Review r, boolean isGivenByMe) {
        JPanel card = Theme.createCardPanel(12);
        card.setLayout(new BorderLayout(14, 8));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 105));

        // Top line: Reviewer/Target + Rating stars
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        String heading = isGivenByMe ?
                "Review for session on " + r.getSkillName() :
                "Reviewed by " + r.getReviewerName() + " (" + r.getSkillName() + ")";

        JLabel title = new JLabel(heading);
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel stars = new JLabel(r.getStarsDisplay() + " (" + r.getRating() + "/5)");
        stars.setFont(Theme.FONT_BODY_BOLD);
        stars.setForeground(Theme.STAR_GOLD);

        top.add(title, BorderLayout.WEST);
        top.add(stars, BorderLayout.EAST);

        // Comment text
        JLabel comment = new JLabel("“ " + r.getComment() + " ”");
        comment.setFont(Theme.FONT_BODY);
        comment.setForeground(Theme.TEXT_SECONDARY);

        // Date footer
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        String dateStr = r.getCreatedAt() != null ? sdf.format(r.getCreatedAt()) : "";
        JLabel dateLbl = new JLabel("Submitted on " + dateStr);
        dateLbl.setFont(Theme.FONT_SMALL);
        dateLbl.setForeground(Theme.TEXT_MUTED);

        JPanel content = new JPanel(new GridLayout(2, 1, 0, 4));
        content.setOpaque(false);
        content.add(comment);
        content.add(dateLbl);

        card.add(top, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    /**
     * Show modal dialog to submit review for a specific completed session.
     */
    public void showReviewDialog(int sessionId, String skillName, String partnerName) {
        JDialog dialog = new JDialog(this, "Submit Session Review", true);
        dialog.setSize(460, 400);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Theme.BG_DARK);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(22, 26, 22, 26));

        JLabel title = new JLabel("Review Session: " + skillName);
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Sharing feedback for your peer: " + partnerName);
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);

        JLabel ratingLbl = new JLabel("Rating (1 to 5 Stars)");
        ratingLbl.setFont(Theme.FONT_SMALL_BOLD);
        ratingLbl.setForeground(Theme.TEXT_SECONDARY);

        Integer[] ratings = {5, 4, 3, 2, 1};
        JComboBox<Integer> ratingCombo = Theme.createComboBox(ratings);

        JLabel commentLbl = new JLabel("Feedback Comment");
        commentLbl.setFont(Theme.FONT_SMALL_BOLD);
        commentLbl.setForeground(Theme.TEXT_SECONDARY);

        JTextArea commentArea = new JTextArea(4, 20);
        commentArea.setBackground(Theme.BG_INPUT);
        commentArea.setForeground(Theme.TEXT_PRIMARY);
        commentArea.setFont(Theme.FONT_BODY);
        commentArea.setCaretColor(Theme.TEXT_PRIMARY);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(100, 36));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton submitBtn = Theme.createSuccessButton("Submit Review");
        submitBtn.setPreferredSize(new Dimension(140, 36));
        submitBtn.addActionListener(e -> {
            Integer rating = (Integer) ratingCombo.getSelectedItem();
            String comment = commentArea.getText().trim();

            if (comment.isEmpty()) {
                Theme.showError(dialog, "Please enter a comment describing your learning experience.");
                return;
            }

            try {
                sessionService.submitReview(sessionId, currentStudent.getId(), rating != null ? rating : 5, comment);
                Theme.showSuccess(dialog, "Thank you! Your review has been recorded.");
                dialog.dispose();
                loadReviews();
                if (onDataChanged != null) onDataChanged.run();
            } catch (Exception ex) {
                Theme.showError(dialog, ex.getMessage());
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(submitBtn);

        panel.add(title);
        panel.add(sub);
        panel.add(Box.createVerticalStrut(16));
        panel.add(ratingLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(ratingCombo);
        panel.add(Box.createVerticalStrut(12));
        panel.add(commentLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(commentScroll);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnPanel);

        dialog.setContentPane(panel);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                SwingUtilities.invokeLater(() -> commentArea.requestFocusInWindow());
            }
        });
        dialog.setVisible(true);
    }

    private void showPickSessionAndReviewDialog() {
        try {
            List<Session> sessions = sessionService.getSessionsForStudent(currentStudent.getId());
            List<Session> reviewable = new java.util.ArrayList<>();

            for (Session s : sessions) {
                // Must be completed AND not already reviewed
                if (Session.STATUS_COMPLETED.equalsIgnoreCase(s.getStatus()) && !sessionService.hasReviewed(s.getSessionId(), currentStudent.getId())) {
                    reviewable.add(s);
                }
            }

            if (reviewable.isEmpty()) {
                Theme.showError(this, "No unreviewed completed sessions found.\nSessions must be marked as 'Completed' before a review can be submitted.");
                return;
            }

            String[] options = new String[reviewable.size()];
            for (int i = 0; i < reviewable.size(); i++) {
                Session s = reviewable.get(i);
                boolean isTeacher = s.getReceiverId() == currentStudent.getId();
                String partner = isTeacher ? s.getSenderName() : s.getReceiverName();
                options[i] = String.format("Session #%d - %s with %s (%s)", s.getSessionId(), s.getSkillName(), partner, s.getSessionDate());
            }

            String chosen = (String) JOptionPane.showInputDialog(
                    this,
                    "Select a completed session to review:",
                    "Submit Review",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (chosen != null) {
                int idx = java.util.Arrays.asList(options).indexOf(chosen);
                Session s = reviewable.get(idx);
                boolean isTeacher = s.getReceiverId() == currentStudent.getId();
                String partner = isTeacher ? s.getSenderName() : s.getReceiverName();
                showReviewDialog(s.getSessionId(), s.getSkillName(), partner);
            }
        } catch (Exception ex) {
            Theme.showError(this, "Error fetching sessions: " + ex.getMessage());
        }
    }
}
