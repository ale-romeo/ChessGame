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
    private boolean isWhiteTurn = true;

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
            while (true) {
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
        } catch (IOException e) {
            System.out.println("Errore durante l'esecuzione del client handler: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveNickname(Socket whiteSocket, Socket blackSocket) throws IOException, ClassNotFoundException {
        ObjectInputStream whiteInputStream = new ObjectInputStream(whiteSocket.getInputStream());
        String whitePlayer = (String) whiteInputStream.readObject();
        ObjectInputStream blackInputStream = new ObjectInputStream(blackSocket.getInputStream());
        String blackPlayer = (String) blackInputStream.readObject();

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
    }

    private void sendGameStatus(Socket whiteSocket, Socket blackSocket) throws IOException {
        ObjectOutputStream whiteOutputStream = new ObjectOutputStream(whiteSocket.getOutputStream());
        whiteOutputStream.writeObject("Running");
        whiteOutputStream.flush();

        ObjectOutputStream blackOutputStream = new ObjectOutputStream(blackSocket.getOutputStream());
        blackOutputStream.writeObject("Running");
        blackOutputStream.flush();
    }
}
