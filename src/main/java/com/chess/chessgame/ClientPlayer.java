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
    private String serverAddress;
    private int serverPort;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        readConfigFromXML("config.xml");
        primaryStage.setTitle("Benvenuto");

        // Creazione dei controlli
        Label nicknameLabel = new Label("Nickname:");
        TextField nicknameTextField = new TextField();
        Button startButton = new Button("Inizia partita");

        // Azione del pulsante "Inizia partita"
        startButton.setOnAction(event -> {
            nickname = nicknameTextField.getText();
            Label waitingLabel = new Label("In attesa di un avversario...");
            // Avvia il gioco o altre azioni in base alla logica del tuo programma
            startGame(nickname);
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

    private void startGame(String nickname) {
        Thread connectionThread = new Thread(() -> {
            try {
                Socket serverSocket = new Socket(serverAddress, serverPort);
                //sendNickname(socket, nickname);
                Color color = receiveColor(serverSocket);
                Chessboard chessboard = receiveChessboard(serverSocket);
                // Altri passaggi per la gestione della partita
            } catch (IOException e) {
                e.printStackTrace();
                // Gestione dell'errore di connessione
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        connectionThread.start();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    private void sendNickname(Socket socket, String nickname) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(nickname);
        outputStream.flush();
    }

    private void readConfigFromXML(String filePath) {
        ConfigReader configReader = new ConfigReader();
        configReader.readConfigFromXML(filePath);
        this.serverAddress = configReader.getServerAddress();
        this.serverPort = configReader.getServerPort();
    }

    private Color receiveColor(Socket serverSocket) throws IOException, ClassNotFoundException {
        ObjectInputStream serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
        return (Color) serverInputStream.readObject();
    }

    private Chessboard receiveChessboard(Socket serverSocket) throws IOException, ClassNotFoundException {
        ObjectInputStream serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
        return (Chessboard) serverInputStream.readObject();
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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
