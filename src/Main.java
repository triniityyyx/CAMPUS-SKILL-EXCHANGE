import ui.LoginFrame;

import javax.swing.*;

/**
 * Main application entry point for CAMPUS SKILL EXCHANGE.
 */
public class Main {

    public static void main(String[] args) {
        // Enable sub-pixel text anti-aliasing for modern sharp typography
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Set System Look and Feel for native, responsive input handling
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored2) {
            }
        }

        System.out.println("==================================================");
        System.out.println("         CAMPUS SKILL EXCHANGE PLATFORM           ");
        System.out.println("   Peer-to-Peer College Skill Exchange Platform   ");
        System.out.println("==================================================");
        System.out.println("Starting application GUI...");

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
