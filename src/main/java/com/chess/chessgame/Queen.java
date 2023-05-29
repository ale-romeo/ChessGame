package com.chess.chessgame;

public class Queen extends Piece {
    public Queen(Color color) { super(color); }

    @Override
    public void calculatePossibleMoves(Chessboard board, Square currentSquare, Square KingSquare) {
        this.clearAvailableMoves();
        // Calcola le mosse orizzontali e verticali
        getHorizontalVerticalMoves(board, currentSquare, 1, 0, KingSquare); // Verso destra
        getHorizontalVerticalMoves(board, currentSquare, -1, 0, KingSquare); // Verso sinistra
        getHorizontalVerticalMoves(board, currentSquare, 0, 1, KingSquare); // Verso alto
        getHorizontalVerticalMoves(board, currentSquare, 0, -1, KingSquare); // Verso basso

        // Calcola le mosse diagonali
        getDiagonalMoves(board, currentSquare, 1, 1, KingSquare); // In alto a destra
        getDiagonalMoves(board, currentSquare, 1, -1, KingSquare); // In alto a sinistra
        getDiagonalMoves(board, currentSquare, -1, 1, KingSquare); // In basso a destra
        getDiagonalMoves(board, currentSquare, -1, -1, KingSquare); // In basso a sinistra
    }

    private void getHorizontalVerticalMoves(Chessboard board, Square currentSquare, int rankOffset, int fileOffset, Square KingSquare) {
        int currentRank = currentSquare.getRank();
        char currentFile = currentSquare.getFile();

        int targetRank = currentRank + rankOffset;
        char targetFile = (char) (currentFile + fileOffset);

        while (board.isValidSquare(targetRank - 1, targetFile)) {
            Square targetSquare = board.getSquare(targetRank, targetFile);

            if (board.isOccupiedByOpponent(targetSquare, getColor())) {
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
                break;
            }

            if (board.isOccupied(targetSquare)) {
                break;
            }

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
            targetRank += rankOffset;
            targetFile += fileOffset;
        }
    }

    private void getDiagonalMoves(Chessboard board, Square currentSquare, int rankOffset, int fileOffset, Square KingSquare) {
        int currentRank = currentSquare.getRank();
        char currentFile = currentSquare.getFile();

        int targetRank = currentRank + rankOffset;
        char targetFile = (char) (currentFile + fileOffset);

        while (board.isValidSquare(targetRank - 1, targetFile)) {
            Square targetSquare = board.getSquare(targetRank, targetFile);

            if (board.isOccupiedByOpponent(targetSquare, getColor())) {
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
                break;
            }

            if (board.isOccupied(targetSquare)) {
                break;
            }

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
            targetRank += rankOffset;
            targetFile += fileOffset;
        }
    }
}
