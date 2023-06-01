package com.chess.chessgame;

import org.bson.io.BsonOutput;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    public King(Color color) { super(color); }

    public boolean canCastle = true;
    @Override
    public void calculatePossibleMoves(Chessboard board, Square currentSquare, Square KingSquare) {
        this.clearAvailableMoves();
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
        addCastleIfValid(board, currentSquare, currentRank, 'A');
        addCastleIfValid(board, currentSquare, currentRank, 'H');

    }

    private void addCastleIfValid(Chessboard board, Square currentSquare, int rank, char file) {
        Square rookSquare = board.getSquare(rank, file);
        if (this.canCastle && rookSquare.getPiece() instanceof Rook rook && rook.castle) {
            if (file == 'A') {
                for (char f = 'A'; f <= 'E'; f++) {
                    Square s = board.getSquare(rank, f);
                    if (s.getPiece() != null && f != 'A' && f != 'E') {
                        return;
                    }
                    List<Square> threats = getThreats(board, s);
                    if (threats.contains(s)) {
                        return;
                    }
                }
                addAvailableMoves(new Move(currentSquare, board.getSquare(rank, 'C')));
            } else if (file == 'H') {
                for (char f = 'H'; f >= 'E'; f--) {
                    Square s = board.getSquare(rank, f);
                    if (s.getPiece() != null && f != 'H' && f != 'E') {
                        return;
                    }
                    List<Square> threats = getThreats(board, s);
                    if (threats.contains(s)) {
                        return;
                    }
                }
                addAvailableMoves(new Move(currentSquare, board.getSquare(rank, 'G')));
            }
        }
    }

    private void addMoveIfValid(Chessboard board, Square currentSquare, int rank, char file) {
        Square targetSquare = board.getSquare(rank, file);
        if (targetSquare != null && board.isOccupiedKing(targetSquare, getColor())) {
            List<Square> threats = getThreats(board, targetSquare);
            if (!threats.contains(targetSquare)) {
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
                }
                board.movePiece(new Move(currentSquare, targetSquare));
                if (!((King) targetSquare.getPiece()).Check(board, targetSquare)) {
                    addAvailableMoves(new Move(currentSquare, targetSquare));
                }
                board.movePiece(new Move(targetSquare, currentSquare));
                if (temp != null) {
                    targetSquare.setPiece(temp);
                }
            }
        }
    }

    public boolean Check(Chessboard board, Square kingSquare) {
        List<Square> threats = getThreats(board, kingSquare);
        return threats.contains(kingSquare);
    }
    
    private List<Square> getThreats(Chessboard board, Square kingSquare) {
        List<Square> threats = new ArrayList<>();

        // Ottieni le minacce dai pedoni avversari
        threats.addAll(getOpponentPawnThreats(board, kingSquare));

        // Ottieni le minacce dagli alfieri e dai pezzi della regina avversaria
        threats.addAll(getOpponentBishopQueenThreats(board, kingSquare));

        // Ottieni le minacce dalle torri e dai pezzi della regina avversaria
        threats.addAll(getOpponentRookQueenThreats(board, kingSquare));

        // Ottieni le minacce dai cavalli avversari
        threats.addAll(getOpponentKnightThreats(board, kingSquare));

        // Ottieni le minacce dal re avversario
        threats.addAll(getOpponentKingThreats(board, kingSquare));

        return threats;
    }

    private List<Square> getOpponentPawnThreats(Chessboard board, Square kingSquare) {
        List<Square> threats = new ArrayList<>();
        int direction = (getColor() != Color.WHITE) ? -1 : 1; // Direzione dei pedoni avversari

        // Calcola le caselle in cui i pedoni avversari possono minacciare il re
        Square leftThreat = board.getSquare(kingSquare.getRank() + direction, (char) (kingSquare.getFile() - 1));
        Square rightThreat = board.getSquare(kingSquare.getRank() + direction, (char) (kingSquare.getFile() + 1));

        // Verifica se i pedoni avversari minacciano il re
        if (leftThreat != null && board.isOccupiedByOpponent(leftThreat, getColor()) && leftThreat.getPiece() instanceof Pawn) {
            threats.add(kingSquare);
        }
        if (rightThreat != null && board.isOccupiedByOpponent(rightThreat, getColor()) && rightThreat.getPiece() instanceof Pawn) {
            threats.add(kingSquare);
        }

        return threats;
    }

    private List<Square> getOpponentBishopQueenThreats(Chessboard board, Square kingSquare) {
        // Calcola le caselle in cui gli alfieri e i pezzi della regina avversaria possono minacciare il re
        return new ArrayList<>(getThreatsInDiagonalDirections(board, kingSquare));
    }

    private List<Square> getOpponentRookQueenThreats(Chessboard board, Square kingSquare) {
        // Calcola le caselle in cui le torri e i pezzi della regina avversaria possono minacciare il re
        return new ArrayList<>(getThreatsInHorizontalAndVerticalDirections(board, kingSquare));
    }

    private List<Square> getOpponentKnightThreats(Chessboard board, Square kingSquare) {
        List<Square> threats = new ArrayList<>();

        int[] knightOffsets = { -2, -1, 1, 2 };

        // Calcola le caselle in cui i cavalli avversari possono minacciare il re
        for (int dx : knightOffsets) {
            for (int dy : knightOffsets) {
                if (Math.abs(dx) != Math.abs(dy)) {
                    Square targetSquare = board.getSquare(kingSquare.getRank() + dy, (char) (kingSquare.getFile() + dx));
                    if (targetSquare != null) {
                        Piece piece = targetSquare.getPiece();
                        if (piece != null && piece.getColor() != getColor() && piece instanceof Knight) {
                            threats.add(kingSquare);
                        }
                    }
                }
            }
        }

        return threats;
    }

    private List<Square> getOpponentKingThreats(Chessboard board, Square kingSquare) {
        List<Square> threats = new ArrayList<>();

        // Calcola le caselle adiacenti al re avversario
        int[][] adjacentSquares = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},           {0, 1},
                {1, -1},  {1, 0},  {1, 1}
        };

        for (int[] offset : adjacentSquares) {
            int targetRank = kingSquare.getRank() + offset[0];
            char targetFile = (char) (kingSquare.getFile() + offset[1]);
            Square targetSquare = board.getSquare(targetRank, targetFile);

            if (targetSquare != null) {
                Piece piece = targetSquare.getPiece();
                if (piece != null && piece.getColor() != getColor() && piece instanceof King) {
                    threats.add(targetSquare);
                }
            }
        }

        return threats;
    }


    // Metodo ausiliario per ottenere le minacce in direzioni diagonali (alfieri e regine)
    private List<Square> getThreatsInDiagonalDirections(Chessboard board, Square kingSquare) {
        List<Square> threats = new ArrayList<>();
        int[] directions = { -1, 1 }; // Direzioni diagonali (alto-sinistra, alto-destra, basso-sinistra, basso-destra)


        for (int dx : directions){
            for (int dy : directions) {
                int targetRank = kingSquare.getRank();
                char targetFile = kingSquare.getFile();
                for (char i = 'A'; i <= 'H'; i++) {
                    targetRank += dy;
                    targetFile = (char) (targetFile + dx);
                    Square targetSquare = board.getSquare(targetRank, targetFile);

                    if (targetSquare == null) {
                        break; // Fuori dalla scacchiera, interrompi il ciclo
                    }

                    Piece piece = targetSquare.getPiece();
                    if (piece != null) {
                        if (piece.getColor() == getColor()) {
                            break; // Pezzo alleato, interrompi il ciclo
                        } else if (piece instanceof Bishop || piece instanceof Queen) {
                            threats.add(kingSquare);
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
    private List<Square> getThreatsInHorizontalAndVerticalDirections(Chessboard board, Square kingSquare) {
        List<Square> threats = new ArrayList<>();
        int[] directions = { -1, 1 }; // Direzioni orizzontali e verticali (sinistra, destra, alto, basso)

        for (int dx : directions) {
            int targetRank = kingSquare.getRank();
            char targetFile = (char) (kingSquare.getFile() + dx);
            Square targetSquare = board.getSquare(targetRank, targetFile);

            while (targetSquare != null) {
                Piece piece = targetSquare.getPiece();
                if (piece != null) {
                    if (piece.getColor() == getColor()) {
                        break; // Pezzo alleato, interrompi il ciclo
                    } else if (piece instanceof Rook || piece instanceof Queen) {
                        threats.add(kingSquare);
                        break; // Trovato un pezzo avversario che minaccia il re, interrompi il ciclo
                    } else {
                        break; // Altri tipi di pezzi avversari, interrompi il ciclo
                    }
                }

                targetFile = (char) (targetFile + dx);
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
                    if (piece.getColor() == getColor()) {
                        break; // Pezzo alleato, interrompi il ciclo
                    } else if (piece instanceof Rook || piece instanceof Queen) {
                        threats.add(kingSquare);
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