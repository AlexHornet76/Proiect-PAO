package main.java.views;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import main.java.DAOs.TeamDAO;
import main.java.DatabaseConnection;
import main.java.models.Team;

public class MainFrame {
    private JFrame frame;
    private JButton manageTeamsButton;
    private JButton matchesButton;
    private JButton standingsButton;
    private JPanel mainPanel;
    private JPanel teamsPanel;
    private CardLayout cardLayout;
    private TeamDAO teamDAO;

    public MainFrame() {
        // Initialize database connection
        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection connection = dbConnection.connect();
        teamDAO = new TeamDAO(connection);

        // Initialize the frame
        frame = new JFrame("HN");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Create card layout for switching between panels
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create home panel with buttons
        JPanel homePanel = createHomePanel();
        mainPanel.add(homePanel, "HOME");

        // Create teams panel
        teamsPanel = new JPanel();
        teamsPanel.setLayout(new BorderLayout());

        // Add a back button at the top
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "HOME"));

        JPanel teamsContentPanel = new JPanel();
        teamsContentPanel.setLayout(new BoxLayout(teamsContentPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(teamsContentPanel);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);

        teamsPanel.add(topPanel, BorderLayout.NORTH);
        teamsPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(teamsPanel, "TEAMS");

        // Set up button actions
        manageTeamsButton.addActionListener(e -> {
            // Instead of loading teams into the panel, open the ManageTeamsFrame
            SwingUtilities.invokeLater(() -> {
                ManageTeamsFrame manageTeamsFrame = new ManageTeamsFrame();
                manageTeamsFrame.show();
            });
        });

        matchesButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                MatchesFrame matchesFrame = new MatchesFrame();
                matchesFrame.show();
            });
        });

        standingsButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                StandingsFrame standingsFrame = new StandingsFrame();
                standingsFrame.show();
            });
        });

        // Add main panel to frame
        frame.add(mainPanel);
    }

    private JPanel createHomePanel() {
        // Create buttons
        manageTeamsButton = new JButton("MANAGE TEAMS");
        matchesButton = new JButton("MATCHES");
        standingsButton = new JButton("STANDINGS");

        // Set font for buttons
        Font buttonFont = new Font("Courier New", Font.BOLD, 20);
        manageTeamsButton.setFont(buttonFont);
        matchesButton.setFont(buttonFont);
        standingsButton.setFont(buttonFont);

        // Set larger size for buttons
        Dimension buttonSize = new Dimension(300, 100);
        manageTeamsButton.setPreferredSize(buttonSize);
        matchesButton.setPreferredSize(buttonSize);
        standingsButton.setPreferredSize(buttonSize);

        // Set layout and center buttons
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(20, 0, 20, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        panel.add(manageTeamsButton, gbc);
        panel.add(matchesButton, gbc);
        panel.add(standingsButton, gbc);

        return panel;
    }

    private void loadTeamsIntoPanel(JPanel panel) {
        panel.removeAll(); // Clear existing buttons
        try {
            List<Team> teams = teamDAO.readAllTeams();
            for (Team team : teams) {
                JButton teamButton = new JButton(team.getName());
                teamButton.setFont(new Font("Arial", Font.BOLD, 18));
                teamButton.setPreferredSize(new Dimension(400, 50));
                teamButton.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
                teamButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                teamButton.addActionListener(e -> openTeamDetailsFrame(team));
                panel.add(teamButton);
                panel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading teams from the database.");
        }
        panel.revalidate();
        panel.repaint();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame app = new MainFrame();
            app.show();
        });
    }
}