package com.chess.chessgame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Chessboard implements Serializable {
    private Square[][] squares;

    public Chessboard() {
        squares = new Square[8][8];
        initializeBoard();
    }

    private void initializeBoard() {
        // Popola la scacchiera con le caselle
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'A'; file <= 'H'; file++) {
                if (file % 2 != 0 && rank % 2 != 0) {
                    squares[rank][file] = new Square(rank, file, Color.BLACK);
                } else {
                    squares[rank][file] = new Square(rank, file, Color.WHITE);
                }
            }
        }
        // Posizione iniziale dei pezzi bianchi
        squares[1]['A'].setPiece(new Rook(Color.WHITE));
        squares[1]['B'].setPiece(new Knight(Color.WHITE));
        squares[1]['C'].setPiece(new Bishop(Color.WHITE));
        squares[1]['D'].setPiece(new Queen(Color.WHITE));
        squares[1]['E'].setPiece(new King(Color.WHITE));
        squares[1]['F'].setPiece(new Bishop(Color.WHITE));
        squares[1]['G'].setPiece(new Knight(Color.WHITE));
        squares[1]['H'].setPiece(new Rook(Color.WHITE));

// Posizione iniziale dei pedoni bianchi
        for (char file = 'A'; file <= 'H'; file++) {
            squares[2][file].setPiece(new Pawn(Color.WHITE));
        }

// Posizione iniziale dei pezzi neri
        squares[8]['A'].setPiece(new Rook(Color.BLACK));
        squares[8]['B'].setPiece(new Knight(Color.BLACK));
        squares[8]['C'].setPiece(new Bishop(Color.BLACK));
        squares[8]['D'].setPiece(new Queen(Color.BLACK));
        squares[8]['E'].setPiece(new King(Color.BLACK));
        squares[8]['F'].setPiece(new Bishop(Color.BLACK));
        squares[8]['G'].setPiece(new Knight(Color.BLACK));
        squares[8]['H'].setPiece(new Rook(Color.BLACK));

// Posizione iniziale dei pedoni neri
        for (char file = 'A'; file <= 'H'; file++) {
            squares[7][file].setPiece(new Pawn(Color.BLACK));
        }

    }

    public Square getSquare(int rank, char file) {
        if (isValidSquare(rank, file)) {
            return squares[rank][file];
        }
        return null;
    }

    public List<Square> getAllSquares() {
        List<Square> allSquares = new ArrayList<>();

        for (char file = 'A'; file <= 'H'; file++) {
            for (int rank = 1; rank <= 8; rank++) {
                allSquares.add(this.squares[rank][file]);
            }
        }

        return allSquares;
    }


    public boolean isValidSquare(int rank, char file) {
        return rank >= 1 && rank <= 8 && file >= 'A' && file <= 'H';
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
