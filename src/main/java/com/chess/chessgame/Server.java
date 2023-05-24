package com.chess.chessgame;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private List<Socket> waitingClients;

    public Server() {
        clients = new ArrayList<ClientHandler>();
        waitingClients = new ArrayList<Socket>();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(12345); // Port number for server

            System.out.println("Server avviato. In attesa di connessioni...");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Handling new client connection
                handleClientConnection(clientSocket);
            }
        } catch (IOException e) {
            System.out.println("Errore durante l'avvio del server: " + e.getMessage());
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.out.println("Errore durante la chiusura del serverSocket: " + e.getMessage());
                }
            }
        }
    }

    private void handleClientConnection(Socket clientSocket) {
        if (waitingClients.size() > 0) {
            // If there are waiting clients, match the new client with a waiting client
            Socket waitingClient = waitingClients.remove(0);
            ClientHandler clientHandler = new ClientHandler(clientSocket, waitingClient);
            clients.add(clientHandler);
            clientHandler.run();
            System.out.println("Partita avviata tra " + clientSocket.getInetAddress() +
                    " e " + waitingClient.getInetAddress());
        } else {
            // If there are no waiting clients, add the new client to the waiting list
            waitingClients.add(clientSocket);
            System.out.println("Client " + clientSocket.getInetAddress() + " in attesa di un avversario");
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
