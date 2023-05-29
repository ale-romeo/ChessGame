package com.chess.chessgame;
public class Knight extends Piece {
    private static final int[][] KNIGHT_MOVES = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
    };

    public Knight(Color color) { super(color); }

    @Override
    public void calculatePossibleMoves(Chessboard board, Square currentSquare, Square KingSquare) {

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
                    Piece temp = null;
                    Color oppColor;
                    if (getColor() == Color.WHITE) {
                        oppColor = Color.BLACK;
                    } else {
                        oppColor = Color.WHITE;
                    }
                    if (targetSquare.getPiece() instanceof Pawn) {
                        temp = new Pawn(oppColor);
                    } else if (targetSquare.getPiece() instanceof Queen) {
                        temp = new Queen(oppColor);
                    } else if (targetSquare.getPiece() instanceof Rook) {
                        temp = new Rook(oppColor);
                    } else if (targetSquare.getPiece() instanceof Knight) {
                        temp = new Knight(oppColor);
                    } else if (targetSquare.getPiece() instanceof Bishop) {
                        temp = new Bishop(oppColor);
                    } else if (targetSquare.getPiece() instanceof King) {
                        temp = new King(oppColor);
                    }
                    board.movePiece(new Move(currentSquare, targetSquare));
                    if (!((King) KingSquare.getPiece()).Check(board, KingSquare)) {
                        addAvailableMoves(new Move(currentSquare, targetSquare));
                    }
                    board.movePiece(new Move(targetSquare, currentSquare));
                    if (temp != null) {
                        targetSquare.setPiece(temp);
                    }
                }
            }
        }
    }
}

