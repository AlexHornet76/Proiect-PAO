package main.java.models;

public class Stats {
    private int playerId;
    private String playerName;
    private String teamName;
    private int goals;
    private int assists;

    public Stats(int playerId, String playerName, String teamName, int goals, int assists) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.teamName = teamName;
        this.goals = goals;
        this.assists = assists;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getTeamName() {
        return teamName;
    }

    public int getGoals() {
        return goals;
    }

    public int getAssists() {
        return assists;
    }
}
