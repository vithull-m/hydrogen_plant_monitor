package com.hydroplant;

import com.hydroplant.dao.DBConnection;
import com.hydroplant.view.LoginDialog;
import com.hydroplant.view.MainFrame;

import javax.swing.*;

/**
 * Application entry point. Sets the look and feel, shows the login
 * dialog, checks (and warns about) database connectivity, then launches
 * the main window.
 */
public class Main {

    public static void main(String[] args) {
        // Use the OS-native look and feel where available for a more
        // professional appearance.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back silently to the default cross-platform L&F.
        }

        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            if (!login.isSucceeded()) {
                System.exit(0);
                return;
            }

            if (!DBConnection.testConnection()) {
                JOptionPane.showMessageDialog(null,
                        "Warning: could not connect to the MySQL database.\n" +
                        "Check src/main/resources/db.properties and make sure MySQL is running\n" +
                        "and the schema has been imported (see database/schema.sql).\n\n" +
                        "The application will still open, but data screens will show errors\n" +
                        "until the database connection is fixed.",
                        "Database Connection Warning",
                        JOptionPane.WARNING_MESSAGE);
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
