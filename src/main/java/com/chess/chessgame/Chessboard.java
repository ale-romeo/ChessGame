package com.chess.chessgame;

public class Chessboard {
    private Square[][] squares;
    private String files = "ABCDEFGH";

    public Chessboard() {
        squares = new Square[8][8];
        initializeBoard();
    }

    private void initializeBoard() {
        char[] cols = this.files.toCharArray();
        // Popola la scacchiera con le caselle
        for (int rank = 1; rank < 9; rank++) {
            for (int file = 1; file < cols.length+1; file++) {
                if (file % 2 != 0 && rank % 2 != 0) {
                    squares[rank][file] = new Square(rank, cols[file], Color.BLACK);
                } else {
                    squares[rank][file] = new Square(rank, cols[file], Color.WHITE);
                }
            }
        }
    }

    public Square getSquare(int rank, char file) {
        if (isValidSquare(rank, file)) {
            return squares[rank][file];
        }
        return null;
    }

    private boolean isValidSquare(int rank, char file) {
        return rank >= 0 && rank < 8 && files.contains(String.valueOf(file)) ;
    }

    public boolean isOccupied(Square square) {
        return square.getPiece() != null;
    }

    public boolean isOccupiedByOpponent(Square square, Color color) {
        if (isOccupied(square)) {
            return !square.getPiece().getColor().equals(color);
        }
        return false;
    }

    // Altre funzioni utili per la gestione della scacchiera
}
