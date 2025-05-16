package main.java.views;

import main.java.DAOs.MatchDAO;
import main.java.DAOs.PlayerDAO;
import main.java.DAOs.StatsDAO;
import main.java.DAOs.TeamDAO;
import main.java.DatabaseConnection;
import main.java.models.Match;
import main.java.models.Player;
import main.java.models.PlayerMatchStat;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardFrame {
    private JFrame frame;
    private Match match;
    private MatchDAO matchDAO;
    private PlayerDAO playerDAO;
    private StatsDAO statsDAO;
    private JSpinner homeGoalsSpinner;
    private JSpinner awayGoalsSpinner;
    private JButton saveButton;

    private DefaultTableModel homeTeamTableModel;
    private DefaultTableModel awayTeamTableModel;
    private JTable homeTeamTable;
    private JTable awayTeamTable;

    private Map<Integer, Integer> playerGoalsMap = new HashMap<>();
    private Map<Integer, Integer> playerAssistsMap = new HashMap<>();
    private List<Player> homePlayers;
    private List<Player> awayPlayers;

    private JLabel timerLabel;
    private Timer matchTimer;
    private int minutes = 0;
    private int seconds = 0;
    private boolean isTimerRunning = false;
    private JButton startPauseTimerButton;
    private JButton resetTimerButton;

    public ScoreboardFrame(Match match) {
        this.match = match;

        // Initialize database connection
        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection connection = dbConnection.connect();
        matchDAO = new MatchDAO(connection);
        playerDAO = new PlayerDAO(connection);
        statsDAO = new StatsDAO(connection);

        // Create the main frame
        frame = new JFrame("Match Scoreboard");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Create content panel with padding
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Match details
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

        JLabel dateLabel = new JLabel(dateFormat.format(match.getDate()));
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Teams and score panel
        JPanel scorePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Home team
        JLabel homeTeamLabel = new JLabel(match.getHomeTeam());
        homeTeamLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Away team
        JLabel awayTeamLabel = new JLabel(match.getAwayTeam());
        awayTeamLabel.setFont(new Font("Arial", Font.BOLD, 18));

        SpinnerNumberModel homeModel = new SpinnerNumberModel(
                match.getHomeGoals() != null ? match.getHomeGoals() : 0,
                0, 99, 1);
        homeGoalsSpinner = new JSpinner(homeModel);
        homeGoalsSpinner.setFont(new Font("Arial", Font.BOLD, 22));

        SpinnerNumberModel awayModel = new SpinnerNumberModel(
                match.getAwayGoals() != null ? match.getAwayGoals() : 0,
                0, 99, 1);
        awayGoalsSpinner = new JSpinner(awayModel);
        awayGoalsSpinner.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel vsLabel = new JLabel("vs");
        vsLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        // Add components to score panel
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridx = 0; gbc.gridy = 0;
        scorePanel.add(homeTeamLabel, gbc);

        gbc.gridx = 1;
        scorePanel.add(vsLabel, gbc);

        gbc.gridx = 2;
        scorePanel.add(awayTeamLabel, gbc);

        gbc.insets = new Insets(15, 10, 5, 10);
        gbc.gridx = 0; gbc.gridy = 1;
        scorePanel.add(homeGoalsSpinner, gbc);

        gbc.gridx = 1;
        scorePanel.add(new JLabel("-"), gbc);

        gbc.gridx = 2;
        scorePanel.add(awayGoalsSpinner, gbc);

        // Create timer panel - now added directly into the score panel
        JPanel timerPanel = new JPanel();
        timerPanel.setBorder(BorderFactory.createTitledBorder("Match Time"));
        timerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Timer label
        timerLabel = new JLabel("00:00");
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 24));

        // Timer buttons
        startPauseTimerButton = new JButton("Start");
        resetTimerButton = new JButton("Reset");

        // Add components to timer panel
        timerPanel.add(timerLabel);
        timerPanel.add(startPauseTimerButton);
        timerPanel.add(resetTimerButton);

        // Add timer panel directly to the score panel in a new row
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 3; // Span across all columns
        gbc.insets = new Insets(15, 5, 5, 5);
        scorePanel.add(timerPanel, gbc);

        // Initialize the timer (fires every second)
        matchTimer = new Timer(1000, e -> {
            seconds++;
            if (seconds == 60) {
                seconds = 0;
                minutes++;
            }
            updateTimerDisplay();
        });

        // Timer control buttons action listeners
        startPauseTimerButton.addActionListener(e -> {
            if (isTimerRunning) {
                matchTimer.stop();
                startPauseTimerButton.setText("Start");
            } else {
                matchTimer.start();
                startPauseTimerButton.setText("Pause");
            }
            isTimerRunning = !isTimerRunning;
        });

        resetTimerButton.addActionListener(e -> {
            matchTimer.stop();
            isTimerRunning = false;
            minutes = 0;
            seconds = 0;
            updateTimerDisplay();
            startPauseTimerButton.setText("Start");
        });

        // Create player statistics tables
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        // Home team players table
        JPanel homeTablePanel = new JPanel(new BorderLayout());
        homeTablePanel.add(new JLabel("Home Team Players"), BorderLayout.NORTH);
        homeTeamTableModel = createPlayerStatsTableModel();
        homeTeamTable = new JTable(homeTeamTableModel);
        homeTeamTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        homeTablePanel.add(new JScrollPane(homeTeamTable), BorderLayout.CENTER);

        // Away team players table
        JPanel awayTablePanel = new JPanel(new BorderLayout());
        awayTablePanel.add(new JLabel("Away Team Players"), BorderLayout.NORTH);
        awayTeamTableModel = createPlayerStatsTableModel();
        awayTeamTable = new JTable(awayTeamTableModel);
        awayTeamTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        awayTablePanel.add(new JScrollPane(awayTeamTable), BorderLayout.CENTER);

        tablesPanel.add(homeTablePanel);
        tablesPanel.add(awayTablePanel);

        // Load player data
        loadPlayerData();

        // Save button
        saveButton = new JButton("Update Score & Stats");
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.setMaximumSize(new Dimension(200, 40));
        saveButton.addActionListener(e -> saveScore());

        // Back button
        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setMaximumSize(new Dimension(200, 40));
        backButton.addActionListener(e -> frame.dispose());

        // Add components to content panel
        contentPanel.add(dateLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(scorePanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(tablesPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(saveButton);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(backButton);

        frame.add(contentPanel, BorderLayout.CENTER);
    }

    private void updateTimerDisplay() {
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private DefaultTableModel createPlayerStatsTableModel() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[][] {},
                new String[] {"Name", "#", "Goals", "Assists"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 3; // Only goals and assists columns are editable
            }
        };
        return model;
    }

    private void loadPlayerData() {
        try {
            int homeTeamId = TeamDAO.getTeamIdByName(match.getHomeTeam());
            int awayTeamId = TeamDAO.getTeamIdByName(match.getAwayTeam());

            // Load home team players
            homePlayers = playerDAO.getPlayersByTeam(homeTeamId);
            for (Player player : homePlayers) {
                homeTeamTableModel.addRow(new Object[] {
                        player.getName(),
                        player.getShirtNumber(),
                        0,  // Goals - editable
                        0   // Assists - editable
                });
                playerGoalsMap.put(player.getId_person(), 0);
                playerAssistsMap.put(player.getId_person(), 0);
            }

            // Load away team players
            awayPlayers = playerDAO.getPlayersByTeam(awayTeamId);
            for (Player player : awayPlayers) {
                awayTeamTableModel.addRow(new Object[] {
                        player.getName(),
                        player.getShirtNumber(),
                        0,  // Goals - editable
                        0   // Assists - editable
                });
                playerGoalsMap.put(player.getId_person(), 0);
                playerAssistsMap.put(player.getId_person(), 0);
            }

            // Load existing stats if any
            loadExistingStats();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading player data: " + e.getMessage());
        }
    }

    private void loadExistingStats() {
        try {
            // Get existing stats for this match using the new DAO method
            List<PlayerMatchStat> matchStats = statsDAO.getMatchPlayerStats(match.getId());

            for (PlayerMatchStat stat : matchStats) {
                int playerId = stat.getPlayerId();
                int goals = stat.getGoals();
                int assists = stat.getAssists();

                playerGoalsMap.put(playerId, goals);
                playerAssistsMap.put(playerId, assists);

                // Update table models
                updatePlayerStatInTable(homeTeamTableModel, playerId, goals, assists);
                updatePlayerStatInTable(awayTeamTableModel, playerId, goals, assists);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading player stats: " + e.getMessage());
        }
    }

    private void updatePlayerStatInTable(DefaultTableModel model, int playerId, int goals, int assists) {
        // Find player name from ID
        String playerName = "";
        for (Player player : homePlayers) {
            if (player.getId_person() == playerId) {
                playerName = player.getName();
                break;
            }
        }
        if (playerName.isEmpty()) {
            for (Player player : awayPlayers) {
                if (player.getId_person() == playerId) {
                    playerName = player.getName();
                    break;
                }
            }
        }

        // Update the row with the found player name
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(playerName)) {
                model.setValueAt(goals, i, 2);
                model.setValueAt(assists, i, 3);
                break;
            }
        }
    }

    private void saveScore() {
        try {
            int homeGoals = (Integer) homeGoalsSpinner.getValue();
            int awayGoals = (Integer) awayGoalsSpinner.getValue();

            // Validate that goals in the tables match the overall score
            int totalHomeGoals = getTotalGoalsFromTable(homeTeamTableModel);
            int totalAwayGoals = getTotalGoalsFromTable(awayTeamTableModel);

            if (totalHomeGoals != homeGoals || totalAwayGoals != awayGoals) {
                JOptionPane.showMessageDialog(frame,
                        "The sum of individual player goals must match the team scores.\n" +
                                "Home team: " + totalHomeGoals + " vs " + homeGoals + "\n" +
                                "Away team: " + totalAwayGoals + " vs " + awayGoals,
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update match score
            match.setHomeGoals(homeGoals);
            match.setAwayGoals(awayGoals);
            matchDAO.updateMatch(match);

            // Save player statistics
            savePlayerStats(homeTeamTable, match.getId());
            savePlayerStats(awayTeamTable, match.getId());

            JOptionPane.showMessageDialog(frame, "Score and player statistics updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error updating data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getTotalGoalsFromTable(DefaultTableModel model) {
        int totalGoals = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object value = model.getValueAt(i, 2);
            if (value instanceof Integer) {
                totalGoals += (Integer) value;
            } else {
                try {
                    totalGoals += Integer.parseInt(value.toString());
                } catch (NumberFormatException e) {
                    // Handle invalid input
                    JOptionPane.showMessageDialog(frame,
                            "Invalid goal value: " + value + ". Please enter a number.",
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                    return -1; // Signal error
                }
            }
        }
        return totalGoals;
    }

    private void savePlayerStats(JTable table, int matchId) throws SQLException {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        List<Player> playerList = (table == homeTeamTable) ? homePlayers : awayPlayers;

        for (int i = 0; i < model.getRowCount(); i++) {
            String playerName = (String) model.getValueAt(i, 0);

            Object goalsValue = model.getValueAt(i, 2);
            Object assistsValue = model.getValueAt(i, 3);

            int goals, assists;

            // Parse goals
            if (goalsValue instanceof Integer) {
                goals = (Integer) goalsValue;
            } else {
                goals = Integer.parseInt(goalsValue.toString());
            }

            // Parse assists
            if (assistsValue instanceof Integer) {
                assists = (Integer) assistsValue;
            } else {
                assists = Integer.parseInt(assistsValue.toString());
            }

            // Find player ID by name
            int playerId = -1;
            for (Player player : playerList) {
                if (player.getName().equals(playerName)) {
                    playerId = player.getId_person();
                    break;
                }
            }

            if (playerId != -1) {
                statsDAO.savePlayerMatchStats(playerId, matchId, goals, assists);
            }
        }
    }

    public void show() {
        frame.setVisible(true);
    }
}