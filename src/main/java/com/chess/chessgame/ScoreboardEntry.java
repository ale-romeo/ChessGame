package com.chess.chessgame;

public class ScoreboardEntry {
    private String nickname;
    private int wins;
    private int losses;

    public ScoreboardEntry(String nickname, int wins, int losses) {
        this.nickname = nickname;
        this.wins = wins;
        this.losses = losses;
    }

    public String getNickname() {
        return nickname;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }
}
