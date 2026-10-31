package ui;

import model.Student;
import service.AuthService;
import util.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Modern Dark-Themed Login Frame.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();

    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginBtn;

    public LoginFrame() {
        setTitle("Campus Skill Exchange - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        setFocusable(true);
        setFocusableWindowState(true);
        setAutoRequestFocus(true);
        getContentPane().setBackground(Theme.BG_DARK);

        initUI();

        // Assign initial keyboard focus to emailField once the window is opened
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                SwingUtilities.invokeLater(() -> emailField.requestFocusInWindow());
            }
        });
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Theme.BG_DARK);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel card = Theme.createCardPanel(16);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 32, 30, 32));
        card.setPreferredSize(new Dimension(380, 470));

        // App Icon / Logo Header
        JLabel logoBadge = new JLabel("🎓", SwingConstants.CENTER);
        logoBadge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        logoBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appTitle = new JLabel("CAMPUS SKILL EXCHANGE");
        appTitle.setFont(Theme.FONT_TITLE_LARGE);
        appTitle.setForeground(Theme.TEXT_PRIMARY);
        appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Peer-to-Peer Student Learning Platform");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(Theme.FONT_BODY_BOLD);
        emailLabel.setForeground(Theme.TEXT_SECONDARY);
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emailField = Theme.createTextField("e.g. rahul@campus.edu");
        emailField.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailField.setMaximumSize(new Dimension(320, 38));

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.FONT_BODY_BOLD);
        passLabel.setForeground(Theme.TEXT_SECONDARY);
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        passwordField = Theme.createPasswordField("Enter your password");
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(320, 38));

        // Enter key submits login
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        emailField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);

        // Buttons
        loginBtn = Theme.createPrimaryButton("Sign In");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(320, 40));
        loginBtn.addActionListener(e -> performLogin());

        JButton registerBtn = Theme.createSecondaryButton("Create New Account");
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.setMaximumSize(new Dimension(320, 38));
        registerBtn.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        // Database status indicator
        statusLabel = new JLabel("Checking database connection...", SwingConstants.CENTER);
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkDatabaseStatus();

        // Assemble Card
        card.add(logoBadge);
        card.add(Box.createVerticalStrut(8));
        card.add(appTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));

        card.add(emailLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(emailField);
        card.add(Box.createVerticalStrut(14));

        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(22));

        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(16));
        card.add(statusLabel);

        mainPanel.add(card);
        add(mainPanel);
    }

    private void checkDatabaseStatus() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return DBConnection.testConnection();
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        statusLabel.setText("● MySQL Connected");
                        statusLabel.setForeground(Theme.SUCCESS);
                    } else {
                        statusLabel.setText("⚠ MySQL Not Reachable (Check db.properties)");
                        statusLabel.setForeground(Theme.WARNING);
                    }
                } catch (Exception ignored) {
                    statusLabel.setText("⚠ Database Status Unknown");
                    statusLabel.setForeground(Theme.DANGER);
                }
            }
        };
        worker.execute();
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty()) {
            Theme.showError(this, "Please enter your email address.");
            emailField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            Theme.showError(this, "Please enter your password.");
            passwordField.requestFocus();
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in...");

        SwingWorker<Student, Void> worker = new SwingWorker<>() {
            @Override
            protected Student doInBackground() throws Exception {
                return authService.login(email, password);
            }

            @Override
            protected void done() {
                loginBtn.setEnabled(true);
                loginBtn.setText("Sign In");
                try {
                    Student loggedIn = get();
                    if (loggedIn != null) {
                        dispose();
                        DashboardFrame dashboard = new DashboardFrame(loggedIn);
                        dashboard.setVisible(true);
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    Theme.showError(LoginFrame.this, cause.getMessage());
                }
            }
        };
        worker.execute();
    }
}
