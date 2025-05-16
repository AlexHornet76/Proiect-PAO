package main.java.DAOs;

import main.java.models.Coach;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoachDAO {
    private final Connection connection;

    public CoachDAO(Connection connection) {
        this.connection = connection;
    }

    public void createCoach(Coach coach, int teamId) throws SQLException {
        // First insert into PERSON table
        String personQuery = "INSERT INTO PERSON (name, birthday, nationality, id_team) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(personQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, coach.getName());
            stmt.setDate(2, new java.sql.Date(coach.getBirthday().getTime()));
            stmt.setString(3, coach.getNationality());
            stmt.setInt(4, teamId);
            stmt.executeUpdate();

            // Get generated person ID
            int personId;
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    personId = generatedKeys.getInt(1);
                    coach.setId_person(personId);
                } else {
                    throw new SQLException("Creating person failed, no ID obtained.");
                }
            }

            // Then insert into COACH table
            String coachQuery = "INSERT INTO COACH (id_coach, type, experience) VALUES (?, ?, ?)";
            try (PreparedStatement coachStmt = connection.prepareStatement(coachQuery)) {
                coachStmt.setInt(1, personId);
                coachStmt.setString(2, coach.getType());
                coachStmt.setInt(3, coach.getExperience());
                coachStmt.executeUpdate();
            }
        }
    }

    public List<Coach> getCoachesByTeam(int teamId) throws SQLException {
        List<Coach> coaches = new ArrayList<>();
        String query = "SELECT p.id_person, p.name, p.birthday, p.nationality, " +
                "c.type, c.experience " +
                "FROM PERSON p JOIN COACH c ON p.id_person = c.id_coach " +
                "WHERE p.id_team = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Coach coach = new Coach(
                            rs.getInt("id_person"),
                            rs.getString("name"),
                            rs.getDate("birthday"),
                            rs.getString("nationality"),
                            rs.getString("type"),
                            rs.getInt("experience")
                    );
                    coaches.add(coach);
                }
            }
        }
        return coaches;
    }

    public void updateCoach(Coach coach) throws SQLException {
        connection.setAutoCommit(false);
        try {
            // Update PERSON table
            String personQuery = "UPDATE PERSON SET name = ?, birthday = ?, nationality = ? WHERE id_person = ?";
            try (PreparedStatement stmt = connection.prepareStatement(personQuery)) {
                stmt.setString(1, coach.getName());
                stmt.setDate(2, new java.sql.Date(coach.getBirthday().getTime()));
                stmt.setString(3, coach.getNationality());
                stmt.setInt(4, coach.getId_person());
                stmt.executeUpdate();
            }

            // Update COACH table
            String coachQuery = "UPDATE COACH SET type = ?, experience = ? WHERE id_coach = ?";
            try (PreparedStatement stmt = connection.prepareStatement(coachQuery)) {
                stmt.setString(1, coach.getType());
                stmt.setInt(2, coach.getExperience());
                stmt.setInt(3, coach.getId_person());
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

    public void deleteCoach(int coachId) throws SQLException {
        connection.setAutoCommit(false);
        try {
            // First delete from COACH table
            String coachQuery = "DELETE FROM COACH WHERE id_coach = ?";
            try (PreparedStatement stmt = connection.prepareStatement(coachQuery)) {
                stmt.setInt(1, coachId);
                stmt.executeUpdate();
            }

            // Then delete from PERSON table
            String personQuery = "DELETE FROM PERSON WHERE id_person = ?";
            try (PreparedStatement stmt = connection.prepareStatement(personQuery)) {
                stmt.setInt(1, coachId);
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