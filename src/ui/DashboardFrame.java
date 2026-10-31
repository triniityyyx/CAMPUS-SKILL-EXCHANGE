package ui;

import dao.SkillDAO;
import dao.StudentDAO;
import model.Student;
import service.RequestService;
import service.SessionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Application Dashboard Frame for Campus Skill Exchange.
 * Provides sidebar navigation, live student stats, and views for:
 * Dashboard, My Skills, Find Skills, Requests, Sessions, Reviews.
 */
public class DashboardFrame extends JFrame {

    private final Student currentStudent;
    private final StudentDAO studentDAO = new StudentDAO();
    private final SkillDAO skillDAO = new SkillDAO();
    private final RequestService requestService = new RequestService();
    private final SessionService sessionService = new SessionService();

    // View Components
    private CardLayout contentCardLayout;
    private JPanel contentContainer;

    private MySkillsFrame mySkillsView;
    private SkillSearchFrame skillSearchView;
    private RequestsFrame requestsView;
    private SessionsFrame sessionsView;
    private ReviewsFrame reviewsView;

    // Stat card labels
    private JLabel statOfferedLabel;
    private JLabel statWantedLabel;
    private JLabel statPendingLabel;
    private JLabel statSessionsLabel;
    private JLabel statRatingLabel;
    private JLabel sidebarRatingBadge;
    private JLabel navPendingBadge;

    private final Map<String, JButton> navButtons = new HashMap<>();
    private String activeNav = "DASHBOARD";

    public DashboardFrame(Student student) {
        this.currentStudent = student;

        setTitle("Campus Skill Exchange - " + student.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);

        initUI();
        refreshAllData();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_DARK);

        // 1. Sidebar Navigation
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // 2. Central Content Area
        contentCardLayout = new CardLayout();
        contentContainer = new JPanel(contentCardLayout);
        contentContainer.setBackground(Theme.BG_DARK);

        // Instantiate sub-views with callbacks for cross-communication
        Runnable dataRefreshCallback = this::refreshAllData;

        RequestsFrame.SessionsFrameCallback sessionsCallback = (requestId, skillName, partnerName) -> {
            switchView("SESSIONS");
            sessionsView.showScheduleDialog(requestId, skillName, partnerName);
        };

        SessionsFrame.ReviewsCallback reviewsCallback = (sessionId, skillName, partnerName) -> {
            switchView("REVIEWS");
            reviewsView.showReviewDialog(sessionId, skillName, partnerName);
        };

        mySkillsView = new MySkillsFrame(currentStudent, dataRefreshCallback);
        skillSearchView = new SkillSearchFrame(currentStudent, dataRefreshCallback);
        requestsView = new RequestsFrame(currentStudent, dataRefreshCallback, sessionsCallback);
        sessionsView = new SessionsFrame(currentStudent, dataRefreshCallback, reviewsCallback);
        reviewsView = new ReviewsFrame(currentStudent, dataRefreshCallback);

        // Add views to CardLayout
        contentContainer.add(createDashboardOverviewPanel(), "DASHBOARD");
        contentContainer.add(mySkillsView.createPanel(), "MY_SKILLS");
        contentContainer.add(skillSearchView.createPanel(), "FIND_SKILLS");
        contentContainer.add(requestsView.createPanel(), "REQUESTS");
        contentContainer.add(sessionsView.createPanel(), "SESSIONS");
        contentContainer.add(reviewsView.createPanel(), "REVIEWS");

        root.add(contentContainer, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(Theme.BG_SIDEBAR);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_COLOR));

        // Sidebar Top: Brand & User Profile
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(22, 18, 16, 18));

        JLabel brand = new JLabel("🎓 CAMPUS SKILL");
        brand.setFont(Theme.FONT_TITLE);
        brand.setForeground(Theme.TEXT_PRIMARY);

        JLabel brandSub = new JLabel("EXCHANGE PLATFORM");
        brandSub.setFont(Theme.FONT_SMALL_BOLD);
        brandSub.setForeground(Theme.PRIMARY_LIGHT);

        top.add(brand);
        top.add(Box.createVerticalStrut(2));
        top.add(brandSub);
        top.add(Box.createVerticalStrut(18));

        // Student Profile Widget
        JPanel profileCard = Theme.createCardPanel(12, Theme.BG_CARD_ALT);
        profileCard.setLayout(new BorderLayout(10, 8));
        profileCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        profileCard.setMaximumSize(new Dimension(220, 80));

        JLabel avatar = new JLabel(getInitials(currentStudent.getName()), SwingConstants.CENTER) {
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
        avatar.setFont(Theme.FONT_BODY_BOLD);
        avatar.setForeground(Color.WHITE);
        avatar.setPreferredSize(new Dimension(38, 38));

        JPanel nameDept = new JPanel(new GridLayout(2, 1, 0, 2));
        nameDept.setOpaque(false);
        JLabel nameLbl = new JLabel(currentStudent.getName());
        nameLbl.setFont(Theme.FONT_BODY_BOLD);
        nameLbl.setForeground(Theme.TEXT_PRIMARY);

        JLabel deptLbl = new JLabel("Year " + currentStudent.getYear() + " • " + currentStudent.getDepartment());
        deptLbl.setFont(Theme.FONT_SMALL);
        deptLbl.setForeground(Theme.TEXT_SECONDARY);

        nameDept.add(nameLbl);
        nameDept.add(deptLbl);

        profileCard.add(avatar, BorderLayout.WEST);
        profileCard.add(nameDept, BorderLayout.CENTER);

        sidebarRatingBadge = Theme.createStatusBadge("★ " + currentStudent.getFormattedRating());
        sidebarRatingBadge.setFont(Theme.FONT_SMALL_BOLD);
        sidebarRatingBadge.setForeground(Theme.STAR_GOLD);
        profileCard.add(sidebarRatingBadge, BorderLayout.SOUTH);

        top.add(profileCard);
        sidebar.add(top, BorderLayout.NORTH);

        // Sidebar Middle: Navigation Menu
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setOpaque(false);
        menu.setBorder(new EmptyBorder(10, 14, 10, 14));

        menu.add(createNavButton("📊 Dashboard", "DASHBOARD"));
        menu.add(Box.createVerticalStrut(6));
        menu.add(createNavButton("💡 My Skills", "MY_SKILLS"));
        menu.add(Box.createVerticalStrut(6));
        menu.add(createNavButton("🔍 Find Skills", "FIND_SKILLS"));
        menu.add(Box.createVerticalStrut(6));

        // Requests button with pending badge
        JPanel reqWrap = new JPanel(new BorderLayout());
        reqWrap.setOpaque(false);
        reqWrap.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        JButton reqBtn = createNavButton("📩 Requests", "REQUESTS");
        navPendingBadge = Theme.createStatusBadge("0");
        navPendingBadge.setVisible(false);
        reqWrap.add(reqBtn, BorderLayout.CENTER);
        menu.add(reqWrap);
        menu.add(Box.createVerticalStrut(6));

        menu.add(createNavButton("📅 Sessions", "SESSIONS"));
        menu.add(Box.createVerticalStrut(6));
        menu.add(createNavButton("⭐ Reviews", "REVIEWS"));

        sidebar.add(menu, BorderLayout.CENTER);

        // Sidebar Bottom: Logout
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(14, 14, 18, 14));

        JButton logoutBtn = Theme.createDangerButton("🚪 Sign Out");
        logoutBtn.setPreferredSize(new Dimension(200, 38));
        logoutBtn.addActionListener(e -> {
            boolean confirm = Theme.showConfirm(this, "Are you sure you want to sign out?", "Sign Out");
            if (confirm) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        bottom.add(logoutBtn, BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String text, String viewKey) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = activeNav.equals(viewKey);

                if (isActive) {
                    g2.setColor(Theme.PRIMARY);
                } else if (getModel().isRollover()) {
                    g2.setColor(Theme.BG_CARD_HOVER);
                } else {
                    g2.setColor(new Color(0, 0, 0, 0));
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(Theme.FONT_BODY_BOLD);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        btn.addActionListener(e -> switchView(viewKey));

        navButtons.put(viewKey, btn);
        return btn;
    }

    public void switchView(String viewKey) {
        activeNav = viewKey;
        contentCardLayout.show(contentContainer, viewKey);

        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            entry.getValue().repaint();
        }

        // Trigger refresh on view entry
        if ("DASHBOARD".equals(viewKey)) refreshAllData();
        else if ("MY_SKILLS".equals(viewKey)) mySkillsView.loadSkills();
        else if ("FIND_SKILLS".equals(viewKey)) skillSearchView.performSearch("");
        else if ("REQUESTS".equals(viewKey)) requestsView.loadRequests();
        else if ("SESSIONS".equals(viewKey)) sessionsView.loadSessions();
        else if ("REVIEWS".equals(viewKey)) reviewsView.loadReviews();
    }

    private JPanel createDashboardOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 20));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Welcome Header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel welcome = new JLabel("Welcome back, " + currentStudent.getName() + " 👋");
        welcome.setFont(Theme.FONT_TITLE_LARGE);
        welcome.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Here is what's happening with your campus skills and learning exchanges today.");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(Theme.TEXT_SECONDARY);

        top.add(welcome, BorderLayout.NORTH);
        top.add(sub, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);

        // Center: Stats Grid + Quick Actions
        JPanel centerStack = new JPanel();
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.setOpaque(false);

        // 5 Stat Cards Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 5, 14, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Short.MAX_VALUE, 110));

        statOfferedLabel = new JLabel("0", SwingConstants.CENTER);
        statWantedLabel = new JLabel("0", SwingConstants.CENTER);
        statPendingLabel = new JLabel("0", SwingConstants.CENTER);
        statSessionsLabel = new JLabel("0", SwingConstants.CENTER);
        statRatingLabel = new JLabel("0.0", SwingConstants.CENTER);

        statsGrid.add(createStatCard("💡 Skills Offered", statOfferedLabel, Theme.SUCCESS));
        statsGrid.add(createStatCard("🎯 Skills Wanted", statWantedLabel, Theme.PRIMARY_LIGHT));
        statsGrid.add(createStatCard("📩 Pending Requests", statPendingLabel, Theme.WARNING));
        statsGrid.add(createStatCard("📅 Upcoming Sessions", statSessionsLabel, Theme.INFO));
        statsGrid.add(createStatCard("⭐ Average Rating", statRatingLabel, Theme.STAR_GOLD));

        centerStack.add(statsGrid);
        centerStack.add(Box.createVerticalStrut(28));

        // Quick Actions Section
        JLabel quickTitle = new JLabel("🚀 Quick Actions");
        quickTitle.setFont(Theme.FONT_TITLE);
        quickTitle.setForeground(Theme.TEXT_PRIMARY);
        quickTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerStack.add(quickTitle);
        centerStack.add(Box.createVerticalStrut(12));

        JPanel actionsGrid = new JPanel(new GridLayout(2, 2, 16, 16));
        actionsGrid.setOpaque(false);
        actionsGrid.setMaximumSize(new Dimension(Short.MAX_VALUE, 200));

        actionsGrid.add(createActionCard("💡 Manage My Skills",
                "Add skills you can teach or skills you wish to learn from others.",
                "Go to My Skills", () -> switchView("MY_SKILLS")));

        actionsGrid.add(createActionCard("🔍 Find Peer Mentors",
                "Search students offering skills with Rule-Based Match Scoring.",
                "Search Mentors", () -> switchView("FIND_SKILLS")));

        actionsGrid.add(createActionCard("📩 Learning Requests",
                "Review pending invitations, accept requests, or see sent requests.",
                "View Requests", () -> switchView("REQUESTS")));

        actionsGrid.add(createActionCard("📅 Sessions & Feedback",
                "Coordinate meeting dates, complete sessions, and leave peer reviews.",
                "View Sessions", () -> switchView("SESSIONS")));

        centerStack.add(actionsGrid);
        panel.add(centerStack, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = Theme.createCardPanel(12);
        card.setLayout(new BorderLayout(8, 6));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(Theme.FONT_SMALL_BOLD);
        t.setForeground(Theme.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(accent);

        card.add(t, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionCard(String title, String desc, String btnText, Runnable action) {
        JPanel card = Theme.createCardPanel(12);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel textStack = new JPanel(new GridLayout(2, 1, 0, 4));
        textStack.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_SUBTITLE);
        t.setForeground(Theme.TEXT_PRIMARY);

        JLabel d = new JLabel(desc);
        d.setFont(Theme.FONT_SMALL);
        d.setForeground(Theme.TEXT_SECONDARY);

        textStack.add(t);
        textStack.add(d);

        JButton btn = Theme.createSecondaryButton(btnText);
        btn.setPreferredSize(new Dimension(150, 36));
        btn.setFont(Theme.FONT_SMALL_BOLD);
        btn.addActionListener(e -> action.run());

        card.add(textStack, BorderLayout.CENTER);
        card.add(btn, BorderLayout.EAST);
        return card;
    }

    /**
     * Refresh all statistical metrics across dashboard and sub-views.
     */
    public void refreshAllData() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            double[] ratingData;

            @Override
            protected int[] doInBackground() throws Exception {
                int offered = skillDAO.getSkillCountByStudent(currentStudent.getId(), "OFFERED");
                int wanted = skillDAO.getSkillCountByStudent(currentStudent.getId(), "WANTED");
                int pending = requestService.getPendingCount(currentStudent.getId());
                int sessions = sessionService.getUpcomingSessionsCount(currentStudent.getId());
                ratingData = studentDAO.getAverageRatingAndCount(currentStudent.getId());

                return new int[]{offered, wanted, pending, sessions};
            }

            @Override
            protected void done() {
                try {
                    int[] res = get();
                    statOfferedLabel.setText(String.valueOf(res[0]));
                    statWantedLabel.setText(String.valueOf(res[1]));
                    statPendingLabel.setText(String.valueOf(res[2]));
                    statSessionsLabel.setText(String.valueOf(res[3]));

                    if (ratingData[1] > 0) {
                        statRatingLabel.setText(String.format("%.1f ★", ratingData[0]));
                        sidebarRatingBadge.setText(String.format("★ %.1f/5 (%d)", ratingData[0], (int) ratingData[1]));
                    } else {
                        statRatingLabel.setText("New ★");
                        sidebarRatingBadge.setText("★ New Student");
                    }

                    if (res[2] > 0) {
                        navPendingBadge.setText(res[2] + " NEW");
                        navPendingBadge.setVisible(true);
                    } else {
                        navPendingBadge.setVisible(false);
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
