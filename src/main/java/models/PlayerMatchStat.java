package main.java.models;

public class PlayerMatchStat {
    private int playerId;
    private int goals;
    private int assists;

    public PlayerMatchStat(int playerId, int goals, int assists) {
        this.playerId = playerId;
        this.goals = goals;
        this.assists = assists;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getGoals() {
        return goals;
    }

    public int getAssists() {
        return assists;
    }
}