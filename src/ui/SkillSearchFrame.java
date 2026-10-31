package ui;

import model.SkillMatchResult;
import model.Student;
import service.MatchingService;
import service.RequestService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Screen to search for students offering skills.
 * Features: Rule-Based Skill Matching (Skill 70%, Category 15%, Rating 15%).
 */
public class SkillSearchFrame extends JFrame {

    private final Student currentStudent;
    private final MatchingService matchingService = new MatchingService();
    private final RequestService requestService = new RequestService();
    private final Runnable onDataChanged;

    private JTextField searchField;
    private JPanel resultsPanel;
    private JLabel resultsCountLabel;

    public SkillSearchFrame(Student student, Runnable onDataChanged) {
        this.currentStudent = student;
        this.onDataChanged = onDataChanged;

        setTitle("Campus Skill Exchange - Find Skills & Mentors");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(960, 680);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);

        initUI();
        performSearch("");
    }

    public SkillSearchFrame(Student student) {
        this(student, null);
    }

    public JPanel createPanel() {
        JPanel container = new JPanel(new BorderLayout(16, 16));
        container.setBackground(Theme.BG_DARK);
        container.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top Search Bar Section
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        JLabel title = new JLabel("🔍 Find Students Offering Skills");
        title.setFont(Theme.FONT_TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Powered by Rule-Based Skill Matching (Skill Match: 70% | Category/Barter: 15% | Rating: 15%)");
        subtitle.setFont(Theme.FONT_SMALL_BOLD);
        subtitle.setForeground(Theme.PRIMARY_LIGHT);

        // Search Input Bar
        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(new EmptyBorder(12, 0, 8, 0));

        searchField = Theme.createTextField("Search skill name (e.g. Java, Python, Web Dev, DSA, UI/UX)...");
        searchField.setPreferredSize(new Dimension(500, 40));

        JButton searchBtn = Theme.createPrimaryButton("Search Mentors");
        searchBtn.setPreferredSize(new Dimension(150, 40));
        searchBtn.addActionListener(e -> performSearch(searchField.getText().trim()));

        JButton showAllBtn = Theme.createSecondaryButton("View All");
        showAllBtn.setPreferredSize(new Dimension(100, 40));
        showAllBtn.addActionListener(e -> {
            searchField.setText("");
            performSearch("");
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch(searchField.getText().trim());
                }
            }
        });

        searchBar.add(searchField, BorderLayout.CENTER);

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(searchBtn);
        btnWrap.add(showAllBtn);
        searchBar.add(btnWrap, BorderLayout.EAST);

        // Status / Count Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);
        resultsCountLabel = new JLabel("Finding mentors...");
        resultsCountLabel.setFont(Theme.FONT_SMALL_BOLD);
        resultsCountLabel.setForeground(Theme.TEXT_SECONDARY);
        statusBar.add(resultsCountLabel, BorderLayout.WEST);

        topSection.add(title);
        topSection.add(Box.createVerticalStrut(4));
        topSection.add(subtitle);
        topSection.add(searchBar);
        topSection.add(statusBar);

        container.add(topSection, BorderLayout.NORTH);

        // Results Grid / List
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setOpaque(false);

        JScrollPane scrollPane = Theme.createScrollPane(resultsPanel);
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private void initUI() {
        setContentPane(createPanel());
    }

    public void performSearch(String query) {
        resultsCountLabel.setText("Searching matching peers...");
        resultsPanel.removeAll();
        resultsPanel.revalidate();
        resultsPanel.repaint();

        SwingWorker<List<SkillMatchResult>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SkillMatchResult> doInBackground() throws Exception {
                return matchingService.findMatches(currentStudent.getId(), query);
            }

            @Override
            protected void done() {
                try {
                    List<SkillMatchResult> matches = get();
                    renderResults(matches, query);
                } catch (Exception e) {
                    resultsCountLabel.setText("Error searching: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void renderResults(List<SkillMatchResult> matches, String query) {
        resultsPanel.removeAll();
        resultsCountLabel.setText(String.format("Found %d matching student%s for \"%s\" (Sorted by Rule-Based Match Score)",
                matches.size(), matches.size() == 1 ? "" : "s", query.isEmpty() ? "All Skills" : query));

        if (matches.isEmpty()) {
            JPanel emptyPanel = Theme.createCardPanel(12);
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setBorder(new EmptyBorder(40, 20, 40, 20));
            emptyPanel.setMaximumSize(new Dimension(800, 140));

            JLabel emptyTitle = new JLabel("No students found offering this skill yet.", SwingConstants.CENTER);
            emptyTitle.setFont(Theme.FONT_SUBTITLE);
            emptyTitle.setForeground(Theme.TEXT_SECONDARY);
            emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptySub = new JLabel("Try searching for another skill like 'Java', 'Python', 'Web Development', or click 'View All'.", SwingConstants.CENTER);
            emptySub.setFont(Theme.FONT_SMALL);
            emptySub.setForeground(Theme.TEXT_MUTED);
            emptySub.setAlignmentX(Component.CENTER_ALIGNMENT);

            emptyPanel.add(emptyTitle);
            emptyPanel.add(Box.createVerticalStrut(8));
            emptyPanel.add(emptySub);
            resultsPanel.add(emptyPanel);
        } else {
            for (SkillMatchResult match : matches) {
                resultsPanel.add(createStudentCard(match));
                resultsPanel.add(Box.createVerticalStrut(12));
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private JPanel createStudentCard(SkillMatchResult match) {
        Student candidate = match.getStudent();

        JPanel card = Theme.createCardPanel(12);
        card.setLayout(new BorderLayout(16, 10));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 115));

        // Left: Avatar + Details
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        leftPanel.setOpaque(false);

        // Avatar icon
        JLabel avatar = new JLabel(candidate.getName().substring(0, 1).toUpperCase(), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(Theme.FONT_TITLE);
        avatar.setForeground(Color.WHITE);
        avatar.setPreferredSize(new Dimension(46, 46));

        // Info stack
        JPanel infoStack = new JPanel();
        infoStack.setLayout(new BoxLayout(infoStack, BoxLayout.Y_AXIS));
        infoStack.setOpaque(false);

        JLabel nameLabel = new JLabel(candidate.getName());
        nameLabel.setFont(Theme.FONT_TITLE);
        nameLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel deptYear = new JLabel(candidate.getDepartment() + " | Year " + candidate.getYear());
        deptYear.setFont(Theme.FONT_SMALL);
        deptYear.setForeground(Theme.TEXT_SECONDARY);

        FlowLayout skillTagLayout = new FlowLayout(FlowLayout.LEFT, 6, 2);
        JPanel skillTagPanel = new JPanel(skillTagLayout);
        skillTagPanel.setOpaque(false);

        JLabel offersLabel = new JLabel("Offers: " + match.getSkill().getSkillName());
        offersLabel.setFont(Theme.FONT_BODY_BOLD);
        offersLabel.setForeground(Theme.SUCCESS);

        JLabel catBadge = Theme.createStatusBadge(match.getSkill().getCategory());

        skillTagPanel.add(offersLabel);
        skillTagPanel.add(catBadge);

        infoStack.add(nameLabel);
        infoStack.add(Box.createVerticalStrut(2));
        infoStack.add(deptYear);
        infoStack.add(Box.createVerticalStrut(4));
        infoStack.add(skillTagPanel);

        leftPanel.add(avatar);
        leftPanel.add(infoStack);

        // Right: Rating + Match Score + Action Button
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        JPanel scoreRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        scoreRow.setOpaque(false);

        JLabel ratingLabel = new JLabel("★ " + (candidate.getReviewCount() > 0 ?
                String.format("%.1f/5 (%d)", candidate.getAverageRating(), candidate.getReviewCount()) : "New Mentor"));
        ratingLabel.setFont(Theme.FONT_BODY_BOLD);
        ratingLabel.setForeground(Theme.STAR_GOLD);

        JLabel scoreBadge = Theme.createStatusBadge("Match: " + match.getFormattedTotalScore());
        scoreBadge.setToolTipText(match.getMatchReason());

        scoreRow.add(ratingLabel);
        scoreRow.add(scoreBadge);

        JButton requestBtn = Theme.createPrimaryButton("Send Learning Request");
        requestBtn.setFont(Theme.FONT_SMALL_BOLD);
        requestBtn.setPreferredSize(new Dimension(190, 36));
        requestBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);

        requestBtn.addActionListener(e -> {
            boolean confirm = Theme.showConfirm(this,
                    "Send a learning request to " + candidate.getName() + " for " + match.getSkill().getSkillName() + "?",
                    "Confirm Learning Request");
            if (confirm) {
                try {
                    requestService.sendRequest(currentStudent.getId(), candidate.getId(), match.getSkill().getSkillId());
                    Theme.showSuccess(this, "Learning request sent to " + candidate.getName() + " successfully!");
                    if (onDataChanged != null) onDataChanged.run();
                } catch (Exception ex) {
                    Theme.showError(this, ex.getMessage());
                }
            }
        });

        rightPanel.add(scoreRow);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(requestBtn);

        card.add(leftPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);
        return card;
    }
}
