package com.chess.chessgame;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    public King(Color color) { super(color); }

    @Override
    public void calculatePossibleMoves(Chessboard board, Square currentSquare) {

        int currentRank = currentSquare.getRank();
        char currentFile = currentSquare.getFile();

        // Calcola le mosse nelle 8 direzioni intorno al re
        addMoveIfValid(board, currentSquare, currentRank + 1, currentFile); // Movimento verso l'alto
        addMoveIfValid(board, currentSquare, currentRank - 1, currentFile); // Movimento verso il basso
        addMoveIfValid(board, currentSquare, currentRank, (char) (currentFile + 1)); // Movimento verso destra
        addMoveIfValid(board, currentSquare, currentRank, (char) (currentFile - 1)); // Movimento verso sinistra
        addMoveIfValid(board, currentSquare, currentRank + 1, (char) (currentFile + 1)); // Movimento in alto a destra
        addMoveIfValid(board, currentSquare, currentRank + 1, (char) (currentFile - 1)); // Movimento in alto a sinistra
        addMoveIfValid(board, currentSquare, currentRank - 1, (char) (currentFile + 1)); // Movimento in basso a destra
        addMoveIfValid(board, currentSquare, currentRank - 1, (char) (currentFile - 1)); // Movimento in basso a sinistra

    }

    private void addMoveIfValid(Chessboard board, Square currentSquare, int rank, char file) {
        Square targetSquare = board.getSquare(rank, file);
        if (targetSquare != null && (!board.isOccupied(targetSquare) || board.isOccupiedByOpponent(targetSquare, getColor()))) {
            List<Square> threats = getThreats(board, targetSquare);
            if (!threats.contains(targetSquare)) {
                addAvailableMoves(new Move(currentSquare, targetSquare));
            }
        }
    }
    private List<Square> getThreats(Chessboard board, Square kingSquare) {
        List<Square> threats = new ArrayList<>();
        Color kingColor = getColor();

        // Ottieni le minacce dai pedoni avversari
        threats.addAll(getOpponentPawnThreats(board, kingSquare, kingColor));

        // Ottieni le minacce dagli alfieri e dai pezzi della regina avversaria
        threats.addAll(getOpponentBishopQueenThreats(board, kingSquare, kingColor));

        // Ottieni le minacce dalle torri e dai pezzi della regina avversaria
        threats.addAll(getOpponentRookQueenThreats(board, kingSquare, kingColor));

        // Ottieni le minacce dai cavalli avversari
        threats.addAll(getOpponentKnightThreats(board, kingSquare, kingColor));

        // Ottieni le minacce dal re avversario
        threats.addAll(getOpponentKingThreats(board, kingSquare, kingColor));

        return threats;
    }

    private List<Square> getOpponentPawnThreats(Chessboard board, Square kingSquare, Color kingColor) {
        List<Square> threats = new ArrayList<>();
        int direction = (kingColor == Color.WHITE) ? -1 : 1; // Direzione dei pedoni avversari

        // Calcola le caselle in cui i pedoni avversari possono minacciare il re
        Square leftThreat = board.getSquare(kingSquare.getRank() + direction, (char) (kingSquare.getFile() - 1));
        Square rightThreat = board.getSquare(kingSquare.getRank() + direction, (char) (kingSquare.getFile() + 1));

        // Verifica se i pedoni avversari minacciano il re
        if (leftThreat != null && board.isOccupiedByOpponent(leftThreat, kingColor) && leftThreat.getPiece() instanceof Pawn) {
            threats.add(leftThreat);
        }
        if (rightThreat != null && board.isOccupiedByOpponent(rightThreat, kingColor) && rightThreat.getPiece() instanceof Pawn) {
            threats.add(rightThreat);
        }

        return threats;
    }

    private List<Square> getOpponentBishopQueenThreats(Chessboard board, Square kingSquare, Color kingColor) {
        List<Square> threats = new ArrayList<>();

        // Calcola le caselle in cui gli alfieri e i pezzi della regina avversaria possono minacciare il re
        threats.addAll(getThreatsInDiagonalDirections(board, kingSquare, kingColor));

        return threats;
    }

    private List<Square> getOpponentRookQueenThreats(Chessboard board, Square kingSquare, Color kingColor) {
        List<Square> threats = new ArrayList<>();

        // Calcola le caselle in cui le torri e i pezzi della regina avversaria possono minacciare il re
        threats.addAll(getThreatsInHorizontalAndVerticalDirections(board, kingSquare, kingColor));

        return threats;
    }

    private List<Square> getOpponentKnightThreats(Chessboard board, Square kingSquare, Color kingColor) {
        List<Square> threats = new ArrayList<>();

        // Calcola le caselle in cui i cavalli avversari possono minacciare il re
        for (Square square : board.getAllSquares()) {
            Piece piece = square.getPiece();
            if (piece != null && piece.getColor() != kingColor && piece instanceof Knight) {
                List<Move> knightMoves = piece.getAvailableMoves();
                for (Move move: knightMoves) {
                    if (move.getToSquare().equals(kingSquare)) {
                        threats.add(square);
                    }
                }
            }
        }

        return threats;
    }

    private List<Square> getOpponentKingThreats(Chessboard board, Square kingSquare, Color kingColor) {
        List<Square> threats = new ArrayList<>();

        // Calcola le caselle in cui il re avversario può minacciare il re
        for (Square square : board.getAllSquares()) {
            Piece piece = square.getPiece();
            if (piece != null && piece.getColor() != kingColor && piece instanceof King) {
                List<Move> kingMoves = piece.getAvailableMoves();
                for (Move move: kingMoves) {
                    if (move.getToSquare().equals(kingSquare)) {
                        threats.add(square);
                    }
                }
            }
        }

        return threats;
    }

    // Metodo ausiliario per ottenere le minacce in direzioni diagonali (alfieri e regine)
    private List<Square> getThreatsInDiagonalDirections(Chessboard board, Square kingSquare, Color kingColor) {
        List<Square> threats = new ArrayList<>();
        int[] directions = { -1, 1 }; // Direzioni diagonali (alto-sinistra, alto-destra, basso-sinistra, basso-destra)

        for (int dx : directions) {
            for (int dy : directions) {
                for (char i = 'A'; i <= 'H'; i++) {
                    int targetRank = kingSquare.getRank() + (i * dy);
                    char targetFile = (char) (kingSquare.getFile() + (i * dx));
                    Square targetSquare = board.getSquare(targetRank, targetFile);

                    if (targetSquare == null) {
                        break; // Fuori dalla scacchiera, interrompi il ciclo
                    }

                    Piece piece = targetSquare.getPiece();
                    if (piece != null) {
                        if (piece.getColor() == kingColor) {
                            break; // Pezzo alleato, interrompi il ciclo
                        } else if (piece instanceof Bishop || piece instanceof Queen) {
                            threats.add(targetSquare);
                            break; // Trovato un pezzo avversario che minaccia il re, interrompi il ciclo
                        } else {
                            break; // Altri tipi di pezzi avversari, interrompi il ciclo
                        }
                    }
                }
            }
        }

        return threats;
    }

    // Metodo ausiliario per ottenere le minacce in direzioni orizzontali e verticali (torri e regine)
    private List<Square> getThreatsInHorizontalAndVerticalDirections(Chessboard board, Square kingSquare, Color kingColor) {
        List<Square> threats = new ArrayList<>();
        int[] directions = { -1, 1 }; // Direzioni orizzontali e verticali (sinistra, destra, alto, basso)

        for (int dx : directions) {
            int targetRank = kingSquare.getRank();
            char targetFile = (char) (kingSquare.getFile() + dx);
            Square targetSquare = board.getSquare(targetRank, targetFile);

            while (targetSquare != null) {
                Piece piece = targetSquare.getPiece();
                if (piece != null) {
                    if (piece.getColor() == kingColor) {
                        break; // Pezzo alleato, interrompi il ciclo
                    } else if (piece instanceof Rook || piece instanceof Queen) {
                        threats.add(targetSquare);
                        break; // Trovato un pezzo avversario che minaccia il re, interrompi il ciclo
                    } else {
                        break; // Altri tipi di pezzi avversari, interrompi il ciclo
                    }
                }

                targetFile += dx;
                targetSquare = board.getSquare(targetRank, targetFile);
            }
        }

        for (int dy : directions) {
            int targetRank = kingSquare.getRank() + dy;
            char targetFile = kingSquare.getFile();
            Square targetSquare = board.getSquare(targetRank, targetFile);

            while (targetSquare != null) {
                Piece piece = targetSquare.getPiece();
                if (piece != null) {
                    if (piece.getColor() == kingColor) {
                        break; // Pezzo alleato, interrompi il ciclo
                    } else if (piece instanceof Rook || piece instanceof Queen) {
                        threats.add(targetSquare);
                        break; // Trovato un pezzo avversario che minaccia il re, interrompi il ciclo
                    } else {
                        break; // Altri tipi di pezzi avversari, interrompi il ciclo
                    }
                }

                targetRank += dy;
                targetSquare = board.getSquare(targetRank, targetFile);
            }
        }

        return threats;
    }
}

