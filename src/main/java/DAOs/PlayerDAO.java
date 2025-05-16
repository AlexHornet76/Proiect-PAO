package main.java.DAOs;

import main.java.models.Player;
import main.java.models.Person;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {
    private final Connection connection;

    public PlayerDAO(Connection connection) {
        this.connection = connection;
    }

    public void createPlayer(Player player, int teamId) throws SQLException {
        // First insert into PERSON table
        String personQuery = "INSERT INTO PERSON (name, birthday, nationality, id_team) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(personQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, player.getName());
            stmt.setDate(2, new java.sql.Date(player.getBirthday().getTime()));
            stmt.setString(3, player.getNationality());
            stmt.setInt(4, teamId);
            stmt.executeUpdate();

            // Get generated person ID
            int personId;
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    personId = generatedKeys.getInt(1);
                    player.setId_person(personId);
                } else {
                    throw new SQLException("Creating person failed, no ID obtained.");
                }
            }

            // Then insert into PLAYER table
            String playerQuery = "INSERT INTO PLAYER (id_player, position, shirt_number) VALUES (?, ?, ?)";
            try (PreparedStatement playerStmt = connection.prepareStatement(playerQuery)) {
                playerStmt.setInt(1, personId);
                playerStmt.setString(2, player.getPosition());
                playerStmt.setInt(3, player.getShirtNumber());
                playerStmt.executeUpdate();
            }
        }
    }

    public List<Player> getPlayersByTeam(int teamId) throws SQLException {
        List<Player> players = new ArrayList<>();
        String query = "SELECT p.id_person, p.name, p.birthday, p.nationality, " +
                "pl.position, pl.shirt_number " +
                "FROM PERSON p JOIN PLAYER pl ON p.id_person = pl.id_player " +
                "WHERE p.id_team = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Player player = new Player(
                            rs.getInt("id_person"),
                            rs.getString("name"),
                            rs.getDate("birthday"),
                            rs.getString("nationality"),
                            rs.getString("position"),
                            rs.getInt("shirt_number")
                    );
                    players.add(player);
                }
            }
        }
        return players;
    }

    public void updatePlayer(Player player) throws SQLException {
        connection.setAutoCommit(false);
        try {
            // Update PERSON table
            String personQuery = "UPDATE PERSON SET name = ?, birthday = ?, nationality = ? WHERE id_person = ?";
            try (PreparedStatement stmt = connection.prepareStatement(personQuery)) {
                stmt.setString(1, player.getName());
                stmt.setDate(2, new java.sql.Date(player.getBirthday().getTime()));
                stmt.setString(3, player.getNationality());
                stmt.setInt(4, player.getId_person());
                stmt.executeUpdate();
            }

            // Update PLAYER table
            String playerQuery = "UPDATE PLAYER SET position = ?, shirt_number = ? WHERE id_player = ?";
            try (PreparedStatement stmt = connection.prepareStatement(playerQuery)) {
                stmt.setString(1, player.getPosition());
                stmt.setInt(2, player.getShirtNumber());
                stmt.setInt(3, player.getId_person());
                stmt.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void deletePlayer(int playerId) throws SQLException {
        connection.setAutoCommit(false);
        try {
            // First delete from PLAYER table
            String playerQuery = "DELETE FROM PLAYER WHERE id_player = ?";
            try (PreparedStatement stmt = connection.prepareStatement(playerQuery)) {
                stmt.setInt(1, playerId);
                stmt.executeUpdate();
            }

            // Then delete from PERSON table
            String personQuery = "DELETE FROM PERSON WHERE id_person = ?";
            try (PreparedStatement stmt = connection.prepareStatement(personQuery)) {
                stmt.setInt(1, playerId);
                stmt.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
