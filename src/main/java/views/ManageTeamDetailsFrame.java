package main.java.views;

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
import java.sql.DriverManager;
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

    // Constants for card layout
    private static final String PLAYERS_PANEL = "PLAYERS";
    private static final String COACHES_PANEL = "COACHES";

    public ManageTeamDetailsFrame(Team team) {
        this.team = team;

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
                cardLayout.show(contentPanel, COACHES_PANEL);
                switchButton.setText("Switch to Players");
                showingPlayers = false;
            } else {
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
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading players: " + e.getMessage());
        }
    }

    private void loadCoachesData(DefaultTableModel model, JTable coachTable) {
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
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading coaches: " + e.getMessage());
        }
    }

    private void addPlayer() {
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

                loadPlayersData();
            } catch (ParseException | SQLException | NumberFormatException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error adding player: " + e.getMessage());
            }
        }
    }

    private void updatePlayer() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
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

                loadPlayersData();
            } catch (ParseException | SQLException | NumberFormatException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error updating player: " + e.getMessage());
            }
        }
    }

    private void deletePlayer() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a player to delete");
            return;
        }

        // Get player ID from the hidden first column
        int playerId = (int) tableModel.getValueAt(selectedRow, 0);
        String playerName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete player " + playerName + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                playerDAO.deletePlayer(playerId);
                loadPlayersData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error deleting player: " + e.getMessage());
            }
        }
    }

    private void addCoach() {
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

                // Refresh coach data in the table model
                if (!showingPlayers) {
                    DefaultTableModel coachModel = (DefaultTableModel) ((JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(1)).getComponent(0)).getViewport().getView()).getModel();
                    JTable coachTable = (JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(1)).getComponent(0)).getViewport().getView();
                    loadCoachesData(coachModel, coachTable);
                }
            } catch (ParseException | SQLException | NumberFormatException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error adding coach: " + e.getMessage());
            }
        }
    }

    private void updateCoach(JTable coachTable, DefaultTableModel coachTableModel) {
        int selectedRow = coachTable.getSelectedRow();
        if (selectedRow == -1) {
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

                loadCoachesData(coachTableModel, coachTable);
            } catch (ParseException | SQLException | NumberFormatException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error updating coach: " + e.getMessage());
            }
        }
    }

    private void deleteCoach(JTable coachTable, DefaultTableModel coachTableModel) {
        int selectedRow = coachTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a coach to delete");
            return;
        }

        // Get coach ID from the hidden first column
        int coachId = (int) coachTableModel.getValueAt(selectedRow, 0);
        String coachName = (String) coachTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete coach " + coachName + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                coachDAO.deleteCoach(coachId);
                loadCoachesData(coachTableModel, coachTable);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error deleting coach: " + e.getMessage());
            }
        }
    }

    public void show() {
        frame.setVisible(true);
    }
}