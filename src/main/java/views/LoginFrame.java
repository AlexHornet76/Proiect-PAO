package main.java.views;

import main.java.AuditService;
import main.java.DAOs.UserDAO;
import main.java.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

public class LoginFrame {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private UserDAO userDAO;
    private JButton registerButton;
    private AuditService auditService;

    public LoginFrame() {
        // Initialize audit service
        auditService = AuditService.getInstance();

        // Initialize database connection
        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection connection = dbConnection.connect();
        userDAO = new UserDAO(connection);

        // Initialize frame
        frame = new JFrame("Login");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Create the main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create components
        JLabel titleLabel = new JLabel("Welcome! Please login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        loginButton = new JButton("Login");

        // Create register button
        registerButton = new JButton("Register");

        // Add register button to panel (after login button)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(registerButton, gbc);

        // Add action listener for register button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                auditService.logAction("OPEN_REGISTRATION_DIALOG");
                showRegistrationDialog();
            }
        });

        // Add components to panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(loginButton, gbc);

        // Add action listener to login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (authenticate(username, password)) {
                    auditService.logAction("SUCCESSFUL_LOGIN");
                    frame.dispose();
                    SwingUtilities.invokeLater(() -> {
                        MainFrame mainFrame = new MainFrame();
                        mainFrame.show();
                    });
                } else {
                    auditService.logAction("FAILED_LOGIN_ATTEMPT");
                    JOptionPane.showMessageDialog(frame,
                            "Invalid username or password",
                            "Login Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add panel to frame and display
        frame.add(mainPanel);
    }

    private void showRegistrationDialog() {
        JDialog dialog = new JDialog(frame, "Register New Account", true);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(frame);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        JPasswordField confirmPasswordField = new JPasswordField(20);

        JButton submitButton = new JButton("Create Account");
        JButton cancelButton = new JButton("Cancel");

        // Add components to panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        panel.add(confirmPasswordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        // Action listeners
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());

                // Validation
                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    auditService.logAction("REGISTRATION_VALIDATION_FAILED");
                    JOptionPane.showMessageDialog(dialog,
                            "All fields are required",
                            "Registration Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    auditService.logAction("REGISTRATION_PASSWORD_MISMATCH");
                    JOptionPane.showMessageDialog(dialog,
                            "Passwords do not match",
                            "Registration Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check if username already exists
                if (userDAO.usernameExists(username)) {
                    auditService.logAction("REGISTRATION_USERNAME_EXISTS");
                    JOptionPane.showMessageDialog(dialog,
                            "Username already exists",
                            "Registration Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Create new user
                boolean success = userDAO.createUser(username, password);
                if (success) {
                    auditService.logAction("SUCCESSFUL_REGISTRATION");
                    JOptionPane.showMessageDialog(dialog,
                            "Registration successful! Please log in.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    auditService.logAction("FAILED_REGISTRATION");
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to create account",
                            "Registration Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                auditService.logAction("REGISTRATION_CANCELLED");
                dialog.dispose();
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private boolean authenticate(String username, String password) {
        return userDAO.authenticate(username, password);
    }

    public void show() {
        auditService.logAction("LOGIN_SCREEN_SHOWN");
        frame.setVisible(true);
    }
}