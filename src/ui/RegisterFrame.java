package ui;

import model.Student;
import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern Dark-Themed Student Registration Frame.
 */
public class RegisterFrame extends JFrame {

    private final AuthService authService = new AuthService();

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> deptCombo;
    private JComboBox<Integer> yearCombo;
    private JButton registerBtn;

    private static final String[] DEPARTMENTS = {
            "Computer Science & Engineering",
            "Information Technology",
            "Electronics & Comm. Engineering",
            "Electrical & Electronics Engineering",
            "Mechanical Engineering",
            "Civil Engineering",
            "Artificial Intelligence & Data Science",
            "Management Studies / MBA",
            "Design & Applied Arts"
    };

    private static final Integer[] YEARS = {1, 2, 3, 4, 5};

    public RegisterFrame() {
        setTitle("Campus Skill Exchange - Create Account");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 680);
        setLocationRelativeTo(null);
        setResizable(false);
        setFocusable(true);
        setFocusableWindowState(true);
        setAutoRequestFocus(true);
        getContentPane().setBackground(Theme.BG_DARK);

        initUI();

        // Assign initial keyboard focus to nameField once the window is opened
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
            }
        });
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Theme.BG_DARK);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel card = Theme.createCardPanel(16);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 32, 28, 32));
        card.setPreferredSize(new Dimension(420, 590));

        // Header
        JLabel logoBadge = new JLabel("📝", SwingConstants.CENTER);
        logoBadge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        logoBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Student Registration");
        title.setFont(Theme.FONT_TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Join your campus peer learning community");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Inputs
        nameField = Theme.createTextField("e.g. Rahul Sharma");
        emailField = Theme.createTextField("e.g. rahul@campus.edu");
        passwordField = Theme.createPasswordField("Choose a secure password");
        deptCombo = Theme.createComboBox(DEPARTMENTS);
        yearCombo = Theme.createComboBox(YEARS);

        // Buttons
        registerBtn = Theme.createPrimaryButton("Create Account");
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.setMaximumSize(new Dimension(350, 40));
        registerBtn.addActionListener(e -> performRegistration());

        JButton backBtn = Theme.createSecondaryButton("Back to Login");
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setMaximumSize(new Dimension(350, 38));
        backBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        // Assemble Form
        card.add(logoBadge);
        card.add(Box.createVerticalStrut(6));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));

        card.add(createFieldLabel("Full Name"));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapField(nameField));
        card.add(Box.createVerticalStrut(10));

        card.add(createFieldLabel("College Email Address"));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapField(emailField));
        card.add(Box.createVerticalStrut(10));

        card.add(createFieldLabel("Password"));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapField(passwordField));
        card.add(Box.createVerticalStrut(10));

        card.add(createFieldLabel("Department / Major"));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapField(deptCombo));
        card.add(Box.createVerticalStrut(10));

        card.add(createFieldLabel("Academic Year"));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapField(yearCombo));
        card.add(Box.createVerticalStrut(20));

        card.add(registerBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(backBtn);

        mainPanel.add(card);
        add(mainPanel);
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BODY_BOLD);
        l.setForeground(Theme.TEXT_SECONDARY);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JComponent wrapField(JComponent c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.setMaximumSize(new Dimension(350, 38));
        return c;
    }

    private void performRegistration() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String dept = (String) deptCombo.getSelectedItem();
        Integer year = (Integer) yearCombo.getSelectedItem();

        registerBtn.setEnabled(false);
        registerBtn.setText("Registering...");

        SwingWorker<Student, Void> worker = new SwingWorker<>() {
            @Override
            protected Student doInBackground() throws Exception {
                return authService.register(name, email, password, dept, year != null ? year : 1);
            }

            @Override
            protected void done() {
                registerBtn.setEnabled(true);
                registerBtn.setText("Create Account");
                try {
                    Student registered = get();
                    if (registered != null) {
                        Theme.showSuccess(RegisterFrame.this,
                                "Account registered successfully!\nWelcome to Campus Skill Exchange, " + registered.getName() + "!");
                        dispose();
                        new DashboardFrame(registered).setVisible(true);
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    Theme.showError(RegisterFrame.this, cause.getMessage());
                }
            }
        };
        worker.execute();
    }
}
