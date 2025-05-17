package main.java.models;

public class GameAction {
    private int id;
    private int matchId;
    private int playerId;
    private String actionType;
    private int minute;
    private int seconds;

    public GameAction(int id, int matchId, int playerId, String actionType, int minute, int seconds) {
        this.id = id;
        this.matchId = matchId;
        this.playerId = playerId;
        this.actionType = actionType;
        this.minute = minute;
        this.seconds = seconds;
    }

    // Getters
    public int getId() { return id; }
    public int getMatchId() { return matchId; }
    public int getPlayerId() { return playerId; }
    public String getActionType() { return actionType; }
    public int getMinute() { return minute; }
    public int getSeconds() { return seconds; }

    // Format the time as MM:SS
    public String getFormattedTime() {
        return String.format("%02d:%02d", minute, seconds);
    }
}
