package com.chess.chessgame;

public class ScoreboardEntry {
    private final String nickname;
    private final int wins;
    private final int losses;
    private final int draws;

    public ScoreboardEntry(String nickname, int wins, int losses, int draws) {
        this.nickname = nickname;
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
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

    public int getDraws() {
        return draws;
    }
}
