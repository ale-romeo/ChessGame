package com.chess.chessgame;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {
    public Rook(Color color) { super(color); }

    @Override
    public void calculatePossibleMoves(Chessboard board, Square currentSquare) {

        int currentRank = currentSquare.getRank();
        char currentFile = currentSquare.getFile();

        // Movimento orizzontale verso destra
        for (char file = (char) (currentFile + 1); file <= 'H'; file++) {
            Square nextSquare = board.getSquare(currentRank, file);
            if (nextSquare == null) break;

            if (!board.isOccupied(nextSquare)) {
                addAvailableMoves(new Move(currentSquare, nextSquare));
            } else if (board.isOccupiedByOpponent(nextSquare, getColor())) {
                addAvailableMoves(new Move(currentSquare, nextSquare));
                break;
            } else {
                break;
            }
        }

        // Movimento orizzontale verso sinistra
        for (char file = (char) (currentFile - 1); file >= 'A'; file--) {
            Square nextSquare = board.getSquare(currentRank, file);
            if (nextSquare == null) break;

            if (!board.isOccupied(nextSquare)) {
                addAvailableMoves(new Move(currentSquare, nextSquare));
            } else if (board.isOccupiedByOpponent(nextSquare, getColor())) {
                addAvailableMoves(new Move(currentSquare, nextSquare));
                break;
            } else {
                break;
            }
        }

        // Movimento verticale verso l'alto
        for (int rank = currentRank + 1; rank <= 8; rank++) {
            Square nextSquare = board.getSquare(rank, currentFile);
            if (nextSquare == null) break;

            if (!board.isOccupied(nextSquare)) {
                addAvailableMoves(new Move(currentSquare, nextSquare));
            } else if (board.isOccupiedByOpponent(nextSquare, getColor())) {
                addAvailableMoves(new Move(currentSquare, nextSquare));
                break;
            } else {
                break;
            }
        }

        // Movimento verticale verso il basso
        for (int rank = currentRank - 1; rank >= 1; rank--) {
            Square nextSquare = board.getSquare(rank, currentFile);
            if (nextSquare == null) break;

            if (!board.isOccupied(nextSquare)) {
                addAvailableMoves(new Move(currentSquare, nextSquare));
            } else if (board.isOccupiedByOpponent(nextSquare, getColor())) {
                addAvailableMoves(new Move(currentSquare, nextSquare));
                break;
            } else {
                break;
            }
        }
    }
}