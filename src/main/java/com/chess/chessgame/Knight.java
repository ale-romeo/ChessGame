package com.chess.chessgame;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
    private static final int[][] KNIGHT_MOVES = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
    };

    public Knight(Color color) { super(color); }

    @Override
    public void calculatePossibleMoves(Chessboard board, Square currentSquare) {

        int currentRank = currentSquare.getRank();
        char currentFile = currentSquare.getFile();

        for (int[] knightMove : KNIGHT_MOVES) {
            int rankOffset = knightMove[0];
            int fileOffset = knightMove[1];
            int targetRank = currentRank + rankOffset;
            char targetFile = (char) (currentFile + fileOffset);

            Square targetSquare = board.getSquare(targetRank, targetFile);
            if (targetSquare != null) {
                if (!board.isOccupied(targetSquare) || board.isOccupiedByOpponent(targetSquare, getColor())) {
                    addAvailableMoves(new Move(currentSquare, targetSquare));
                }
            }
        }
    }
}

