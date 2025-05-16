package main.java.DAOs;

import main.java.models.Team;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamDAO {
    private static Connection connection;

    public TeamDAO(Connection connection) {
        this.connection = connection;
    }

    public void createTeam(Team team) throws SQLException {
        String query = "INSERT INTO TEAM (name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, team.getName());
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    team.setId_team(generatedKeys.getInt(1));
                }
            }
        }
    }

    public Team readTeam(int id_team) throws SQLException {
        String query = "SELECT * FROM TEAM WHERE id_team = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id_team);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Team(rs.getInt("id_team"), rs.getString("name"));
                }
            }
        }
        return null;
    }

    public List<Team> readAllTeams() throws SQLException {
        String query = "SELECT * FROM TEAM";
        List<Team> teams = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                teams.add(new Team(rs.getInt("id_team"), rs.getString("name")));
            }
        }
        return teams;
    }

    public static void updateTeam(Team team) throws SQLException {
        String query = "UPDATE TEAM SET name = ? WHERE id_team = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, team.getName());
            stmt.setInt(2, team.getId_team());
            stmt.executeUpdate();
        }
    }

    public static void deleteTeam(int id_team) throws SQLException {
        String query = "DELETE FROM TEAM WHERE id_team = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id_team);
            stmt.executeUpdate();
        }
    }

    public static int getTeamIdByName(String teamName) throws SQLException {
        String query = "SELECT id_team FROM TEAM WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, teamName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_team");
                }
            }
        }
        return -1; // Return -1 if team not found
    }
}
