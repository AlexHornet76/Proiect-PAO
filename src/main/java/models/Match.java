package main.java.models;

import java.util.Date;

public class Match {
    private int id;
    private String homeTeam;
    private String awayTeam;
    private Date date;
    private Integer homeGoals;
    private Integer awayGoals;

    public Match(int id, String homeTeam, String awayTeam, Date date, Integer homeGoals, Integer awayGoals) {
        this.id = id;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.date = date;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
    }

    // Constructor without result for upcoming matches
    public Match(int id, String homeTeam, String awayTeam, Date date) {
        this.id = id;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.date = date;
        this.homeGoals = null;
        this.awayGoals = null;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getHomeGoals() {
        return homeGoals;
    }

    public void setHomeGoals(Integer homeGoals) {
        this.homeGoals = homeGoals;
    }

    public Integer getAwayGoals() {
        return awayGoals;
    }

    public void setAwayGoals(Integer awayGoals) {
        this.awayGoals = awayGoals;
    }

    public boolean isPlayed() {
        return homeGoals != null && awayGoals != null;
    }

    public String getResult() {
        if (isPlayed()) {
            return homeGoals + " - " + awayGoals;
        }
        return "Match not played yet";
    }

    public String getWinner() {
        if (!isPlayed()) {
            return "Match not played yet";
        }

        if (homeGoals > awayGoals) {
            return homeTeam;
        } else if (awayGoals > homeGoals) {
            return awayTeam;
        } else {
            return "Draw";
        }
    }

    @Override
    public String toString() {
        return homeTeam + " vs " + awayTeam + " (" + (isPlayed() ? getResult() : "upcoming") + ")";
    }
}