package com.chess.chessgame;

import java.util.List;

public abstract class Piece {
    private Color color;
    private List<Move> availableMoves;
    public abstract void calculatePossibleMoves(Chessboard board, Square currentSquare);

    public Piece(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public List<Move> getAvailableMoves(Chessboard board, Square currentSquare) {
        return availableMoves;
    }

    public void setAvailableMoves(List<Move> availableMoves) {
        this.availableMoves = availableMoves;
    }
    public void addAvailableMoves(Move move) {
        this.availableMoves.add(move);
    }
}
