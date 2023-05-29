package com.chess.chessgame;

public class Rook extends Piece {
    public Rook(Color color) { super(color); }

    @Override
    public void calculatePossibleMoves(Chessboard board, Square currentSquare, Square KingSquare) {
        this.clearAvailableMoves();
        int currentRank = currentSquare.getRank();
        char currentFile = currentSquare.getFile();

        // Movimento orizzontale verso destra
        for (char file = (char) (currentFile + 1); file <= 'H'; file++) {
            Square nextSquare = board.getSquare(currentRank, file);
            if (nextSquare == null) break;

            if (!board.isOccupied(nextSquare)) {
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
            } else if (board.isOccupiedByOpponent(nextSquare, getColor())) {
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
            } else if (board.isOccupiedByOpponent(nextSquare, getColor())) {
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
            } else if (board.isOccupiedByOpponent(nextSquare, getColor())) {
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
            } else if (board.isOccupiedByOpponent(nextSquare, getColor())) {
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
                break;
            } else {
                break;
            }
        }
    }
}
