package main.java.views;

import main.java.DAOs.TeamDAO;
import main.java.DatabaseConnection;
import main.java.models.Team;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class ManageTeamsFrame {
    private JFrame frame;
    private TeamDAO teamDAO;
    private JPanel teamsPanel;

    public ManageTeamsFrame() {
        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection connection = dbConnection.connect();
        teamDAO = new TeamDAO(connection);

        frame = new JFrame("Manage Teams");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // Create a scrollable panel for team buttons
        teamsPanel = new JPanel();
        teamsPanel.setLayout(new BoxLayout(teamsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(teamsPanel);

        // Add control buttons at the top
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createTeamButton = new JButton("Create New Team");
        createTeamButton.addActionListener(e -> createNewTeam());
        topPanel.add(createTeamButton);

        loadTeamsIntoPanel(teamsPanel);

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
    }

    private void loadTeamsIntoPanel(JPanel panel) {
        panel.removeAll(); // Clear existing buttons
        try {
            List<Team> teams = teamDAO.readAllTeams();
            for (Team team : teams) {
                // Create panel for each team with button and controls
                JPanel teamPanel = new JPanel(new BorderLayout());
                teamPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
                teamPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                // Team button for opening details
                JButton teamButton = new JButton(team.getName());
                teamButton.setFont(new Font("Arial", Font.BOLD, 18));
                teamButton.addActionListener(e -> openTeamDetailsFrame(team));

                // Controls panel with buttons
                JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

                // Edit button
                JButton editButton = new JButton("Edit name");
                editButton.setToolTipText("Change team name");
                editButton.addActionListener(e -> updateTeamName(team));

                // Delete button
                JButton deleteButton = new JButton("Delete");
                deleteButton.setToolTipText("Delete this team");
                deleteButton.addActionListener(e -> deleteTeam(team));

                controlsPanel.add(editButton);
                controlsPanel.add(deleteButton);

                teamPanel.add(teamButton, BorderLayout.CENTER);
                teamPanel.add(controlsPanel, BorderLayout.EAST);

                panel.add(teamPanel);
                panel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading teams from the database.");
        }
        panel.revalidate();
        panel.repaint();
    }

    private void createNewTeam() {
        String teamName = JOptionPane.showInputDialog(frame, "Enter new team name:");
        if (teamName != null && !teamName.trim().isEmpty()) {
            try {
                Team newTeam = new Team(0, teamName);
                teamDAO.createTeam(newTeam);
                loadTeamsIntoPanel(teamsPanel); // Refresh the list
                JOptionPane.showMessageDialog(frame, "Team created successfully!");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error creating team: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateTeamName(Team team) {
        String newName = JOptionPane.showInputDialog(frame, "Enter new name for " + team.getName() + ":", team.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            try {
                team.setName(newName);
                TeamDAO.updateTeam(team);
                loadTeamsIntoPanel(teamsPanel); // Refresh the list
                JOptionPane.showMessageDialog(frame, "Team name updated successfully!");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error updating team: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteTeam(Team team) {
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete " + team.getName() + "?\n" +
                        "This will also delete all players and coaches associated with this team.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                TeamDAO.deleteTeam(team.getId_team());
                loadTeamsIntoPanel(teamsPanel); // Refresh the list
                JOptionPane.showMessageDialog(frame, "Team deleted successfully!");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error deleting team: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openTeamDetailsFrame(Team team) {
        SwingUtilities.invokeLater(() -> {
            ManageTeamDetailsFrame detailsFrame = new ManageTeamDetailsFrame(team);
            detailsFrame.show();
        });
    }

    public void show() {
        frame.setVisible(true);
    }
}