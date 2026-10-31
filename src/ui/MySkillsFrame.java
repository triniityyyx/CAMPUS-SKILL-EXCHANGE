package ui;

import model.Skill;
import model.Student;
import model.StudentSkill;
import service.SkillService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Screen to manage Skills I Can Teach (Offered) and Skills I Want to Learn (Wanted).
 */
public class MySkillsFrame extends JFrame {

    private final Student currentStudent;
    private final SkillService skillService = new SkillService();
    private final Runnable onDataChanged;

    private JPanel offeredListPanel;
    private JPanel wantedListPanel;
    private JLabel offeredCountLabel;
    private JLabel wantedCountLabel;

    public MySkillsFrame(Student student, Runnable onDataChanged) {
        this.currentStudent = student;
        this.onDataChanged = onDataChanged;

        setTitle("Campus Skill Exchange - My Skills (" + student.getName() + ")");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(920, 640);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);

        initUI();
        loadSkills();
    }

    public MySkillsFrame(Student student) {
        this(student, null);
    }

    public JPanel createPanel() {
        JPanel container = new JPanel(new BorderLayout(16, 16));
        container.setBackground(Theme.BG_DARK);
        container.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("💡 Manage Your Skills");
        title.setFont(Theme.FONT_TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Add the skills you can mentor others in, and skills you are eager to learn.");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        container.add(headerPanel, BorderLayout.NORTH);

        // Center: Split layout between Offered and Wanted
        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        columnsPanel.setOpaque(false);

        // --- Column 1: Skills I Can Teach ---
        JPanel offeredCol = Theme.createCardPanel(14);
        offeredCol.setLayout(new BorderLayout(10, 10));
        offeredCol.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel offeredTop = new JPanel(new BorderLayout());
        offeredTop.setOpaque(false);

        JPanel offeredTitleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        offeredTitleWrap.setOpaque(false);
        JLabel offTitle = new JLabel("Skills I Can Teach");
        offTitle.setFont(Theme.FONT_TITLE);
        offTitle.setForeground(Theme.SUCCESS);
        offeredCountLabel = Theme.createStatusBadge("0 OFFERED");
        offeredTitleWrap.add(offTitle);
        offeredTitleWrap.add(offeredCountLabel);

        JButton addOfferedBtn = Theme.createSuccessButton("+ Add Skill to Teach");
        addOfferedBtn.setFont(Theme.FONT_SMALL_BOLD);
        addOfferedBtn.setPreferredSize(new Dimension(170, 32));
        addOfferedBtn.addActionListener(e -> showAddSkillDialog(StudentSkill.TYPE_OFFERED));

        offeredTop.add(offeredTitleWrap, BorderLayout.WEST);
        offeredTop.add(addOfferedBtn, BorderLayout.EAST);

        offeredListPanel = new JPanel();
        offeredListPanel.setLayout(new BoxLayout(offeredListPanel, BoxLayout.Y_AXIS));
        offeredListPanel.setOpaque(false);

        JScrollPane offScroll = Theme.createScrollPane(offeredListPanel);
        offeredCol.add(offeredTop, BorderLayout.NORTH);
        offeredCol.add(offScroll, BorderLayout.CENTER);

        // --- Column 2: Skills I Want to Learn ---
        JPanel wantedCol = Theme.createCardPanel(14);
        wantedCol.setLayout(new BorderLayout(10, 10));
        wantedCol.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel wantedTop = new JPanel(new BorderLayout());
        wantedTop.setOpaque(false);

        JPanel wantedTitleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        wantedTitleWrap.setOpaque(false);
        JLabel wanTitle = new JLabel("Skills I Want to Learn");
        wanTitle.setFont(Theme.FONT_TITLE);
        wanTitle.setForeground(Theme.PRIMARY_LIGHT);
        wantedCountLabel = Theme.createStatusBadge("0 WANTED");
        wantedTitleWrap.add(wanTitle);
        wantedTitleWrap.add(wantedCountLabel);

        JButton addWantedBtn = Theme.createPrimaryButton("+ Add Skill to Learn");
        addWantedBtn.setFont(Theme.FONT_SMALL_BOLD);
        addWantedBtn.setPreferredSize(new Dimension(170, 32));
        addWantedBtn.addActionListener(e -> showAddSkillDialog(StudentSkill.TYPE_WANTED));

        wantedTop.add(wantedTitleWrap, BorderLayout.WEST);
        wantedTop.add(addWantedBtn, BorderLayout.EAST);

        wantedListPanel = new JPanel();
        wantedListPanel.setLayout(new BoxLayout(wantedListPanel, BoxLayout.Y_AXIS));
        wantedListPanel.setOpaque(false);

        JScrollPane wanScroll = Theme.createScrollPane(wantedListPanel);
        wantedCol.add(wantedTop, BorderLayout.NORTH);
        wantedCol.add(wanScroll, BorderLayout.CENTER);

        columnsPanel.add(offeredCol);
        columnsPanel.add(wantedCol);
        container.add(columnsPanel, BorderLayout.CENTER);

        return container;
    }

    private void initUI() {
        setContentPane(createPanel());
    }

    public void loadSkills() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<StudentSkill> offeredList;
            List<StudentSkill> wantedList;

            @Override
            protected Void doInBackground() throws Exception {
                offeredList = skillService.getOfferedSkills(currentStudent.getId());
                wantedList = skillService.getWantedSkills(currentStudent.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    renderOfferedList(offeredList);
                    renderWantedList(wantedList);
                } catch (Exception e) {
                    Theme.showError(MySkillsFrame.this, "Failed to load skills: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void renderOfferedList(List<StudentSkill> list) {
        offeredListPanel.removeAll();
        offeredCountLabel.setText(list.size() + " OFFERED");

        if (list.isEmpty()) {
            JLabel empty = new JLabel("No skills added yet. Click '+ Add Skill to Teach' above!", SwingConstants.CENTER);
            empty.setFont(Theme.FONT_BODY);
            empty.setForeground(Theme.TEXT_MUTED);
            empty.setBorder(new EmptyBorder(30, 0, 0, 0));
            offeredListPanel.add(empty);
        } else {
            for (StudentSkill ss : list) {
                offeredListPanel.add(createSkillCard(ss, Theme.SUCCESS));
                offeredListPanel.add(Box.createVerticalStrut(10));
            }
        }
        offeredListPanel.revalidate();
        offeredListPanel.repaint();
    }

    private void renderWantedList(List<StudentSkill> list) {
        wantedListPanel.removeAll();
        wantedCountLabel.setText(list.size() + " WANTED");

        if (list.isEmpty()) {
            JLabel empty = new JLabel("No skills added yet. Click '+ Add Skill to Learn' above!", SwingConstants.CENTER);
            empty.setFont(Theme.FONT_BODY);
            empty.setForeground(Theme.TEXT_MUTED);
            empty.setBorder(new EmptyBorder(30, 0, 0, 0));
            wantedListPanel.add(empty);
        } else {
            for (StudentSkill ss : list) {
                wantedListPanel.add(createSkillCard(ss, Theme.PRIMARY));
                wantedListPanel.add(Box.createVerticalStrut(10));
            }
        }
        wantedListPanel.revalidate();
        wantedListPanel.repaint();
    }

    private JPanel createSkillCard(StudentSkill ss, Color accentColor) {
        JPanel card = Theme.createCardPanel(10, Theme.BG_CARD_ALT);
        card.setLayout(new BorderLayout(12, 6));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 58));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        JLabel name = new JLabel(ss.getSkillName());
        name.setFont(Theme.FONT_BODY_BOLD);
        name.setForeground(Theme.TEXT_PRIMARY);

        JLabel cat = new JLabel(ss.getCategory() != null ? ss.getCategory() : "General");
        cat.setFont(Theme.FONT_SMALL);
        cat.setForeground(Theme.TEXT_SECONDARY);

        info.add(name);
        info.add(cat);

        // Delete button
        JButton delBtn = Theme.createDangerButton("✕ Remove");
        delBtn.setFont(Theme.FONT_SMALL_BOLD);
        delBtn.setPreferredSize(new Dimension(90, 28));
        delBtn.addActionListener(e -> {
            boolean confirm = Theme.showConfirm(this,
                    "Are you sure you want to remove '" + ss.getSkillName() + "' from your " + ss.getSkillType().toLowerCase() + " skills?",
                    "Confirm Remove Skill");
            if (confirm) {
                try {
                    skillService.removeSkillFromStudent(currentStudent.getId(), ss.getSkillId(), ss.getSkillType());
                    loadSkills();
                    if (onDataChanged != null) onDataChanged.run();
                } catch (Exception ex) {
                    Theme.showError(this, "Failed to remove skill: " + ex.getMessage());
                }
            }
        });

        card.add(info, BorderLayout.CENTER);
        card.add(delBtn, BorderLayout.EAST);
        return card;
    }

    private void showAddSkillDialog(String skillType) {
        JDialog dialog = new JDialog(this, "Add Skill (" + skillType + ")", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Theme.BG_DARK);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel(skillType.equals(StudentSkill.TYPE_OFFERED) ? "Offer a Skill You Can Teach" : "Request a Skill You Want to Learn");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = new JLabel("Skill Name (e.g. Java, Python, UI/UX)");
        nameLbl.setFont(Theme.FONT_SMALL_BOLD);
        nameLbl.setForeground(Theme.TEXT_SECONDARY);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameField = Theme.createTextField("Enter skill name");
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameField.setMaximumSize(new Dimension(360, 38));

        JLabel catLbl = new JLabel("Category");
        catLbl.setFont(Theme.FONT_SMALL_BOLD);
        catLbl.setForeground(Theme.TEXT_SECONDARY);
        catLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> catCombo = Theme.createComboBox(skillService.getCategories().toArray(new String[0]));
        catCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        catCombo.setMaximumSize(new Dimension(360, 38));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPanel.setMaximumSize(new Dimension(360, 40));

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(100, 36));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton addBtn = skillType.equals(StudentSkill.TYPE_OFFERED) ?
                Theme.createSuccessButton("Add Skill") : Theme.createPrimaryButton("Add Skill");
        addBtn.setPreferredSize(new Dimension(120, 36));

        addBtn.addActionListener(e -> {
            String skillName = nameField.getText().trim();
            String cat = (String) catCombo.getSelectedItem();
            if (skillName.isEmpty()) {
                Theme.showError(dialog, "Please enter a skill name.");
                return;
            }

            try {
                skillService.addSkillToStudent(currentStudent.getId(), skillName, cat, skillType);
                Theme.showSuccess(dialog, "'" + skillName + "' added successfully to your " + skillType.toLowerCase() + " skills!");
                dialog.dispose();
                loadSkills();
                if (onDataChanged != null) onDataChanged.run();
            } catch (Exception ex) {
                Theme.showError(dialog, ex.getMessage());
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(addBtn);

        panel.add(title);
        panel.add(Box.createVerticalStrut(14));
        panel.add(nameLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(catLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(catCombo);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnPanel);

        dialog.setContentPane(panel);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
            }
        });
        dialog.setVisible(true);
    }
}
