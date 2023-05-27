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
        for (int rank = 0; rank < 8; rank++) {
            for (char file = 'A'; file <= 'H'; file++) {
                if ((file -'A') % 2 != 0 && rank % 2 != 0) {
                    squares[rank][file - 'A'] = new Square(rank + 1, file, Color.BLACK);
                } else {
                    squares[rank][file - 'A'] = new Square(rank + 1, file, Color.WHITE);
                }
            }
        }
        // Posizione iniziale dei pezzi bianchi
        squares[0]['A' - 'A'].setPiece(new Rook(Color.WHITE));
        squares[0]['B' - 'A'].setPiece(new Knight(Color.WHITE));
        squares[0]['C' - 'A'].setPiece(new Bishop(Color.WHITE));
        squares[0]['D' - 'A'].setPiece(new Queen(Color.WHITE));
        squares[0]['E' - 'A'].setPiece(new King(Color.WHITE));
        squares[0]['F' - 'A'].setPiece(new Bishop(Color.WHITE));
        squares[0]['G' - 'A'].setPiece(new Knight(Color.WHITE));
        squares[0]['H' - 'A'].setPiece(new Rook(Color.WHITE));

// Posizione iniziale dei pedoni bianchi
        for (char file = 'A'; file <= 'H'; file++) {
            squares[1][file - 'A'].setPiece(new Pawn(Color.WHITE));
        }

// Posizione iniziale dei pezzi neri
        squares[7]['A' - 'A'].setPiece(new Rook(Color.BLACK));
        squares[7]['B' - 'A'].setPiece(new Knight(Color.BLACK));
        squares[7]['C' - 'A'].setPiece(new Bishop(Color.BLACK));
        squares[7]['D' - 'A'].setPiece(new Queen(Color.BLACK));
        squares[7]['E' - 'A'].setPiece(new King(Color.BLACK));
        squares[7]['F' - 'A'].setPiece(new Bishop(Color.BLACK));
        squares[7]['G' - 'A'].setPiece(new Knight(Color.BLACK));
        squares[7]['H' - 'A'].setPiece(new Rook(Color.BLACK));

// Posizione iniziale dei pedoni neri
        for (char file = 'A'; file <= 'H'; file++) {
            squares[6][file - 'A'].setPiece(new Pawn(Color.BLACK));
        }

    }

    public Square getSquare(int rank, char file) {
        if (isValidSquare(rank - 1, file)) {
            return squares[rank - 1][file - 'A'];
        }
        return null;
    }

    public List<Square> getAllSquares() {
        List<Square> allSquares = new ArrayList<>();

        for (char file = 'A'; file <= 'H'; file++) {
            for (int rank = 0; rank < 8; rank++) {
                allSquares.add(this.squares[rank][file - 'A']);
            }
        }

        return allSquares;
    }

    public boolean isValidSquare(int rank, char file) {
        return rank >= 0 && rank < 8 && file >= 'A' && file <= 'H';
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

    public void movePiece(Move move) {
        Piece piece = move.getPiece();
        squares[move.getFromSquare().getRank() - 1][move.getFromSquare().getFile() - 'A'].clearPiece();
        if (isOccupied(squares[move.getToSquare().getRank() - 1][move.getToSquare().getFile() - 'A']) && isOccupiedByOpponent(squares[move.getToSquare().getRank() - 1][move.getToSquare().getFile() - 'A'], move.getToSquare().getPiece().getColor())) {
            squares[move.getToSquare().getRank() - 1][move.getToSquare().getFile() - 'A'].clearPiece();
        }
        squares[move.getToSquare().getRank() - 1][move.getToSquare().getFile() - 'A'].setPiece(piece);
    }
}
