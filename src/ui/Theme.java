package ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern Dark Theme Design System for Campus Skill Exchange.
 * Provides cohesive color palettes, typography, custom rounded components,
 * hover states, badges, and smooth scrollbars.
 */
public class Theme {

    // --- Color Palette ---
    public static final Color BG_DARK        = new Color(15, 23, 42);      // Slate 900 #0F172A
    public static final Color BG_SIDEBAR     = new Color(23, 32, 54);      // Dark slate #172036
    public static final Color BG_CARD        = new Color(30, 41, 59);      // Slate 800 #1E293B
    public static final Color BG_CARD_HOVER  = new Color(44, 57, 80);      // Slate 750
    public static final Color BG_CARD_ALT    = new Color(24, 33, 47);      // Deep Slate
    public static final Color BG_INPUT       = new Color(18, 25, 38);      // Dark input
    
    public static final Color BORDER_COLOR   = new Color(51, 65, 85);      // Slate 700 #334155
    public static final Color BORDER_FOCUS   = new Color(99, 102, 241);    // Indigo 500
    public static final Color BORDER_LIGHT   = new Color(71, 85, 105);

    public static final Color PRIMARY        = new Color(99, 102, 241);    // Indigo 500 #6366F1
    public static final Color PRIMARY_HOVER  = new Color(79, 70, 229);    // Indigo 600
    public static final Color PRIMARY_LIGHT  = new Color(129, 140, 248);

    public static final Color SUCCESS        = new Color(16, 185, 129);    // Emerald 500 #10B981
    public static final Color SUCCESS_HOVER  = new Color(5, 150, 105);
    public static final Color DANGER         = new Color(239, 68, 68);     // Rose 500 #EF4444
    public static final Color DANGER_HOVER   = new Color(220, 38, 38);
    public static final Color WARNING        = new Color(245, 158, 11);    // Amber 500 #F59E0B
    public static final Color INFO           = new Color(59, 130, 246);     // Blue 500 #3B82F6
    public static final Color PURPLE         = new Color(168, 85, 247);    // Purple 500 #A855F7

    public static final Color TEXT_PRIMARY   = new Color(248, 250, 252);   // Slate 50 #F8FAFC
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);   // Slate 400 #94A3B8
    public static final Color TEXT_MUTED     = new Color(100, 116, 139);   // Slate 500 #64748B
    public static final Color STAR_GOLD      = new Color(251, 191, 36);    // Gold #FBBF24

    // --- Typography ---
    public static final Font FONT_TITLE_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE       = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_SUBTITLE    = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY        = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL_BOLD  = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_SMALL       = new Font("Segoe UI", Font.PLAIN, 11);

    /**
     * Create rounded panel with custom background color and border.
     */
    public static JPanel createCardPanel(int radius, Color bgColor) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor != null ? bgColor : BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    public static JPanel createCardPanel(int radius) {
        return createCardPanel(radius, BG_CARD);
    }

    /**
     * Create modern styled button with hover and click state transitions.
     */
    public static JButton createButton(String text, Color baseColor, Color hoverColor, Color textColor, int radius) {
        JButton btn = new JButton(text) {
            private boolean isHover = false;
            private boolean isPressed = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHover = false;
                        repaint();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        isPressed = true;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        isPressed = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentBg = baseColor;
                if (!isEnabled()) {
                    currentBg = BG_CARD_ALT;
                } else if (isPressed) {
                    currentBg = hoverColor.darker();
                } else if (isHover) {
                    currentBg = hoverColor;
                }

                g2.setColor(currentBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));

                FontMetrics fm = g2.getFontMetrics();
                Rectangle stringBounds = fm.getStringBounds(this.getText(), g2).getBounds();
                int textX = (getWidth() - stringBounds.width) / 2;
                int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();

                g2.setColor(isEnabled() ? textColor : TEXT_MUTED);
                g2.setFont(getFont());
                g2.drawString(getText(), textX, textY);
                g2.dispose();
            }
        };

        btn.setFont(FONT_BODY_BOLD);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 24, 38));
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, PRIMARY, PRIMARY_HOVER, TEXT_PRIMARY, 10);
    }

    public static JButton createSecondaryButton(String text) {
        return createButton(text, BG_CARD, BG_CARD_HOVER, TEXT_PRIMARY, 10);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, SUCCESS, SUCCESS_HOVER, TEXT_PRIMARY, 10);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER, DANGER_HOVER, TEXT_PRIMARY, 10);
    }

    /**
     * Create rounded modern text field.
     */
    public static JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();

                super.paintComponent(g);

                if (getText().isEmpty() && !hasFocus() && placeholder != null) {
                    Graphics2D g3 = (Graphics2D) g.create();
                    g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g3.setColor(TEXT_MUTED);
                    g3.setFont(FONT_BODY);
                    Insets insets = getInsets();
                    FontMetrics fm = g3.getFontMetrics();
                    int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g3.drawString(placeholder, insets.left + 2, textY);
                    g3.dispose();
                }
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasFocus() ? BORDER_FOCUS : BORDER_COLOR);
                g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
            }
        };

        tf.setEditable(true);
        tf.setEnabled(true);
        tf.setFocusable(true);
        tf.setOpaque(false);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setSelectedTextColor(Color.WHITE);
        tf.setSelectionColor(PRIMARY);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        tf.setPreferredSize(new Dimension(200, 38));

        tf.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                tf.requestFocusInWindow();
            }
        });

        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                tf.repaint();
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                tf.repaint();
            }
        });

        return tf;
    }

    /**
     * Create rounded modern password field.
     */
    public static JPasswordField createPasswordField(String placeholder) {
        JPasswordField pf = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();

                super.paintComponent(g);

                if (getPassword().length == 0 && !hasFocus() && placeholder != null) {
                    Graphics2D g3 = (Graphics2D) g.create();
                    g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g3.setColor(TEXT_MUTED);
                    g3.setFont(FONT_BODY);
                    Insets insets = getInsets();
                    FontMetrics fm = g3.getFontMetrics();
                    int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g3.drawString(placeholder, insets.left + 2, textY);
                    g3.dispose();
                }
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasFocus() ? BORDER_FOCUS : BORDER_COLOR);
                g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
            }
        };

        pf.setEditable(true);
        pf.setEnabled(true);
        pf.setFocusable(true);
        pf.setOpaque(false);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(TEXT_PRIMARY);
        pf.setSelectedTextColor(Color.WHITE);
        pf.setSelectionColor(PRIMARY);
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        pf.setPreferredSize(new Dimension(200, 38));

        pf.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pf.requestFocusInWindow();
            }
        });

        pf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                pf.repaint();
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                pf.repaint();
            }
        });

        return pf;
    }

    /**
     * Style standard combo box.
     */
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_PRIMARY);
        cb.setFont(FONT_BODY);
        cb.setFocusable(false);
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width, 38));
        return cb;
    }

    /**
     * Create pill badge for request or session status.
     */
    public static JLabel createStatusBadge(String status) {
        Color bg;
        Color fg = Color.WHITE;
        String text = status != null ? status.toUpperCase() : "UNKNOWN";

        switch (text) {
            case "PENDING":
                bg = new Color(180, 83, 9); // Amber dark
                break;
            case "ACCEPTED":
                bg = new Color(37, 99, 235); // Blue
                break;
            case "REJECTED":
            case "CANCELLED":
                bg = new Color(220, 38, 38); // Red
                break;
            case "SCHEDULED":
                bg = new Color(79, 70, 229); // Indigo
                break;
            case "COMPLETED":
                bg = new Color(5, 150, 105); // Green
                break;
            case "REVIEWED":
                bg = new Color(147, 51, 234); // Purple
                break;
            default:
                bg = BG_CARD_ALT;
                fg = TEXT_SECONDARY;
                break;
        }

        JLabel label = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setFont(FONT_SMALL_BOLD);
        label.setForeground(fg);
        label.setOpaque(false);
        label.setBorder(new EmptyBorder(3, 10, 3, 10));
        return label;
    }

    /**
     * Styled scroll pane matching dark theme.
     */
    public static JScrollPane createScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = BORDER_COLOR;
                this.trackColor = BG_DARK;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
        return sp;
    }

    // Modern Dialog Helpers
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        int opt = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return opt == JOptionPane.YES_OPTION;
    }
}
