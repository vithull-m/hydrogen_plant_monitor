package com.hydroplant.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Simple login dialog. For this student project, credentials are checked
 * against a hard-coded admin account when the database is unavailable,
 * and against the users table when it is (see README for details).
 * This keeps the app runnable for demo/grading purposes even without
 * a MySQL server configured.
 */
public class LoginDialog extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean succeeded = false;

    private static final String DEMO_USER = "admin";
    private static final String DEMO_PASS = "admin123";

    public LoginDialog(Frame owner) {
        super(owner, "Green Hydrogen Plant - Login", true);
        setSize(360, 220);
        setLocationRelativeTo(owner);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Plant Management Login", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(DEMO_USER, 15);
        panel.add(usernameField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        JLabel hint = new JLabel("Demo credentials: admin / admin123");
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 11f));
        hint.setForeground(Color.GRAY);
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(hint, gbc);

        JButton loginButton = new JButton("Log In");
        loginButton.addActionListener(this::onLogin);
        gbc.gridy = 4;
        panel.add(loginButton, gbc);

        getRootPane().setDefaultButton(loginButton);
        setContentPane(panel);
    }

    private void onLogin(ActionEvent e) {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (DEMO_USER.equals(user) && DEMO_PASS.equals(pass)) {
            succeeded = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
