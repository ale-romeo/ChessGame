package com.chess.chessgame;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ClientPlayer extends Application {
    private String nickname;
    private String color;
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ClientPlayer client = new ClientPlayer();
        client.connect("localhost", 12345); // Indirizzo del server e numero di porta

        primaryStage.setTitle("Benvenuto");

        // Creazione dei controlli
        Label nicknameLabel = new Label("Nickname:");
        TextField nicknameTextField = new TextField();
        Button startButton = new Button("Inizia partita");

        // Azione del pulsante "Inizia partita"
        startButton.setOnAction(event -> {
            nickname = nicknameTextField.getText();
            // Avvia il gioco o altre azioni in base alla logica del tuo programma
            System.out.println("Partita iniziata con il nickname: " + nickname);
        });

        // Creazione del layout
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(nicknameLabel, nicknameTextField, startButton);

        // Creazione della scena
        Scene scene = new Scene(vbox, 300, 200);

        // Impostazione della scena primaria
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void connect(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            System.out.println("Connessione al server stabilita.");

            while (true) {
                inputStream = new ObjectInputStream(socket.getInputStream());
                outputStream = new ObjectOutputStream(socket.getOutputStream());
            }

            // Logica per gestire la comunicazione con il server

        } catch (IOException e) {
            System.out.println("Errore durante la connessione al server: " + e.getMessage());
        }
    }

    public void sendMove(Move move) {
        // Logica per inviare una mossa al server
    }

    public void receiveUpdate() {
        // Logica per ricevere l'aggiornamento dal server
    }

    public void receiveGameOver() {
        // Logica per ricevere la notifica di fine gioco dal server
    }

    public void disconnect() {
        // Logica per disconnettersi dal server
    }
}
