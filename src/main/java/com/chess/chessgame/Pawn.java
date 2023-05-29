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
            Piece temp = null;
            Color oppColor;
            if (getColor() == Color.WHITE) {
                oppColor = Color.BLACK;
            } else {
                oppColor = Color.WHITE;
            }
            if (nextSquare.getPiece() instanceof Pawn) {
                temp = new Pawn(oppColor);
            } else if (nextSquare.getPiece() instanceof Queen) {
                temp = new Queen(oppColor);
            } else if (nextSquare.getPiece() instanceof Rook) {
                temp = new Rook(oppColor);
            } else if (nextSquare.getPiece() instanceof Knight) {
                temp = new Knight(oppColor);
            } else if (nextSquare.getPiece() instanceof Bishop) {
                temp = new Bishop(oppColor);
            } else if (nextSquare.getPiece() instanceof King) {
                temp = new King(oppColor);
            }
            board.movePiece(new Move(currentSquare, nextSquare));
            if (!((King) KingSquare.getPiece()).Check(board, KingSquare)) {
                addAvailableMoves(new Move(currentSquare, nextSquare));
            }
            board.movePiece(new Move(nextSquare, currentSquare));
            if (temp != null) {
                nextSquare.setPiece(temp);
            }

            // Movimento in avanti di due caselle se è il primo movimento del pedone
            if (isFirstMove(currentRank, direction)) {
                Square doubleMoveSquare = board.getSquare(currentRank + (2 * direction), currentFile);
                if (doubleMoveSquare != null && !board.isOccupied(doubleMoveSquare)) {
                    temp = null;
                    if (doubleMoveSquare.getPiece() instanceof Pawn) {
                        temp = new Pawn(oppColor);
                    } else if (doubleMoveSquare.getPiece() instanceof Queen) {
                        temp = new Queen(oppColor);
                    } else if (doubleMoveSquare.getPiece() instanceof Rook) {
                        temp = new Rook(oppColor);
                    } else if (doubleMoveSquare.getPiece() instanceof Knight) {
                        temp = new Knight(oppColor);
                    } else if (doubleMoveSquare.getPiece() instanceof Bishop) {
                        temp = new Bishop(oppColor);
                    } else if (doubleMoveSquare.getPiece() instanceof King) {
                        temp = new King(oppColor);
                    }
                    board.movePiece(new Move(currentSquare, doubleMoveSquare));
                    if (!((King) KingSquare.getPiece()).Check(board, KingSquare)) {
                        addAvailableMoves(new Move(currentSquare, doubleMoveSquare));
                    }
                    board.movePiece(new Move(doubleMoveSquare, currentSquare));
                    if (temp != null) {
                        doubleMoveSquare.setPiece(temp);
                    }
                }
            }
        }

        // Movimento diagonale per cattura
        Square captureSquare1 = board.getSquare(currentRank + direction, (char) (currentFile + 1));
        Square captureSquare2 = board.getSquare(currentRank + direction, (char) (currentFile - 1));
        if (captureSquare1 != null && board.isOccupiedByOpponent(captureSquare1, getColor())) {
            Piece temp = null;
            Color oppColor;
            if (getColor() == Color.WHITE) {
                oppColor = Color.BLACK;
            } else {
                oppColor = Color.WHITE;
            }
            if (captureSquare1.getPiece() instanceof Pawn) {
                temp = new Pawn(oppColor);
            } else if (captureSquare1.getPiece() instanceof Queen) {
                temp = new Queen(oppColor);
            } else if (captureSquare1.getPiece() instanceof Rook) {
                temp = new Rook(oppColor);
            } else if (captureSquare1.getPiece() instanceof Knight) {
                temp = new Knight(oppColor);
            } else if (captureSquare1.getPiece() instanceof Bishop) {
                temp = new Bishop(oppColor);
            } else if (captureSquare1.getPiece() instanceof King) {
                temp = new King(oppColor);
            }
            board.movePiece(new Move(currentSquare, captureSquare1));
            if (!((King) KingSquare.getPiece()).Check(board, KingSquare)) {
                addAvailableMoves(new Move(currentSquare, captureSquare1));
            }
            board.movePiece(new Move(captureSquare1, currentSquare));
            if (temp != null) {
                captureSquare1.setPiece(temp);
            }
        }
        if (captureSquare2 != null && board.isOccupiedByOpponent(captureSquare2, getColor())) {
            Piece temp = null;
            Color oppColor;
            if (getColor() == Color.WHITE) {
                oppColor = Color.BLACK;
            } else {
                oppColor = Color.WHITE;
            }
            if (captureSquare2.getPiece() instanceof Pawn) {
                temp = new Pawn(oppColor);
            } else if (captureSquare2.getPiece() instanceof Queen) {
                temp = new Queen(oppColor);
            } else if (captureSquare2.getPiece() instanceof Rook) {
                temp = new Rook(oppColor);
            } else if (captureSquare2.getPiece() instanceof Knight) {
                temp = new Knight(oppColor);
            } else if (captureSquare2.getPiece() instanceof Bishop) {
                temp = new Bishop(oppColor);
            } else if (captureSquare2.getPiece() instanceof King) {
                temp = new King(oppColor);
            }
            board.movePiece(new Move(currentSquare, captureSquare2));
            if (!((King) KingSquare.getPiece()).Check(board, KingSquare)) {
                addAvailableMoves(new Move(currentSquare, captureSquare2));
            }
            board.movePiece(new Move(captureSquare2, currentSquare));
            if (temp != null) {
                captureSquare2.setPiece(temp);
            }
        }
    }

    private boolean isFirstMove(int currentRank, int direction) {
        return (direction == 1 && currentRank == 2) || (direction == -1 && currentRank == 7);
    }
}
