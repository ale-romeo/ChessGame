package com.chess.chessgame;

public class Queen extends Piece {
    public Queen(Color color) { super(color); }

    @Override
    public void calculatePossibleMoves(Chessboard board, Square currentSquare, Square KingSquare) {

        // Calcola le mosse orizzontali e verticali
        getHorizontalVerticalMoves(board, currentSquare, 1, 0); // Verso destra
        getHorizontalVerticalMoves(board, currentSquare, -1, 0); // Verso sinistra
        getHorizontalVerticalMoves(board, currentSquare, 0, 1); // Verso alto
        getHorizontalVerticalMoves(board, currentSquare, 0, -1); // Verso basso

        // Calcola le mosse diagonali
        getDiagonalMoves(board, currentSquare, 1, 1); // In alto a destra
        getDiagonalMoves(board, currentSquare, 1, -1); // In alto a sinistra
        getDiagonalMoves(board, currentSquare, -1, 1); // In basso a destra
        getDiagonalMoves(board, currentSquare, -1, -1); // In basso a sinistra
    }

    private void getHorizontalVerticalMoves(Chessboard board, Square currentSquare, int rankOffset, int fileOffset) {
        int currentRank = currentSquare.getRank();
        char currentFile = currentSquare.getFile();

        int targetRank = currentRank + rankOffset;
        char targetFile = (char) (currentFile + fileOffset);

        while (board.isValidSquare(targetRank - 1, targetFile)) {
            Square targetSquare = board.getSquare(targetRank, targetFile);

            if (board.isOccupiedByOpponent(targetSquare, getColor())) {
                addAvailableMoves(new Move(currentSquare, targetSquare));
                break;
            }

            if (board.isOccupied(targetSquare)) {
                break;
            }

            addAvailableMoves(new Move(currentSquare, targetSquare));
            targetRank += rankOffset;
            targetFile += fileOffset;
        }
    }

    private void getDiagonalMoves(Chessboard board, Square currentSquare, int rankOffset, int fileOffset) {
        int currentRank = currentSquare.getRank();
        char currentFile = currentSquare.getFile();

        int targetRank = currentRank + rankOffset;
        char targetFile = (char) (currentFile + fileOffset);

        while (board.isValidSquare(targetRank - 1, targetFile)) {
            Square targetSquare = board.getSquare(targetRank, targetFile);

            if (board.isOccupiedByOpponent(targetSquare, getColor())) {
                addAvailableMoves(new Move(currentSquare, targetSquare));
                break;
            }

            if (board.isOccupied(targetSquare)) {
                break;
            }

            addAvailableMoves(new Move(currentSquare, targetSquare));
            targetRank += rankOffset;
            targetFile += fileOffset;
        }
    }
}
