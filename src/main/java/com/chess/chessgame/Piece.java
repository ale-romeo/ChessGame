package com.chess.chessgame;

import java.util.List;

public abstract class Piece {
    private Color color;
    private List<Move> availableMoves;
    public abstract List<Move> getAvailableMoves(Chessboard board, Square currentSquare);

    public Piece(Color color, List<Move> availableMoves) {
        this.color = color;
        this.availableMoves = availableMoves;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setAvailableMoves(List<Move> availableMoves) {
        this.availableMoves = availableMoves;
    }

}
