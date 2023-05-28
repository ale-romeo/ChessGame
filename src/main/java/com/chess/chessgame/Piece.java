package com.chess.chessgame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Piece implements Serializable {
    private Color color;
    private List<Move> availableMoves = new ArrayList<>();
    public abstract void calculatePossibleMoves(Chessboard board, Square currentSquare, Square KingSquare);

    public Piece(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public List<Move> getAvailableMoves() {
        return availableMoves;
    }

    public void addAvailableMoves(Move move) {
        if (availableMoves == null) {
            availableMoves = new ArrayList<>();
        }
        availableMoves.add(move);
    }
    public void clearAvailableMoves() {
        if (availableMoves != null) {
            availableMoves.clear();
        }
    }
}
