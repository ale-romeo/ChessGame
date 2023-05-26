package com.chess.chessgame;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {
    public Bishop(Color color) { super(color); }

    @Override
    public void calculatePossibleMoves(Chessboard board, Square currentSquare) {

        int currentRank = currentSquare.getRank();
        char currentFile = currentSquare.getFile();

        // Movimento diagonale verso l'alto a destra
        char file = currentFile;
        for (int rank = currentRank + 1; rank <= 8; rank++) {
            file++;
            Square nextSquare = board.getSquare(rank - 1, file);
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

        // Movimento diagonale verso l'alto a sinistra
        file = currentFile;
        for (int rank = currentRank + 1; rank <= 8; rank++) {
            file--;
            Square nextSquare = board.getSquare(rank - 1, file);
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

        // Movimento diagonale verso il basso a sinistra
        file = currentFile;
        for (int rank = currentRank - 1; rank >= 1; rank--) {
            file--;
            Square nextSquare = board.getSquare(rank - 1, file);
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

        // Movimento diagonale verso il basso a destra
        file = currentFile;
        for (int rank = currentRank - 1; rank >= 1; rank--) {
            file++;
            Square nextSquare = board.getSquare(rank - 1, file);
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

