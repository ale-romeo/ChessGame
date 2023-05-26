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
            sendChessboard(White, this.chessboard);
            sendChessboard(Black, this.chessboard);

        } catch (IOException e) {
            System.out.println("Errore durante l'esecuzione del client handler: " + e.getMessage());
        }
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
