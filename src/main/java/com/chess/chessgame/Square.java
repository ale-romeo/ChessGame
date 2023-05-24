package com.chess.chessgame;

public class Square {
    private int rank;
    private char file;
    private Piece piece;
    private Color color;

    public Square(int rank, char file, Color color) {
        this.rank = rank;
        this.file = file;
        this.color = color;
        this.piece = null;
    }

    public int getRank() {
        return rank;
    }

    public char getFile() {
        return file;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public void clearPiece() {
        this.piece = null;
    }
}
