package main.java.views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

import main.java.AuditService;
import main.java.DAOs.MatchDAO;
import main.java.DAOs.StatsDAO;
import main.java.DatabaseConnection;
import main.java.models.Match;
import main.java.models.Stats;

public class StandingsFrame {
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private MatchDAO matchDAO;
    private StatsDAO statsDAO;
    private JButton standingsButton;
    private JButton topScorersButton;
    private AuditService auditService;

    public StandingsFrame() {
        // Initialize audit service
        auditService = AuditService.getInstance();
        auditService.logAction("STANDINGS_FRAME_INITIALIZED");

        // Initialize database connection
        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection connection = dbConnection.connect();
        matchDAO = new MatchDAO(connection);
        statsDAO = new StatsDAO(connection);

        // Set up the frame
        frame = new JFrame("Standings");
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());

        // Create top panel with navigation buttons
        JPanel topPanel = new JPanel(new FlowLayout());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            auditService.logAction("CLOSE_STANDINGS_FRAME");
            frame.dispose();
        });

        standingsButton = new JButton("Team Standings");
        topScorersButton = new JButton("Top Scorers");

        standingsButton.addActionListener(e -> {
            auditService.logAction("VIEW_TEAM_STANDINGS");
            showStandings();
        });

        topScorersButton.addActionListener(e -> {
            auditService.logAction("VIEW_TOP_SCORERS");
            showTopScorers();
        });

        topPanel.add(backButton);
        topPanel.add(standingsButton);
        topPanel.add(topScorersButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Create content panel with card layout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Create panels for each view
        JPanel standingsPanel = createStandingsPanel();
        JPanel topScorersPanel = createTopScorersPanel();

        contentPanel.add(standingsPanel, "STANDINGS");
        contentPanel.add(topScorersPanel, "TOP_SCORERS");

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
    }

    private JPanel createStandingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table for standings
        String[] columns = {"Rank", "Team", "P", "W", "D", "L", "GF", "GA", "GD", "P"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTopScorersPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table for top scorers
        String[] columns = {"Rank", "Player", "Team", "Goals", "Assists"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void showStandings() {
        cardLayout.show(contentPanel, "STANDINGS");
        loadStandingsData();

        // Update button visibility
        standingsButton.setVisible(false);
        topScorersButton.setVisible(true);
    }

    private void showTopScorers() {
        cardLayout.show(contentPanel, "TOP_SCORERS");
        loadTopScorersData();

        // Update button visibility
        standingsButton.setVisible(true);
        topScorersButton.setVisible(false);
    }

    private void loadStandingsData() {
        auditService.logAction("LOAD_STANDINGS_DATA");
        try {
            List<Match> matches = matchDAO.getPlayedMatches();
            Map<String, TeamStats> teamStatsMap = calculateTeamStats(matches);

            // Sort teams by points (descending), then goal difference, then goals scored
            List<Map.Entry<String, TeamStats>> teamList = new ArrayList<>(teamStatsMap.entrySet());
            teamList.sort((t1, t2) -> {
                TeamStats stats1 = t1.getValue();
                TeamStats stats2 = t2.getValue();

                if (stats1.points != stats2.points) {
                    return stats2.points - stats1.points; // Descending by points
                } else if (stats1.goalDifference != stats2.goalDifference) {
                    return stats2.goalDifference - stats1.goalDifference; // By goal difference
                } else {
                    return stats2.goalsFor - stats1.goalsFor; // By goals scored
                }
            });

            // Get the table model from standings panel
            JPanel standingsPanel = (JPanel) contentPanel.getComponent(0);
            JScrollPane scrollPane = (JScrollPane) standingsPanel.getComponent(0);
            JTable table = (JTable) scrollPane.getViewport().getView();
            DefaultTableModel model = (DefaultTableModel) table.getModel();

            // Clear existing data
            model.setRowCount(0);

            // Add the sorted data to the table
            int rank = 1;
            for (Map.Entry<String, TeamStats> entry : teamList) {
                String teamName = entry.getKey();
                TeamStats stats = entry.getValue();

                model.addRow(new Object[] {
                        rank++,
                        teamName,
                        stats.played,
                        stats.won,
                        stats.drawn,
                        stats.lost,
                        stats.goalsFor,
                        stats.goalsAgainst,
                        stats.goalDifference,
                        stats.points
                });
            }
        } catch (SQLException e) {
            auditService.logAction("ERROR_LOADING_STANDINGS_DATA");
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading standings data: " + e.getMessage());
        }
    }

    private Map<String, TeamStats> calculateTeamStats(List<Match> matches) {
        Map<String, TeamStats> teamStats = new HashMap<>();

        for (Match match : matches) {
            String homeTeam = match.getHomeTeam();
            String awayTeam = match.getAwayTeam();
            int homeGoals = match.getHomeGoals();
            int awayGoals = match.getAwayGoals();

            // Get or create team stats objects
            TeamStats homeStats = teamStats.getOrDefault(homeTeam, new TeamStats());
            TeamStats awayStats = teamStats.getOrDefault(awayTeam, new TeamStats());

            // Update match counts
            homeStats.played++;
            awayStats.played++;

            // Update goals
            homeStats.goalsFor += homeGoals;
            homeStats.goalsAgainst += awayGoals;
            awayStats.goalsFor += awayGoals;
            awayStats.goalsAgainst += homeGoals;

            // Update results and points
            if (homeGoals > awayGoals) {
                homeStats.won++;
                homeStats.points += 3;
                awayStats.lost++;
            } else if (homeGoals < awayGoals) {
                homeStats.lost++;
                awayStats.won++;
                awayStats.points += 3;
            } else {
                homeStats.drawn++;
                homeStats.points += 1;
                awayStats.drawn++;
                awayStats.points += 1;
            }

            // Calculate goal differences
            homeStats.goalDifference = homeStats.goalsFor - homeStats.goalsAgainst;
            awayStats.goalDifference = awayStats.goalsFor - awayStats.goalsAgainst;

            // Update the map
            teamStats.put(homeTeam, homeStats);
            teamStats.put(awayTeam, awayStats);
        }

        return teamStats;
    }

    private void loadTopScorersData() {
        auditService.logAction("LOAD_TOP_SCORERS_DATA");
        try {
            List<Stats> topScorers = statsDAO.getTopScorers(10);

            // Get the table model from top scorers panel
            JPanel topScorersPanel = (JPanel) contentPanel.getComponent(1);
            JScrollPane scrollPane = (JScrollPane) topScorersPanel.getComponent(0);
            JTable table = (JTable) scrollPane.getViewport().getView();
            DefaultTableModel model = (DefaultTableModel) table.getModel();

            // Clear existing data
            model.setRowCount(0);

            // Add top scorers to the table
            int rank = 1;
            for (Stats stats : topScorers) {
                model.addRow(new Object[] {
                        rank++,
                        stats.getPlayerName(),
                        stats.getTeamName(),
                        stats.getGoals(),
                        stats.getAssists()
                });
            }
        } catch (SQLException e) {
            auditService.logAction("ERROR_LOADING_TOP_SCORERS_DATA");
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading top scorers data: " + e.getMessage());
        }
    }

    public void show() {
        auditService.logAction("STANDINGS_FRAME_SHOWN");
        // Show standings by default when the frame is opened and hide the standings button
        standingsButton.setVisible(false);
        topScorersButton.setVisible(true);
        showStandings();
        frame.setVisible(true);
    }

    // Helper class to track team statistics
    private static class TeamStats {
        int played = 0;
        int won = 0;
        int drawn = 0;
        int lost = 0;
        int goalsFor = 0;
        int goalsAgainst = 0;
        int goalDifference = 0;
        int points = 0;
    }
}