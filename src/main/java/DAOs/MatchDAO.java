package main.java.DAOs;

import main.java.models.Match;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchDAO {
    private final Connection connection;

    public MatchDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Match> getUpcomingMatches() throws SQLException {
        List<Match> matches = new ArrayList<>();

        String query = "SELECT m.id_match AS id, t1.name AS home_team, t2.name AS away_team, " +
                "m.play_date, m.goals_home, m.goals_away " +
                "FROM MATCHES m " +
                "JOIN TEAM t1 ON m.id_home = t1.id_team " +
                "JOIN TEAM t2 ON m.id_away = t2.id_team " +
                "WHERE m.goals_home = 0 AND m.goals_away = 0 "+
                "ORDER BY m.play_date ASC";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Match match = new Match(
                        rs.getInt("id"),
                        rs.getString("home_team"),
                        rs.getString("away_team"),
                        rs.getDate("play_date"),
                        rs.getObject("goals_home", Integer.class),
                        rs.getObject("goals_away", Integer.class)
                );
                matches.add(match);
            }
        }
        return matches;
    }

    public List<Match> getPlayedMatches() throws SQLException {
        List<Match> matches = new ArrayList<>();

        String query = "SELECT m.id_match AS id, t1.name AS home_team, t2.name AS away_team, " +
                "m.play_date, m.goals_home, m.goals_away " +
                "FROM MATCHES m " +
                "JOIN TEAM t1 ON m.id_home = t1.id_team " +
                "JOIN TEAM t2 ON m.id_away = t2.id_team " +
                "WHERE m.goals_home != 0 AND m.goals_away != 0 " +
                "ORDER BY m.play_date DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Match match = new Match(
                        rs.getInt("id"),
                        rs.getString("home_team"),
                        rs.getString("away_team"),
                        rs.getDate("play_date"),
                        rs.getInt("goals_home"),
                        rs.getInt("goals_away")
                );
                matches.add(match);
            }
        }
        return matches;
    }

    public void createMatch(Match match) throws SQLException {
        String query = "INSERT INTO MATCHES (id_home, id_away, play_date, goals_home, goals_away) " +
                "VALUES ((SELECT id_team FROM TEAM WHERE name = ?), " +
                "(SELECT id_team FROM TEAM WHERE name = ?), ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, match.getHomeTeam());
            stmt.setString(2, match.getAwayTeam());
            stmt.setDate(3, new java.sql.Date(match.getDate().getTime()));

            if (match.getHomeGoals() != null) {
                stmt.setInt(4, match.getHomeGoals());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            if (match.getAwayGoals() != null) {
                stmt.setInt(5, match.getAwayGoals());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.executeUpdate();
        }
    }

    public void updateMatchResult(int matchId, int homeGoals, int awayGoals) throws SQLException {
        String query = "UPDATE MATCHES SET goals_home = ?, goals_away = ? WHERE id_match = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, homeGoals);
            stmt.setInt(2, awayGoals);
            stmt.setInt(3, matchId);
            stmt.executeUpdate();
        }
    }

    public void updateMatchDateTime(int matchId, Date date) throws SQLException {
        String query = "UPDATE MATCHES SET play_date = ? WHERE id_match = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, new java.sql.Date(date.getTime()));
            stmt.setInt(2, matchId);
            stmt.executeUpdate();
        }
    }

    public void deleteMatch(int matchId) throws SQLException {
        String query = "DELETE FROM MATCHES WHERE id_match = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, matchId);
            stmt.executeUpdate();
        }
    }

    public List<Match> getAllMatches() throws SQLException {
        List<Match> matches = new ArrayList<>();

        String query = "SELECT m.id_match AS id, t1.name AS home_team, t2.name AS away_team, " +
                "m.play_date, m.goals_home, m.goals_away " +
                "FROM MATCHES m " +
                "JOIN TEAM t1 ON m.id_home = t1.id_team " +
                "JOIN TEAM t2 ON m.id_away = t2.id_team " +
                "ORDER BY m.play_date DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Match match = new Match(
                        rs.getInt("id"),
                        rs.getString("home_team"),
                        rs.getString("away_team"),
                        rs.getDate("play_date"),
                        rs.getObject("goals_home", Integer.class),
                        rs.getObject("goals_away", Integer.class)
                );
                matches.add(match);
            }
        }
        return matches;
    }

    public Match getMatchById(int matchId) throws SQLException {
        String query = "SELECT m.id_match AS id, t1.name AS home_team, t2.name AS away_team, " +
                "m.play_date, m.goals_home, m.goals_away " +
                "FROM MATCHES m " +
                "JOIN TEAM t1 ON m.id_home = t1.id_team " +
                "JOIN TEAM t2 ON m.id_away = t2.id_team " +
                "WHERE m.id_match = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, matchId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Match(
                            rs.getInt("id"),
                            rs.getString("home_team"),
                            rs.getString("away_team"),
                            rs.getDate("play_date"),
                            rs.getObject("goals_home", Integer.class),
                            rs.getObject("goals_away", Integer.class)
                    );
                }
            }
        }
        return null;
    }

    public void updateMatch(Match match) {
        String query = "UPDATE MATCHES SET id_home = (SELECT id_team FROM TEAM WHERE name = ?), " +
                "id_away = (SELECT id_team FROM TEAM WHERE name = ?), play_date = ?, " +
                "goals_home = ?, goals_away = ? WHERE id_match = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, match.getHomeTeam());
            stmt.setString(2, match.getAwayTeam());
            stmt.setDate(3, new java.sql.Date(match.getDate().getTime()));
            stmt.setInt(4, match.getHomeGoals());
            stmt.setInt(5, match.getAwayGoals());
            stmt.setInt(6, match.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}