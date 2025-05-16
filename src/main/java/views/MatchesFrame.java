package main.java.views;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;

import main.java.DAOs.MatchDAO;
import main.java.DatabaseConnection;
import main.java.models.Match;

import java.sql.SQLException;
import java.util.List;

public class MatchesFrame {
    private JFrame frame;
    private JPanel matchesPanel;
    private MatchDAO matchDAO;
    private boolean showingUpcoming = true;
    private JButton toggleButton;

    public MatchesFrame() {
        // Initialize database connection
        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection connection = dbConnection.connect();
        matchDAO = new MatchDAO(connection);

        // Create the main frame
        frame = new JFrame("Matches");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // Create layout
        frame.setLayout(new BorderLayout());

        // Top panel with controls
        JPanel topPanel = new JPanel(new BorderLayout());

        // Back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> frame.dispose());

        // Toggle button
        toggleButton = new JButton("Show Played Matches");
        toggleButton.addActionListener(e -> toggleMatchesView());

        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(toggleButton, BorderLayout.EAST);

        // Matches content panel
        matchesPanel = new JPanel();
        matchesPanel.setLayout(new BoxLayout(matchesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(matchesPanel);

        // Load initial data
        loadUpcomingMatches();

        // Add panels to frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
    }

    private void toggleMatchesView() {
        if (showingUpcoming) {
            loadPlayedMatches();
            toggleButton.setText("Show Upcoming Matches");
            showingUpcoming = false;
        } else {
            loadUpcomingMatches();
            toggleButton.setText("Show Played Matches");
            showingUpcoming = true;
        }
    }

    private void loadUpcomingMatches() {
        matchesPanel.removeAll();

        JLabel titleLabel = new JLabel("Upcoming Matches");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        matchesPanel.add(titleLabel);
        matchesPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        try {
            List<Match> upcomingMatches = matchDAO.getUpcomingMatches();

            if (upcomingMatches.isEmpty()) {
                JLabel noMatchesLabel = new JLabel("No upcoming matches found");
                noMatchesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                matchesPanel.add(noMatchesLabel);
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                for (Match match : upcomingMatches) {
                    String dateStr = dateFormat.format(match.getDate());
                    String timeStr = timeFormat.format(match.getDate());
                    String teams = match.getHomeTeam() + " vs " + match.getAwayTeam();

                    JPanel matchPanel = createMatchPanel(teams, dateStr, timeStr);

                    // Make the panel clickable to open scoreboard
                    matchPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    matchPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                            openScoreboard(match);
                        }
                    });

                    matchesPanel.add(matchPanel);
                    matchesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading upcoming matches: " + e.getMessage());
        }

        matchesPanel.revalidate();
        matchesPanel.repaint();
    }

    private void openScoreboard(Match match) {
        SwingUtilities.invokeLater(() -> {
            ScoreboardFrame scoreboardFrame = new ScoreboardFrame(match);
            scoreboardFrame.show();
        });
    }

    private void loadPlayedMatches() {
        matchesPanel.removeAll();

        JLabel titleLabel = new JLabel("Played Matches");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        matchesPanel.add(titleLabel);
        matchesPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        try {
            List<Match> playedMatches = matchDAO.getPlayedMatches();

            if (playedMatches.isEmpty()) {
                JLabel noMatchesLabel = new JLabel("No played matches found");
                noMatchesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                matchesPanel.add(noMatchesLabel);
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                for (Match match : playedMatches) {
                    String dateStr = dateFormat.format(match.getDate());
                    String teams = match.getHomeTeam() + " vs " + match.getAwayTeam();
                    String result = match.getHomeGoals() + " - " + match.getAwayGoals();

                    JPanel matchPanel = createPlayedMatchPanel(teams, dateStr, result);
                    matchesPanel.add(matchPanel);
                    matchesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading played matches: " + e.getMessage());
        }

        matchesPanel.revalidate();
        matchesPanel.repaint();
    }

    private JPanel createMatchPanel(String teams, String date, String time) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));

        JLabel teamsLabel = new JLabel(teams);
        teamsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        teamsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dateTimeLabel = new JLabel(date + " at " + time);
        dateTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(teamsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(dateTimeLabel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createPlayedMatchPanel(String teams, String date, String result) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));

        JLabel teamsLabel = new JLabel(teams);
        teamsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        teamsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dateLabel = new JLabel(date);
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel resultLabel = new JLabel("Result: " + result);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 16));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(teamsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(dateLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(resultLabel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    public void show() {
        frame.setVisible(true);
    }
}