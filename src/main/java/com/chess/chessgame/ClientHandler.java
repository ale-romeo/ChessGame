package com.chess.chessgame;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.all;
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
            receiveNickname(White, Black);

            sendColor(White, Color.WHITE);
            sendColor(Black, Color.BLACK);
            while (running) {
                sendGameStatus(White, Black);
                sendChessboard(White, this.chessboard);
                sendChessboard(Black, this.chessboard);
                sendTurn(White, Black, isWhiteTurn);
                if (isWhiteTurn) {
                    handlePlayerTurn(White);
                    isWhiteTurn = false;
                } else {
                    handlePlayerTurn(Black);
                    isWhiteTurn = true;
                }
            }
            sendGameStatus(White, Black);
            sendScoreboard(White);
            sendScoreboard(Black);

        } catch (IOException e) {
            System.out.println("Errore durante l'esecuzione del client handler: " + e.getMessage());
            try {
                boolean flag = waitForResponse(White);
                writeToMongoDB(whitePlayer, 1, 0);
                writeToMongoDB(blackPlayer, 0, 1);
                sendConnErr(White);
                sendScoreboard(White);
            } catch (IOException ex) {
                writeToMongoDB(blackPlayer, 1, 0);
                writeToMongoDB(whitePlayer, 0, 1);
                try {
                    sendConnErr(Black);
                    sendScoreboard(Black);
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

    private void receiveNickname(Socket whiteSocket, Socket blackSocket) throws IOException, ClassNotFoundException {
        ObjectInputStream whiteInputStream = new ObjectInputStream(whiteSocket.getInputStream());
        whitePlayer = (String) whiteInputStream.readObject();
        ObjectInputStream blackInputStream = new ObjectInputStream(blackSocket.getInputStream());
        blackPlayer = (String) blackInputStream.readObject();

        System.out.println("Inizio partita tra " + whitePlayer + " e " + blackPlayer);
    }

    private void sendColor(Socket clientSocket, Color color) throws IOException {
        ObjectOutputStream clientOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        clientOutputStream.writeObject(color);
        clientOutputStream.flush();
    }

    private void sendChessboard(Socket clientSocket, Chessboard chessboard) throws IOException {
        ObjectOutputStream clientOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        clientOutputStream.writeObject(chessboard);
        clientOutputStream.flush();
    }

    private void sendTurn(Socket whiteSocket, Socket blackSocket, boolean turn) throws IOException {
        ObjectOutputStream whiteOutputStream = new ObjectOutputStream(whiteSocket.getOutputStream());
        whiteOutputStream.writeObject(turn ? Color.WHITE : Color.BLACK);
        whiteOutputStream.flush();

        ObjectOutputStream blackOutputStream = new ObjectOutputStream(blackSocket.getOutputStream());
        blackOutputStream.writeObject(turn ? Color.WHITE : Color.BLACK);
        blackOutputStream.flush();
    }

    private void handlePlayerTurn(Socket currentPlayerSocket) throws IOException, ClassNotFoundException {
        // Ricevi la mossa dal giocatore corrente
        ObjectInputStream currentPlayerInputStream = new ObjectInputStream(currentPlayerSocket.getInputStream());
        Move move = (Move) currentPlayerInputStream.readObject();

        this.chessboard.movePiece(move);
        if (CheckMate()) { running = false; }
    }

    private boolean CheckMate() {
        List<Square> allSquares = this.chessboard.getAllSquares();
        List<Move> allAvailableMoves = new ArrayList<>(); // Inizializza la lista delle mosse disponibili
        Square kingSquare = null;

        for (Square square : allSquares) {
            if (square.getPiece() instanceof King && (square.getPiece().getColor() == (Color.WHITE)) == isWhiteTurn) {
                kingSquare = square;
                break;
            }
        }

        for (Square square : allSquares) {
            if (square.getPiece() != null && (square.getPiece().getColor() == (Color.WHITE)) == isWhiteTurn) {
                square.getPiece().calculatePossibleMoves(this.chessboard, square, kingSquare);
                allAvailableMoves.addAll(square.getPiece().getAvailableMoves());
            }
        }

        if (allAvailableMoves.isEmpty() && ((King) kingSquare.getPiece()).Check(this.chessboard, kingSquare)) {
            status = (!isWhiteTurn ? "Black" : "White") + " Wins"; // Aggiorna lo stato correttamente
            writeToMongoDB((isWhiteTurn ? blackPlayer : whitePlayer), 1, 0);
            writeToMongoDB((isWhiteTurn ? whitePlayer : blackPlayer), 0, 1);
            return true;
        } else if (allAvailableMoves.isEmpty() && !((King) kingSquare.getPiece()).Check(this.chessboard, kingSquare)) {
            status = "Stalemate"; // Aggiorna lo stato correttamente
            return true;
        }

        return false;
    }

    private void sendGameStatus(Socket whiteSocket, Socket blackSocket) throws IOException {
        ObjectOutputStream whiteOutputStream = new ObjectOutputStream(whiteSocket.getOutputStream());
        whiteOutputStream.writeObject(status);
        whiteOutputStream.flush();

        ObjectOutputStream blackOutputStream = new ObjectOutputStream(blackSocket.getOutputStream());
        blackOutputStream.writeObject(status);
        blackOutputStream.flush();
    }

    private void sendConnErr(Socket currentSocket) throws IOException {
        ObjectOutputStream currOutputStream = new ObjectOutputStream(currentSocket.getOutputStream());
        currOutputStream.writeObject("conn_err");
        currOutputStream.flush();
    }

    private void writeToMongoDB(String nickname, int win, int loss) {
        // Ottieni un'istanza del database MongoDB
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = mongoClient.getDatabase("chessGame");

        // Seleziona una collezione nel database
        Document existingDocument = db.getCollection("scoreboard").find(eq("nickname", nickname)).first();

        if (existingDocument != null) {
            int existingWins = existingDocument.getInteger("wins", 0);
            int existingLosses = existingDocument.getInteger("losses", 0);

            // Calcola il nuovo punteggio
            int updatedWins = existingWins + win;
            int updatedLosses = existingLosses + loss;

            // Aggiorna il documento con il nuovo punteggio
            db.getCollection("scoreboard").updateOne(eq("nickname", nickname),
                    new Document("$set", new Document("wins", updatedWins)
                            .append("losses", updatedLosses)));
        } else {
            // Il documento non esiste, quindi inserisci un nuovo documento
            Document newDocument = new Document("nickname", nickname)
                    .append("wins", win)
                    .append("losses", loss);
            db.getCollection("scoreboard").insertOne(newDocument);
        }

        mongoClient.close();
    }

    private boolean waitForResponse(Socket clientSocket) throws IOException {
        // Imposta un timeout per la lettura della risposta del client
        try {
            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
            Object receivedObject = inputStream.readObject();
            if (receivedObject instanceof Move) {
                return true;
            } else {
                return receivedObject.equals("ping");
            }
        } catch (ClassNotFoundException | SocketTimeoutException e) {
            return false;
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

}
