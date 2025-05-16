package main.java.DAOs;

import main.java.models.Stats;
import main.java.models.PlayerMatchStat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatsDAO {
    private final Connection connection;

    public StatsDAO(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public List<Stats> getTopScorers(int limit) throws SQLException {
        List<Stats> topScorers = new ArrayList<>();

        String query = "SELECT p.id_player, pe.name AS player_name, t.name AS team_name, " +
                "SUM(s.goals) AS total_goals, SUM(s.assists) AS total_assists " +
                "FROM STATS s " +
                "JOIN PLAYER p ON s.id_player = p.id_player " +
                "JOIN PERSON pe ON p.id_player = pe.id_person " +
                "JOIN TEAM t ON pe.id_team = t.id_team " +
                "GROUP BY p.id_player, pe.name, t.name " +
                "ORDER BY total_goals DESC, total_assists DESC " +
                "LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Stats stats = new Stats(
                            rs.getInt("id_player"),
                            rs.getString("player_name"),
                            rs.getString("team_name"),
                            rs.getInt("total_goals"),
                            rs.getInt("total_assists")
                    );
                    topScorers.add(stats);
                }
            }
        }

        return topScorers;
    }

    public List<Stats> getPlayerStats(int playerId) throws SQLException {
        List<Stats> playerStats = new ArrayList<>();

        String query = "SELECT m.id_match, " +
                "t_home.name AS home_team, t_away.name AS away_team, " +
                "m.goals_home, m.goals_away, s.goals, s.assists " +
                "FROM STATS s " +
                "JOIN MATCHES m ON s.id_match = m.id_match " +
                "JOIN TEAM t_home ON m.id_home = t_home.id_team " +
                "JOIN TEAM t_away ON m.id_away = t_away.id_team " +
                "WHERE s.id_player = ? " +
                "ORDER BY m.play_date DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, playerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Process each match statistic for this player
                    // This could be expanded if needed
                }
            }
        }

        return playerStats;
    }

    public void savePlayerMatchStats(int playerId, int matchId, int goals, int assists) throws SQLException {
        String query = "INSERT INTO STATS (id_player, id_match, goals, assists) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE goals = ?, assists = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, matchId);
            stmt.setInt(3, goals);
            stmt.setInt(4, assists);
            stmt.setInt(5, goals);
            stmt.setInt(6, assists);

            stmt.executeUpdate();
        }
    }

    public List<PlayerMatchStat> getMatchPlayerStats(int matchId) throws SQLException {
        List<PlayerMatchStat> matchStats = new ArrayList<>();
        String query = "SELECT id_player, goals, assists FROM STATS WHERE id_match = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, matchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int playerId = rs.getInt("id_player");
                    int goals = rs.getInt("goals");
                    int assists = rs.getInt("assists");
                    matchStats.add(new PlayerMatchStat(playerId, goals, assists));
                }
            }
        }
        return matchStats;
    }
}