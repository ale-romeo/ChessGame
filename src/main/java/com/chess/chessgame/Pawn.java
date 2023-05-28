package com.chess.chessgame;

public class Pawn extends Piece {
    public Pawn(Color color) { super(color); }

    @Override
    public void calculatePossibleMoves(Chessboard board, Square currentSquare, Square KingSquare) {

        int currentRank = currentSquare.getRank();
        char currentFile = currentSquare.getFile();

        // Calcola la direzione in base al colore del pezzo
        int direction = (getColor() == Color.WHITE) ? 1 : -1;


        // Movimento in avanti di una casella
        Square nextSquare = board.getSquare(currentRank + direction, currentFile);

        if (nextSquare != null && !board.isOccupied(nextSquare)) {
            if (kingSquare.getPiece().Check(board, kingSquare)) {

            }
            addAvailableMoves(new Move(currentSquare, nextSquare));

            // Movimento in avanti di due caselle se è il primo movimento del pedone
            if (isFirstMove(currentRank, direction)) {
                Square doubleMoveSquare = board.getSquare(currentRank + (2 * direction), currentFile);
                if (doubleMoveSquare != null && !board.isOccupied(doubleMoveSquare)) {
                    addAvailableMoves(new Move(currentSquare, doubleMoveSquare));
                }
            }
        }

        // Movimento diagonale per cattura
        Square captureSquare1 = board.getSquare(currentRank + direction, (char) (currentFile + 1));
        Square captureSquare2 = board.getSquare(currentRank + direction, (char) (currentFile - 1));
        if (captureSquare1 != null && board.isOccupiedByOpponent(captureSquare1, getColor())) {
            addAvailableMoves(new Move(currentSquare, captureSquare1));
        }
        if (captureSquare2 != null && board.isOccupiedByOpponent(captureSquare2, getColor())) {
            addAvailableMoves(new Move(currentSquare, captureSquare2));
        }
    }

    private boolean isFirstMove(int currentRank, int direction) {
        return (direction == 1 && currentRank == 2) || (direction == -1 && currentRank == 7);
    }
}
