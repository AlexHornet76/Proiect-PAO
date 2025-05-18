package main.java.views;

import main.java.AuditService;
import main.java.DAOs.MatchDAO;
import main.java.DAOs.PlayerDAO;
import main.java.DAOs.StatsDAO;
import main.java.DAOs.TeamDAO;
import main.java.DatabaseConnection;
import main.java.models.GameAction;
import main.java.models.Match;
import main.java.models.Player;
import main.java.models.PlayerMatchStat;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private AuditService auditService;

    private DefaultTableModel homeTeamTableModel;
    private DefaultTableModel awayTeamTableModel;
    private JTable homeTeamTable;
    private JTable awayTeamTable;

    // Store a direct reference to the actions table model
    private DefaultTableModel actionsTableModel;

    private Map<Integer, Integer> playerGoalsMap = new HashMap<>();
    private Map<Integer, Integer> playerAssistsMap = new HashMap<>();
    private List<Player> homePlayers;
    private List<Player> awayPlayers;

    // Action recording
    private List<GameAction> actionsRecorded = new ArrayList<>();

    private JLabel timerLabel;
    private Timer matchTimer;
    private int minutes = 0;
    private int seconds = 0;
    private boolean isTimerRunning = false;
    private JButton startPauseTimerButton;
    private JButton resetTimerButton;

    public ScoreboardFrame(Match match) {
        this.match = match;

        // Initialize audit service
        auditService = AuditService.getInstance();
        auditService.logAction("SCOREBOARD_FRAME_INITIALIZED_" + match.getHomeTeam() + "_VS_" + match.getAwayTeam());

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

        // Add window listener to log when frame is closed
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                auditService.logAction("CLOSE_SCOREBOARD_FRAME_" + match.getHomeTeam() + "_VS_" + match.getAwayTeam());
            }
        });

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
                auditService.logAction("TIMER_PAUSED_" + formatCurrentTime());
            } else {
                matchTimer.start();
                startPauseTimerButton.setText("Pause");
                auditService.logAction("TIMER_STARTED_" + formatCurrentTime());
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
            auditService.logAction("TIMER_RESET");
        });

        // Initialize table models first
        homeTeamTableModel = createPlayerStatsTableModel();
        awayTeamTableModel = createPlayerStatsTableModel();

        // Load player data
        loadPlayerData();

        // Home team action panel
        JPanel homeActionPanel = createTeamActionPanel("Home Team");
        // Away team action panel
        JPanel awayActionPanel = createTeamActionPanel("Away Team");

        // Create player action recording section
        JPanel actionPanel = new JPanel();
        actionPanel.setBorder(BorderFactory.createTitledBorder("Record Player Actions"));
        actionPanel.setLayout(new GridLayout(1, 2, 10, 0));
        actionPanel.add(homeActionPanel);
        actionPanel.add(awayActionPanel);

        // Create player statistics tables
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        // Home team players table
        JPanel homeTablePanel = new JPanel(new BorderLayout());
        homeTablePanel.add(new JLabel("Home Team Players"), BorderLayout.NORTH);
        homeTeamTable = new JTable(homeTeamTableModel);
        homeTeamTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        homeTablePanel.add(new JScrollPane(homeTeamTable), BorderLayout.CENTER);

        // Away team players table
        JPanel awayTablePanel = new JPanel(new BorderLayout());
        awayTablePanel.add(new JLabel("Away Team Players"), BorderLayout.NORTH);
        awayTeamTable = new JTable(awayTeamTableModel);
        awayTeamTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        awayTablePanel.add(new JScrollPane(awayTeamTable), BorderLayout.CENTER);

        tablesPanel.add(homeTablePanel);
        tablesPanel.add(awayTablePanel);

        // Actions recorded table
        JPanel actionsTablePanel = new JPanel(new BorderLayout());
        actionsTablePanel.setBorder(BorderFactory.createTitledBorder("Recorded Actions"));

        // Create and store a reference to the actions table model
        actionsTableModel = new DefaultTableModel(
                new Object[][] {},
                new String[] {"Time", "Player", "Action Type"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable actionsTable = new JTable(actionsTableModel);
        JScrollPane actionsScrollPane = new JScrollPane(actionsTable);
        actionsScrollPane.setPreferredSize(new Dimension(0, 150));
        actionsTablePanel.add(actionsScrollPane, BorderLayout.CENTER);

        // Add remove action button
        JButton removeActionButton = new JButton("Remove Selected Action");
        removeActionButton.addActionListener(e -> {
            int selectedRow = actionsTable.getSelectedRow();
            if (selectedRow != -1) {
                String actionInfo = actionsTableModel.getValueAt(selectedRow, 1) + "_" +
                        actionsTableModel.getValueAt(selectedRow, 2) + "_" +
                        actionsTableModel.getValueAt(selectedRow, 0);
                actionsRecorded.remove(selectedRow);
                actionsTableModel.removeRow(selectedRow);
                updatePlayerStatsFromActions();
                auditService.logAction("ACTION_REMOVED_" + actionInfo);
            } else {
                auditService.logAction("NO_ACTION_SELECTED_FOR_REMOVAL");
            }
        });
        actionsTablePanel.add(removeActionButton, BorderLayout.SOUTH);

        // Load existing actions
        loadExistingActions(actionsTableModel);

        // Save button
        saveButton = new JButton("Update Score & Stats");
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.setMaximumSize(new Dimension(200, 40));
        saveButton.addActionListener(e -> saveScore());

        // Back button
        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setMaximumSize(new Dimension(200, 40));
        backButton.addActionListener(e -> {
            auditService.logAction("CLOSE_SCOREBOARD_FRAME_" + match.getHomeTeam() + "_VS_" + match.getAwayTeam());
            frame.dispose();
        });

        // Add components to content panel
        contentPanel.add(dateLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(scorePanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(actionPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(actionsTablePanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(tablesPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(saveButton);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(backButton);

        frame.add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }

    private String formatCurrentTime() {
        return String.format("%02d:%02d", minutes, seconds);
    }

    private JPanel createTeamActionPanel(String teamName) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(teamName));

        JPanel playerSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        playerSelectionPanel.add(new JLabel("Player:"));

        // Player dropdown
        JComboBox<PlayerItem> playerComboBox = new JComboBox<>();
        List<Player> playerList = teamName.contains("Home") ? homePlayers : awayPlayers;
        if (playerList != null) {
            for (Player player : playerList) {
                playerComboBox.addItem(new PlayerItem(player));
            }
        }
        playerSelectionPanel.add(playerComboBox);

        // Action type selection panel
        JPanel actionTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionTypePanel.add(new JLabel("Action:"));

        JComboBox<String> actionTypeComboBox = new JComboBox<>(new String[] {"GOAL", "ASSIST"});
        actionTypePanel.add(actionTypeComboBox);

        // Add action button
        JButton addActionButton = new JButton("Record Action");
        addActionButton.addActionListener(e -> {
            if (playerComboBox.getSelectedItem() != null) {
                PlayerItem selectedPlayer = (PlayerItem) playerComboBox.getSelectedItem();
                String actionType = (String) actionTypeComboBox.getSelectedItem();

                // Create and record the action
                GameAction action = new GameAction(
                        0,
                        selectedPlayer.getPlayer().getId_person(),
                        match.getId(),
                        actionType,
                        minutes,
                        seconds
                );

                // Add to our list of recorded actions
                actionsRecorded.add(action);

                // Add to the display table - using our stored reference
                actionsTableModel.addRow(new Object[] {
                        formatCurrentTime(),
                        selectedPlayer.toString(),
                        actionType
                });

                // Update the stats tables
                updatePlayerStatsFromActions();

                auditService.logAction(actionType + "_RECORDED_" +
                        selectedPlayer.getPlayer().getName() + "_" +
                        formatCurrentTime());

                JOptionPane.showMessageDialog(frame,
                        "Action recorded: " + selectedPlayer.toString() + " - " + actionType +
                                " at " + formatCurrentTime());
            }
        });

        panel.add(playerSelectionPanel);
        panel.add(actionTypePanel);
        panel.add(addActionButton);

        return panel;
    }

    private void updatePlayerStatsFromActions() {
        // Reset all player stats
        for (Player player : homePlayers) {
            playerGoalsMap.put(player.getId_person(), 0);
            playerAssistsMap.put(player.getId_person(), 0);
        }
        for (Player player : awayPlayers) {
            playerGoalsMap.put(player.getId_person(), 0);
            playerAssistsMap.put(player.getId_person(), 0);
        }

        // Count goals and assists from recorded actions
        for (GameAction action : actionsRecorded) {
            int playerId = action.getPlayerId();

            if (action.getActionType().equals("GOAL")) {
                playerGoalsMap.put(playerId, playerGoalsMap.getOrDefault(playerId, 0) + 1);
            } else if (action.getActionType().equals("ASSIST")) {
                playerAssistsMap.put(playerId, playerAssistsMap.getOrDefault(playerId, 0) + 1);
            }
        }

        // Update tables
        updateTableFromMaps(homeTeamTableModel, homePlayers);
        updateTableFromMaps(awayTeamTableModel, awayPlayers);

        // Update score spinners
        int homeGoals = 0;
        int awayGoals = 0;

        for (Player player : homePlayers) {
            homeGoals += playerGoalsMap.getOrDefault(player.getId_person(), 0);
        }

        for (Player player : awayPlayers) {
            awayGoals += playerGoalsMap.getOrDefault(player.getId_person(), 0);
        }

        homeGoalsSpinner.setValue(homeGoals);
        awayGoalsSpinner.setValue(awayGoals);

        auditService.logAction("PLAYER_STATS_UPDATED");
    }

    private void updateTableFromMaps(DefaultTableModel model, List<Player> players) {
        for (int i = 0; i < model.getRowCount(); i++) {
            String playerName = (String) model.getValueAt(i, 0);

            // Find player ID by name
            for (Player player : players) {
                if (player.getName().equals(playerName)) {
                    int playerId = player.getId_person();
                    int goals = playerGoalsMap.getOrDefault(playerId, 0);
                    int assists = playerAssistsMap.getOrDefault(playerId, 0);

                    model.setValueAt(goals, i, 2);
                    model.setValueAt(assists, i, 3);
                    break;
                }
            }
        }
    }

    private void loadExistingActions(DefaultTableModel actionsModel) {
        auditService.logAction("LOADING_EXISTING_ACTIONS");
        try {
            List<GameAction> actions = statsDAO.getMatchActions(match.getId());
            actionsRecorded.addAll(actions);

            // Populate the actions table
            for (GameAction action : actions) {
                // Find player name
                String playerName = "Unknown Player";
                for (Player player : homePlayers) {
                    if (player.getId_person() == action.getPlayerId()) {
                        playerName = player.getName() + " (#" + player.getShirtNumber() + ")";
                        break;
                    }
                }
                if (playerName.equals("Unknown Player")) {
                    for (Player player : awayPlayers) {
                        if (player.getId_person() == action.getPlayerId()) {
                            playerName = player.getName() + " (#" + player.getShirtNumber() + ")";
                            break;
                        }
                    }
                }

                actionsModel.addRow(new Object[] {
                        String.format("%02d:%02d", action.getMinute(), action.getSeconds()),
                        playerName,
                        action.getActionType()
                });
            }

            // Update player stats from actions
            updatePlayerStatsFromActions();

            // Set timer to the latest action time if available
            if (!actions.isEmpty()) {
                // Find the latest time
                int maxMinutes = 0;
                int maxSeconds = 0;

                for (GameAction action : actions) {
                    if (action.getMinute() > maxMinutes ||
                            (action.getMinute() == maxMinutes && action.getSeconds() > maxSeconds)) {
                        maxMinutes = action.getMinute();
                        maxSeconds = action.getSeconds();
                    }
                }

                // Set timer
                minutes = maxMinutes;
                seconds = maxSeconds;
                updateTimerDisplay();
            }

            auditService.logAction("EXISTING_ACTIONS_LOADED_" + actions.size());

        } catch (SQLException e) {
            auditService.logAction("ERROR_LOADING_ACTIONS");
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading match actions: " + e.getMessage());
        }
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
                return false; // Make table read-only as we now update through actions
            }
        };
        return model;
    }

    private void loadPlayerData() {
        auditService.logAction("LOADING_PLAYER_DATA");
        try {
            int homeTeamId = TeamDAO.getTeamIdByName(match.getHomeTeam());
            int awayTeamId = TeamDAO.getTeamIdByName(match.getAwayTeam());

            // Load home team players
            homePlayers = playerDAO.getPlayersByTeam(homeTeamId);
            for (Player player : homePlayers) {
                homeTeamTableModel.addRow(new Object[] {
                        player.getName(),
                        player.getShirtNumber(),
                        0,
                        0
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
                        0,
                        0
                });
                playerGoalsMap.put(player.getId_person(), 0);
                playerAssistsMap.put(player.getId_person(), 0);
            }

            auditService.logAction("PLAYER_DATA_LOADED_HOME_" + homePlayers.size() + "_AWAY_" + awayPlayers.size());

        } catch (SQLException e) {
            auditService.logAction("ERROR_LOADING_PLAYER_DATA");
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading player data: " + e.getMessage());
        }
    }

    private void saveScore() {
        auditService.logAction("SAVING_MATCH_SCORE_AND_STATS");
        try {
            int homeGoals = (Integer) homeGoalsSpinner.getValue();
            int awayGoals = (Integer) awayGoalsSpinner.getValue();

            // Validate that goals in the tables match the overall score
            int totalHomeGoals = getTotalGoalsFromTable(homeTeamTableModel);
            int totalAwayGoals = getTotalGoalsFromTable(awayTeamTableModel);

            if (totalHomeGoals != homeGoals || totalAwayGoals != awayGoals) {
                auditService.logAction("SCORE_VALIDATION_ERROR");
                JOptionPane.showMessageDialog(frame,
                        "The total goals in the tables don't match the final score.\n" +
                                "Home team: " + totalHomeGoals + " vs " + homeGoals + "\n" +
                                "Away team: " + totalAwayGoals + " vs " + awayGoals,
                        "Score Mismatch", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Update match score
            match.setHomeGoals(homeGoals);
            match.setAwayGoals(awayGoals);
            matchDAO.updateMatch(match);

            // Delete old actions and stats
            statsDAO.deleteMatchActions(match.getId());

            // Save actions first
            for (GameAction action : actionsRecorded) {
                statsDAO.saveGameAction(
                        action.getPlayerId(),
                        match.getId(),
                        action.getActionType(),
                        action.getMinute(),
                        action.getSeconds()
                );
            }

            // Then save aggregated stats
            for (Map.Entry<Integer, Integer> entry : playerGoalsMap.entrySet()) {
                int playerId = entry.getKey();
                int goals = entry.getValue();
                int assists = playerAssistsMap.getOrDefault(playerId, 0);

                statsDAO.savePlayerMatchStats(playerId, match.getId(), goals, assists);
            }

            auditService.logAction("MATCH_SCORE_SAVED_" + homeGoals + "-" + awayGoals +
                    "_ACTIONS_" + actionsRecorded.size());
            JOptionPane.showMessageDialog(frame, "Score, player statistics, and actions updated successfully!");
        } catch (Exception e) {
            auditService.logAction("ERROR_SAVING_SCORE_AND_STATS");
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
                    auditService.logAction("INVALID_GOAL_VALUE_" + value);
                    JOptionPane.showMessageDialog(frame,
                            "Invalid goal value: " + value + ". Please enter a number.",
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                    return -1; // Signal error
                }
            }
        }
        return totalGoals;
    }

    public void show() {
        auditService.logAction("SCOREBOARD_FRAME_SHOWN_" + match.getHomeTeam() + "_VS_" + match.getAwayTeam());
        frame.setVisible(true);
    }

    // Helper class for player dropdown
    private class PlayerItem {
        private Player player;

        public PlayerItem(Player player) {
            this.player = player;
        }

        public Player getPlayer() {
            return player;
        }

        @Override
        public String toString() {
            return player.getName() + " (#" + player.getShirtNumber() + ")";
        }
    }
}