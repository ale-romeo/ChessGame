package com.chess.chessgame;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import javafx.scene.media.AudioClip;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;


public class ClientHandler implements Runnable {
    private final Socket White;
    private final Socket Black;
    private final Chessboard chessboard;
    private boolean isWhiteTurn = true;
    public boolean running = true;
    private String whitePlayer;
    private String blackPlayer;
    private String status = "Running";
    private boolean castle = false, move_self = false, capture = false;
    private String audioClip = "file:src/main/img/game-start.wav";

    public ClientHandler(Socket clientSocket, Socket waitingClient) {
        Random random = new Random();
        int r = random.nextInt(2);
        if (r == 0){
            this.White = clientSocket;
            this.Black = waitingClient;
        } else {
            this.White = waitingClient;
            this.Black = clientSocket;
        }
        this.chessboard = new Chessboard();
    }

    public void run() {
        try {
            receiveNickname();
            sendNickname();

            sendColor(White, Color.WHITE);
            sendColor(Black, Color.BLACK);
            while (running) {
                sendGameStatus();
                sendChessboard(this.chessboard);
                sendSound();
                sendTurn(isWhiteTurn);
                if (isWhiteTurn) {
                    handlePlayerTurn(White);
                    isWhiteTurn = false;
                } else {
                    handlePlayerTurn(Black);
                    isWhiteTurn = true;
                }
                if (Objects.equals(status, "Promo")) {
                    isWhiteTurn = !isWhiteTurn;
                }
            }
            sendGameStatus();
            sendSound();
            sendScoreboard(White);
            sendScoreboard(Black);
        } catch (IOException e) {
            System.out.println("Errore durante l'esecuzione del client handler: " + e.getMessage());
            try {
                System.out.println(waitForResponse(isWhiteTurn ? White : Black));
                writeToMongoDB(isWhiteTurn ? whitePlayer : blackPlayer, 1, 0, 0);
                writeToMongoDB(isWhiteTurn ? blackPlayer : whitePlayer, 0, 1, 0);
                sendConnErr(isWhiteTurn ? White : Black);
                sendScoreboard(isWhiteTurn ? White : Black);
            } catch (IOException ex) {
                try {
                    System.out.println(isWhiteTurn ? "White disconnesso" : "Black disconnesso");
                    writeToMongoDB(isWhiteTurn ? blackPlayer : whitePlayer, 1, 0, 0);
                    writeToMongoDB(isWhiteTurn ? whitePlayer : blackPlayer, 0, 1, 0);
                    sendConnErr(isWhiteTurn ? Black : White);
                    sendScoreboard(isWhiteTurn ? Black : White);
                } catch (IOException exc) {
                    throw new RuntimeException(exc);
                }
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (White != null) {
                try {
                    White.close();
                } catch (IOException e) {
                    // Gestione dell'eccezione durante la chiusura del socket del client
                    e.printStackTrace();
                }
            }
            if (Black != null) {
                try {
                    Black.close();
                } catch (IOException e) {
                    // Gestione dell'eccezione durante la chiusura del socket del client
                    e.printStackTrace();
                }
            }
        }
    }

    private void receiveNickname() throws IOException, ClassNotFoundException {
        ObjectInputStream whiteInputStream = new ObjectInputStream(White.getInputStream());
        whitePlayer = (String) whiteInputStream.readObject();
        ObjectInputStream blackInputStream = new ObjectInputStream(Black.getInputStream());
        blackPlayer = (String) blackInputStream.readObject();
        if (whitePlayer.equals(blackPlayer)) {
            Random r = new Random();
            int rr = r.nextInt(1000);
            blackPlayer += String.valueOf(rr);
            System.out.println("I giocatori hanno inserito lo stesso nickname. Il nickname del nero è stato cambiato in: " + blackPlayer);
        }

        System.out.println("Inizio partita tra " + whitePlayer + " e " + blackPlayer);
    }

    private void sendNickname() throws IOException {
        ObjectOutputStream whiteOutputStream = new ObjectOutputStream(White.getOutputStream());
        whiteOutputStream.writeObject(whitePlayer + " " + blackPlayer);
        whiteOutputStream.flush();

        ObjectOutputStream blackOutputStream = new ObjectOutputStream(Black.getOutputStream());
        blackOutputStream.writeObject(blackPlayer + " " + whitePlayer);
        blackOutputStream.flush();
    }

    private void sendColor(Socket clientSocket, Color color) throws IOException {
        ObjectOutputStream clientOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        clientOutputStream.writeObject(color);
        clientOutputStream.flush();
    }

    private void sendChessboard(Chessboard chessboard) throws IOException {
        ObjectOutputStream whiteOutputStream = new ObjectOutputStream(White.getOutputStream());
        whiteOutputStream.writeObject(chessboard);
        whiteOutputStream.flush();

        ObjectOutputStream blackOutputStream = new ObjectOutputStream(Black.getOutputStream());
        blackOutputStream.writeObject(chessboard);
        blackOutputStream.flush();
    }

    private void sendTurn(boolean turn) throws IOException {
        ObjectOutputStream whiteOutputStream = new ObjectOutputStream(White.getOutputStream());
        whiteOutputStream.writeObject(turn ? Color.WHITE : Color.BLACK);
        whiteOutputStream.flush();

        ObjectOutputStream blackOutputStream = new ObjectOutputStream(Black.getOutputStream());
        blackOutputStream.writeObject(turn ? Color.WHITE : Color.BLACK);
        blackOutputStream.flush();
    }

    private void handlePlayerTurn(Socket currentPlayerSocket) throws IOException, ClassNotFoundException {
        ObjectInputStream currentPlayerInputStream = new ObjectInputStream(currentPlayerSocket.getInputStream());
        Object obj = currentPlayerInputStream.readObject();
        if (obj instanceof String && (obj).equals("Surrender")) {
            Color turn = !isWhiteTurn ? Color.WHITE : Color.BLACK;
            writeToMongoDB((isWhiteTurn ? blackPlayer : whitePlayer), 1, 0, 0);
            writeToMongoDB((isWhiteTurn ? whitePlayer : blackPlayer), 0, 1, 0);
            status = turn + " Wins";
            audioClip = "file:src/main/img/game-end.wav";
            running = false;
        } else {
            if (obj instanceof Piece piece && Objects.equals(status, "Promo")) {

                List<Square> allSquares = this.chessboard.getAllSquares();
                for (Square a : allSquares) {
                    if (a != null && a.getPiece() instanceof Pawn && (a.getRank() == 8 || a.getRank() == 1)) {
                        a.setPiece(piece);
                    }
                }
                status = "Running";
            } else {
                // Ricevi la mossa dal giocatore corrente
                Move move = null;
                if (obj instanceof Move) {
                    move = (Move) obj;
                }

                assert move != null;
                this.chessboard.movePiece(move);
                if (move.toSquare().getPiece() == null) {
                    move_self = true;
                } else if (move.toSquare().getPiece() instanceof King && Math.abs(move.fromSquare().getFile() - move.toSquare().getFile()) == 2) {
                    castle = true;
                } else {
                    capture = true;
                    this.chessboard.addEatenPiece(move.toSquare().getPiece());
                }
                if (this.chessboard.getSquare(move.toSquare().getRank(),move.toSquare().getFile()).getPiece() instanceof Rook rook) {
                    rook.castle = false;
                } else if (this.chessboard.getSquare(move.toSquare().getRank(),move.toSquare().getFile()).getPiece() instanceof King king) {
                    king.canCastle = false;
                }
                List<Square> allSquares = this.chessboard.getAllSquares();
                for (Square a : allSquares) {
                    if (a != null && chessboard.isOccupiedByOpponent(a, isWhiteTurn ? Color.WHITE : Color.BLACK) && a.getPiece() instanceof Pawn pawn) {
                        pawn.enpassant = false;
                    }
                    if (a != null && a.getPiece() instanceof Pawn && (a.getRank() == 8 || a.getRank() == 1)) {
                        status = "Promo";
                    }
                }
            }

            if (CheckMate()) { running = false; }
        }

    }

    private boolean CheckMate() {
        Color turn = isWhiteTurn ? Color.WHITE : Color.BLACK;
        List<Square> allSquares = this.chessboard.getAllSquares();
        List<Move> allAvailableMoves = new ArrayList<>(); // Inizializza la lista delle mosse disponibili
        Square kingSquare = null;

        for (Square square : allSquares) {
            if (square.getPiece() instanceof King && square.getPiece().getColor() != turn) {
                kingSquare = square;
                break;
            }
        }

        for (Square s : allSquares) {
            if (s.getPiece() != null && s.getPiece().getColor() != turn) {
                s.getPiece().calculatePossibleMoves(this.chessboard, s, kingSquare);
                allAvailableMoves.addAll(s.getPiece().getAvailableMoves());
            }
        }

        if (!allAvailableMoves.isEmpty() && ((King) Objects.requireNonNull(kingSquare).getPiece()).Check(this.chessboard, kingSquare)) {
            audioClip = "file:src/main/img/move-check.wav";
        } else if(capture) {
            audioClip = "file:src/main/img/capture.wav";
        } else if (castle) {
            audioClip = "file:src/main/img/castle.wav";
        } else if (move_self) {
            audioClip = "file:src/main/img/move-self.wav";
        }
        capture = false;
        move_self = false;
        castle = false;

        if (allAvailableMoves.isEmpty() && ((King) Objects.requireNonNull(kingSquare).getPiece()).Check(this.chessboard, kingSquare)) {
            status = turn + " Wins"; // Aggiorna lo stato correttamente
            writeToMongoDB((isWhiteTurn ? blackPlayer : whitePlayer), 1, 0, 0);
            writeToMongoDB((isWhiteTurn ? whitePlayer : blackPlayer), 0, 1, 0);
            audioClip = "file:src/main/img/game-end.wav";
            return true;
        } else if (allAvailableMoves.isEmpty() && !((King) kingSquare.getPiece()).Check(this.chessboard, kingSquare)) {
            status = "Stalemate"; // Aggiorna lo stato correttamente
            writeToMongoDB(whitePlayer, 0, 0, 1);
            writeToMongoDB(blackPlayer, 0, 0, 1);
            audioClip = "file:src/main/img/game-end.wav";
            return true;
        }

        return false;
    }

    private void sendGameStatus() throws IOException {
        ObjectOutputStream whiteOutputStream = new ObjectOutputStream(White.getOutputStream());
        whiteOutputStream.writeObject(status);
        whiteOutputStream.flush();

        ObjectOutputStream blackOutputStream = new ObjectOutputStream(Black.getOutputStream());
        blackOutputStream.writeObject(status);
        blackOutputStream.flush();
    }

    private void sendConnErr(Socket currentSocket) throws IOException {
        ObjectOutputStream currOutputStream = new ObjectOutputStream(currentSocket.getOutputStream());
        currOutputStream.writeObject("conn_err");
        currOutputStream.flush();
    }

    private void writeToMongoDB(String nickname, int win, int loss, int draws) {
        // Ottieni un'istanza del database MongoDB
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = mongoClient.getDatabase("chessGame");

        // Seleziona una collezione nel database
        Document existingDocument = db.getCollection("scoreboard").find(eq("nickname", nickname)).first();

        if (existingDocument != null) {
            int existingWins = existingDocument.getInteger("wins", 0);
            int existingLosses = existingDocument.getInteger("losses", 0);
            int existingDraws = existingDocument.getInteger("draws", 0);

            // Calcola il nuovo punteggio
            int updatedWins = existingWins + win;
            int updatedLosses = existingLosses + loss;
            int updatedDraws = existingDraws + draws;

            // Aggiorna il documento con il nuovo punteggio
            db.getCollection("scoreboard").updateOne(eq("nickname", nickname),
                    new Document("$set", new Document("wins", updatedWins)
                            .append("losses", updatedLosses)
                            .append("draws", updatedDraws)));
        } else {
            // Il documento non esiste, quindi inserisci un nuovo documento
            Document newDocument = new Document("nickname", nickname)
                    .append("wins", win)
                    .append("losses", loss)
                    .append("draws", draws);
            db.getCollection("scoreboard").insertOne(newDocument);
        }

        mongoClient.close();
    }

    private String waitForResponse(Socket clientSocket) throws IOException {
        // Imposta un timeout per la lettura della risposta del client
        try {
            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
            Object receivedObject = inputStream.readObject();
            return isWhiteTurn ? "Black disconnesso" : "White disconnesso";
        } catch (ClassNotFoundException | SocketTimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendScoreboard(Socket clientSocket) throws IOException {
        // Ottieni un'istanza del database MongoDB
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = mongoClient.getDatabase("chessGame");

        // Ottieni tutti i documenti dalla collezione scoreboard
        List<Document> scoreboardDocuments = db.getCollection("scoreboard").find().into(new ArrayList<>());
        scoreboardDocuments.sort(Comparator.comparingInt(doc -> -doc.getInteger("wins")));
        // Chiudi la connessione al database
        mongoClient.close();

        // Invia la scoreboard al client
        ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        outputStream.writeObject(scoreboardDocuments);
        outputStream.flush();
    }

    private void sendSound() throws IOException {
        // Invia il sound effect al client
        ObjectOutputStream whiteOutputStream = new ObjectOutputStream(White.getOutputStream());
        whiteOutputStream.writeObject(audioClip);
        whiteOutputStream.flush();

        ObjectOutputStream blackOutputStream = new ObjectOutputStream(Black.getOutputStream());
        blackOutputStream.writeObject(audioClip);
        blackOutputStream.flush();
    }

}
