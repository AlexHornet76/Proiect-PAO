package main.java.views;

import main.java.AuditService;
import main.java.DAOs.CoachDAO;
import main.java.DAOs.PlayerDAO;
import main.java.DatabaseConnection;
import main.java.models.Coach;
import main.java.models.Player;
import main.java.models.Team;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ManageTeamDetailsFrame {
    private JFrame frame;
    private Team team;
    private PlayerDAO playerDAO;
    private CoachDAO coachDAO;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private boolean showingPlayers = true;
    private AuditService auditService;

    // Constants for card layout
    private static final String PLAYERS_PANEL = "PLAYERS";
    private static final String COACHES_PANEL = "COACHES";

    public ManageTeamDetailsFrame(Team team) {
        this.team = team;

        // Initialize audit service
        auditService = AuditService.getInstance();
        auditService.logAction("TEAM_DETAILS_FRAME_INITIALIZED_" + team.getName());

        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection connection = dbConnection.connect();
        playerDAO = new PlayerDAO(connection);
        coachDAO = new CoachDAO(connection);

        frame = new JFrame("Manage Team: " + team.getName());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        // Create card layout for switching between players and coaches
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);

        // Setup panels
        contentPanel.add(createPlayersPanel(), PLAYERS_PANEL);
        contentPanel.add(createCoachesPanel(), COACHES_PANEL);

        // Create toggle button panel
        JPanel togglePanel = new JPanel();
        JButton switchButton = new JButton("Switch to Coaches");
        switchButton.addActionListener(e -> {
            if (showingPlayers) {
                auditService.logAction("VIEW_COACHES_" + team.getName());
                cardLayout.show(contentPanel, COACHES_PANEL);
                switchButton.setText("Switch to Players");
                showingPlayers = false;
            } else {
                auditService.logAction("VIEW_PLAYERS_" + team.getName());
                cardLayout.show(contentPanel, PLAYERS_PANEL);
                switchButton.setText("Switch to Coaches");
                showingPlayers = true;
            }
        });

        togglePanel.add(switchButton);

        frame.setLayout(new BorderLayout());
        frame.add(togglePanel, BorderLayout.NORTH);
        frame.add(contentPanel, BorderLayout.CENTER);

        // Initially show players panel
        cardLayout.show(contentPanel, PLAYERS_PANEL);

        // Add window listener to log when frame is closed
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                auditService.logAction("CLOSE_TEAM_DETAILS_FRAME_" + team.getName());
            }
        });
    }

    private JPanel createPlayersPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table for players - add ID column which will be hidden
        String[] columns = {"ID", "Name", "Birthday", "Nationality", "Position", "Shirt Number"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        dataTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(dataTable);

        // Load players data
        loadPlayersData();

        // CRUD buttons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Player");
        JButton updateButton = new JButton("Update Player");
        JButton deleteButton = new JButton("Delete Player");

        addButton.addActionListener(e -> addPlayer());
        updateButton.addActionListener(e -> updatePlayer());
        deleteButton.addActionListener(e -> deletePlayer());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCoachesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table for coaches - add ID column which will be hidden
        String[] columns = {"ID", "Name", "Birthday", "Nationality", "Type", "Experience"};
        DefaultTableModel coachTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        JTable coachTable = new JTable(coachTableModel);
        JScrollPane scrollPane = new JScrollPane(coachTable);

        // Load coaches data
        loadCoachesData(coachTableModel, coachTable);

        // CRUD buttons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Coach");
        JButton updateButton = new JButton("Update Coach");
        JButton deleteButton = new JButton("Delete Coach");

        addButton.addActionListener(e -> addCoach());
        updateButton.addActionListener(e -> updateCoach(coachTable, coachTableModel));
        deleteButton.addActionListener(e -> deleteCoach(coachTable, coachTableModel));

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadPlayersData() {
        auditService.logAction("LOAD_PLAYERS_DATA_" + team.getName());
        tableModel.setRowCount(0);
        try {
            List<Player> players = playerDAO.getPlayersByTeam(team.getId_team());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (Player player : players) {
                Object[] rowData = {
                        player.getId_person(),
                        player.getName(),
                        dateFormat.format(player.getBirthday()),
                        player.getNationality(),
                        player.getPosition(),
                        player.getShirtNumber()
                };
                tableModel.addRow(rowData);
            }

            // Hide the ID column from view but keep it in the model
            dataTable.getColumnModel().getColumn(0).setMinWidth(0);
            dataTable.getColumnModel().getColumn(0).setMaxWidth(0);
            dataTable.getColumnModel().getColumn(0).setWidth(0);
        } catch (SQLException e) {
            auditService.logAction("ERROR_LOADING_PLAYERS_" + team.getName());
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading players: " + e.getMessage());
        }
    }

    private void loadCoachesData(DefaultTableModel model, JTable coachTable) {
        auditService.logAction("LOAD_COACHES_DATA_" + team.getName());
        model.setRowCount(0);
        try {
            List<Coach> coaches = coachDAO.getCoachesByTeam(team.getId_team());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (Coach coach : coaches) {
                Object[] rowData = {
                        coach.getId_person(),
                        coach.getName(),
                        dateFormat.format(coach.getBirthday()),
                        coach.getNationality(),
                        coach.getType(),
                        coach.getExperience()
                };
                model.addRow(rowData);
            }

            // Hide the ID column from view but keep it in the model
            coachTable.getColumnModel().getColumn(0).setMinWidth(0);
            coachTable.getColumnModel().getColumn(0).setMaxWidth(0);
            coachTable.getColumnModel().getColumn(0).setWidth(0);
        } catch (SQLException e) {
            auditService.logAction("ERROR_LOADING_COACHES_" + team.getName());
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading coaches: " + e.getMessage());
        }
    }

    private void addPlayer() {
        auditService.logAction("OPEN_ADD_PLAYER_DIALOG_" + team.getName());
        JTextField nameField = new JTextField();
        JTextField birthdayField = new JTextField("yyyy-MM-dd");
        JTextField nationalityField = new JTextField();
        JTextField positionField = new JTextField();
        JTextField shirtNumberField = new JTextField();

        Object[] fields = {
                "Name:", nameField,
                "Birthday (yyyy-MM-dd):", birthdayField,
                "Nationality:", nationalityField,
                "Position:", positionField,
                "Shirt Number:", shirtNumberField
        };

        int result = JOptionPane.showConfirmDialog(frame, fields, "Add Player", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date birthday = dateFormat.parse(birthdayField.getText());
                String nationality = nationalityField.getText();
                String position = positionField.getText();
                int shirtNumber = Integer.parseInt(shirtNumberField.getText());

                Player player = new Player(0, name, birthday, nationality, position, shirtNumber);
                playerDAO.createPlayer(player, team.getId_team());

                auditService.logAction("PLAYER_ADDED_" + name + "_TO_" + team.getName());
                loadPlayersData();
            } catch (ParseException | SQLException | NumberFormatException e) {
                auditService.logAction("ERROR_ADDING_PLAYER");
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error adding player: " + e.getMessage());
            }
        } else {
            auditService.logAction("CANCELLED_ADD_PLAYER");
        }
    }

    private void updatePlayer() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            auditService.logAction("NO_PLAYER_SELECTED_FOR_UPDATE");
            JOptionPane.showMessageDialog(frame, "Please select a player to update");
            return;
        }

        // Get data from the selected row
        int playerId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        String birthdayStr = (String) tableModel.getValueAt(selectedRow, 2);
        String nationality = (String) tableModel.getValueAt(selectedRow, 3);
        String position = (String) tableModel.getValueAt(selectedRow, 4);
        int shirtNumber = (int) tableModel.getValueAt(selectedRow, 5);

        auditService.logAction("OPEN_UPDATE_PLAYER_DIALOG_" + name);

        // Create input fields for updating
        JTextField nameField = new JTextField(name);
        JTextField birthdayField = new JTextField(birthdayStr);
        JTextField nationalityField = new JTextField(nationality);
        JTextField positionField = new JTextField(position);
        JTextField shirtNumberField = new JTextField(String.valueOf(shirtNumber));

        Object[] fields = {
                "Name:", nameField,
                "Birthday (yyyy-MM-dd):", birthdayField,
                "Nationality:", nationalityField,
                "Position:", positionField,
                "Shirt Number:", shirtNumberField
        };

        int result = JOptionPane.showConfirmDialog(frame, fields, "Update Player", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date birthday = dateFormat.parse(birthdayField.getText());
                int newShirtNumber = Integer.parseInt(shirtNumberField.getText());

                Player player = new Player(
                        playerId,
                        nameField.getText(),
                        birthday,
                        nationalityField.getText(),
                        positionField.getText(),
                        newShirtNumber
                );
                playerDAO.updatePlayer(player);

                auditService.logAction("PLAYER_UPDATED_" + name);
                loadPlayersData();
            } catch (ParseException | SQLException | NumberFormatException e) {
                auditService.logAction("ERROR_UPDATING_PLAYER");
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error updating player: " + e.getMessage());
            }
        } else {
            auditService.logAction("CANCELLED_UPDATE_PLAYER");
        }
    }

    private void deletePlayer() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            auditService.logAction("NO_PLAYER_SELECTED_FOR_DELETE");
            JOptionPane.showMessageDialog(frame, "Please select a player to delete");
            return;
        }

        // Get player ID from the hidden first column
        int playerId = (int) tableModel.getValueAt(selectedRow, 0);
        String playerName = (String) tableModel.getValueAt(selectedRow, 1);

        auditService.logAction("OPEN_DELETE_PLAYER_DIALOG_" + playerName);

        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete player " + playerName + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                playerDAO.deletePlayer(playerId);
                auditService.logAction("PLAYER_DELETED_" + playerName);
                loadPlayersData();
            } catch (SQLException e) {
                auditService.logAction("ERROR_DELETING_PLAYER");
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error deleting player: " + e.getMessage());
            }
        } else {
            auditService.logAction("CANCELLED_DELETE_PLAYER");
        }
    }

    private void addCoach() {
        auditService.logAction("OPEN_ADD_COACH_DIALOG_" + team.getName());
        JTextField nameField = new JTextField();
        JTextField birthdayField = new JTextField("yyyy-MM-dd");
        JTextField nationalityField = new JTextField();
        JTextField typeField = new JTextField();
        JTextField experienceField = new JTextField();

        Object[] fields = {
                "Name:", nameField,
                "Birthday (yyyy-MM-dd):", birthdayField,
                "Nationality:", nationalityField,
                "Type:", typeField,
                "Experience (years):", experienceField
        };

        int result = JOptionPane.showConfirmDialog(frame, fields, "Add Coach", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date birthday = dateFormat.parse(birthdayField.getText());
                String nationality = nationalityField.getText();
                String type = typeField.getText();
                int experience = Integer.parseInt(experienceField.getText());

                Coach coach = new Coach(0, name, birthday, nationality, type, experience);
                coachDAO.createCoach(coach, team.getId_team());

                auditService.logAction("COACH_ADDED_" + name + "_TO_" + team.getName());

                // Refresh coach data in the table model
                if (!showingPlayers) {
                    DefaultTableModel coachModel = (DefaultTableModel) ((JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(1)).getComponent(0)).getViewport().getView()).getModel();
                    JTable coachTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(1)).getComponent(0)).getViewport().getView();
                    loadCoachesData(coachModel, coachTable);
                }
            } catch (ParseException | SQLException | NumberFormatException e) {
                auditService.logAction("ERROR_ADDING_COACH");
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error adding coach: " + e.getMessage());
            }
        } else {
            auditService.logAction("CANCELLED_ADD_COACH");
        }
    }

    private void updateCoach(JTable coachTable, DefaultTableModel coachTableModel) {
        int selectedRow = coachTable.getSelectedRow();
        if (selectedRow == -1) {
            auditService.logAction("NO_COACH_SELECTED_FOR_UPDATE");
            JOptionPane.showMessageDialog(frame, "Please select a coach to update");
            return;
        }

        // Get data from the selected row
        int coachId = (int) coachTableModel.getValueAt(selectedRow, 0);
        String name = (String) coachTableModel.getValueAt(selectedRow, 1);
        String birthdayStr = (String) coachTableModel.getValueAt(selectedRow, 2);
        String nationality = (String) coachTableModel.getValueAt(selectedRow, 3);
        String type = (String) coachTableModel.getValueAt(selectedRow, 4);
        int experience = (int) coachTableModel.getValueAt(selectedRow, 5);

        auditService.logAction("OPEN_UPDATE_COACH_DIALOG_" + name);

        // Create input fields for updating
        JTextField nameField = new JTextField(name);
        JTextField birthdayField = new JTextField(birthdayStr);
        JTextField nationalityField = new JTextField(nationality);
        JTextField typeField = new JTextField(type);
        JTextField experienceField = new JTextField(String.valueOf(experience));

        Object[] fields = {
                "Name:", nameField,
                "Birthday (yyyy-MM-dd):", birthdayField,
                "Nationality:", nationalityField,
                "Type:", typeField,
                "Experience (years):", experienceField
        };

        int result = JOptionPane.showConfirmDialog(frame, fields, "Update Coach", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date birthday = dateFormat.parse(birthdayField.getText());
                int newExperience = Integer.parseInt(experienceField.getText());

                Coach coach = new Coach(
                        coachId,
                        nameField.getText(),
                        birthday,
                        nationalityField.getText(),
                        typeField.getText(),
                        newExperience
                );
                coachDAO.updateCoach(coach);

                auditService.logAction("COACH_UPDATED_" + name);
                loadCoachesData(coachTableModel, coachTable);
            } catch (ParseException | SQLException | NumberFormatException e) {
                auditService.logAction("ERROR_UPDATING_COACH");
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error updating coach: " + e.getMessage());
            }
        } else {
            auditService.logAction("CANCELLED_UPDATE_COACH");
        }
    }

    private void deleteCoach(JTable coachTable, DefaultTableModel coachTableModel) {
        int selectedRow = coachTable.getSelectedRow();
        if (selectedRow == -1) {
            auditService.logAction("NO_COACH_SELECTED_FOR_DELETE");
            JOptionPane.showMessageDialog(frame, "Please select a coach to delete");
            return;
        }

        // Get coach ID from the hidden first column
        int coachId = (int) coachTableModel.getValueAt(selectedRow, 0);
        String coachName = (String) coachTableModel.getValueAt(selectedRow, 1);

        auditService.logAction("OPEN_DELETE_COACH_DIALOG_" + coachName);

        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete coach " + coachName + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                coachDAO.deleteCoach(coachId);
                auditService.logAction("COACH_DELETED_" + coachName);
                loadCoachesData(coachTableModel, coachTable);
            } catch (SQLException e) {
                auditService.logAction("ERROR_DELETING_COACH");
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error deleting coach: " + e.getMessage());
            }
        } else {
            auditService.logAction("CANCELLED_DELETE_COACH");
        }
    }

    public void show() {
        auditService.logAction("TEAM_DETAILS_FRAME_SHOWN_" + team.getName());
        frame.setVisible(true);
    }
}