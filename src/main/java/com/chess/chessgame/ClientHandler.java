package com.chess.chessgame;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

public class ClientHandler implements Runnable {
    private Socket White;
    private Socket Black;
    private Server server;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Chessboard chessboard;

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
            sendColor(White, Color.WHITE);
            sendColor(Black, Color.BLACK);

            playGame(White, Black);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Errore durante l'esecuzione del client handler: " + e.getMessage());
        }
    }

    private void sendColor(Socket clientSocket, Color color) throws IOException {
        ObjectOutputStream clientOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        clientOutputStream.writeObject(color);
        clientOutputStream.flush();
    }

    private void sendChessboard(ObjectOutputStream outputStream) throws IOException {
        // Invia la scacchiera al giocatore tramite l'output stream
        outputStream.writeObject(this.chessboard);
        outputStream.flush();
    }


    private void playGame(Socket white, Socket black) throws IOException, ClassNotFoundException {
        // Crea gli stream di input/output per i giocatori bianco e nero
        ObjectInputStream whiteInputStream = new ObjectInputStream(white.getInputStream());
        ObjectOutputStream whiteOutputStream = new ObjectOutputStream(white.getOutputStream());

        ObjectInputStream blackInputStream = new ObjectInputStream(black.getInputStream());
        ObjectOutputStream blackOutputStream = new ObjectOutputStream(black.getOutputStream());

        // Invia la scacchiera iniziale ai giocatori
        sendChessboard(whiteOutputStream);
        sendChessboard(blackOutputStream);

        // Inizializza il giocatore corrente come bianco
        ObjectInputStream currentPlayerInputStream = whiteInputStream;
        ObjectOutputStream currentPlayerOutputStream = whiteOutputStream;

        while (true) {
            // Ricevi la mossa dal giocatore corrente
            Move move = (Move) currentPlayerInputStream.readObject();

            // Valida la mossa e aggiorna la scacchiera
            if (isValidMove(move)) {
                updateChessboard(move);
                sendMoveToOpponent(move, currentPlayerOutputStream);
            } else {
                // Invia un messaggio di errore al giocatore che ha fatto una mossa non valida
                sendErrorMessage(currentPlayerOutputStream, "Mossa non valida. Riprova.");
            }

            // Controlla se il giocatore corrente ha vinto
            if (hasCurrentPlayerWon()) {
                // Invia un messaggio di vittoria al giocatore corrente
                sendVictoryMessage(currentPlayerOutputStream);
                break; // Termina il gioco
            }

            // Passa al giocatore successivo
            currentPlayerInputStream = (currentPlayerInputStream == whiteInputStream) ? blackInputStream : whiteInputStream;
            currentPlayerOutputStream = (currentPlayerOutputStream == whiteOutputStream) ? blackOutputStream : whiteOutputStream;
        }
    }

    public boolean isValidMove(Move move) {

    }


    public void receiveMove() {

    }

    public void confirmMove() throws IOException {
        outputStream.writeObject(true);
        outputStream.flush();
    }

    public void sendMove(Piece piece, Square from, Square to, ClientPlayer player) throws IOException {

    }

    public void sendUpdate() throws IOException {
        outputStream.writeObject(chessboard);
        outputStream.flush();
    }

    public void removePiece() {
        // Logica per rimuovere una pedina dalla scacchiera
    }
}
