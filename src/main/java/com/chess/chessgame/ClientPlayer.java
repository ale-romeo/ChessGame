package com.chess.chessgame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;


public class ClientPlayer extends Application {
    private String nickname;
    private Color color;
    private Socket serverSocket;
    private String serverAddress;
    private int serverPort;
    private Chessboard chessboard;
    private ObjectInputStream inputStream;
    private List<Circle> highlightedCircles = new ArrayList<>();
    private List<Move> posMoves = new ArrayList<>();
    public boolean myTurn;
    public boolean gameStatus = true;
    public boolean wait;


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
            initGame(primaryStage, nickname);
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

    private void initGame(Stage primaryStage, String nickname) {
        Thread gameThread = new Thread(() -> {
            try {
                this.serverSocket = new Socket(serverAddress, serverPort);
                sendNickname(nickname);
                this.color = receiveColor();

                playGame(primaryStage);
            } catch (IOException e) {
                e.printStackTrace();
                // Gestione dell'errore di connessione
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        gameThread.start();
    }

    private void playGame(Stage primaryStage) {
            while (gameStatus) {
                try {
                    this.gameStatus = receiveStatus();
                    this.chessboard = receiveChessboard();
                    this.myTurn = receiveTurn();
                    Platform.runLater(() -> {
                        GridPane gridPane = createChessboard();

                        primaryStage.setTitle("Chess Game - " + nickname);
                        primaryStage.setScene(new Scene(gridPane, 450, 450));
                        displayChessboardInGame(gridPane);
                    });
                    if (myTurn) {
                        while (wait);
                        wait = true;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
    }

    private void readConfigFromXML(String filePath) {
        ConfigReader configReader = new ConfigReader();
        configReader.readConfigFromXML(filePath);
        this.serverAddress = configReader.getServerAddress();
        this.serverPort = configReader.getServerPort();
    }

    private Color receiveColor() throws IOException, ClassNotFoundException {
        ObjectInputStream serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
        return (Color) serverInputStream.readObject();
    }

    private boolean receiveTurn() throws IOException, ClassNotFoundException {
        ObjectInputStream serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
        return (Color) serverInputStream.readObject() == this.color;
    }

    private boolean receiveStatus() throws IOException, ClassNotFoundException {
        ObjectInputStream serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
        return ((String) serverInputStream.readObject()).equals("Running");
    }

    private Chessboard receiveChessboard() throws IOException, ClassNotFoundException {
        ObjectInputStream serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
        return (Chessboard) serverInputStream.readObject();
    }

    private GridPane createChessboard() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(2);
        gridPane.setVgap(2);

        // Inserimento delle lettere sotto ogni colonna
        for (char file = 'H'; file >= 'A'; file--) {
            Label label = new Label(String.valueOf(file));
            label.setStyle("-fx-font-weight: bold;");
            gridPane.add(label, 'H' - file, 9);
        }

        // Inserimento dei numeri accanto ad ogni riga
        for (int rank = 1; rank <= 8; rank++) {
            Label label = new Label(String.valueOf(rank));
            label.setStyle("-fx-font-weight: bold;");
            gridPane.add(label, 8, rank);
        }

        // Creazione delle caselle della scacchiera come rettangoli colorati
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'A'; file <= 'H'; file++) {
                Rectangle square = new Rectangle(50, 50, (rank + ('H' - file)) % 2 == 0 ? javafx.scene.paint.Color.WHITE : javafx.scene.paint.Color.LIGHTGRAY);
                gridPane.add(square, 'H' - file, rank);
            }
        }

        return gridPane;
    }

    private void displayChessboardInGame(GridPane gridPane) {
        // Posizionamento dei pezzi sulla scacchiera grafica
        for (char file = 'A'; file <= 'H'; file++) {
            for (int rank = 1; rank <= 8; rank++) {
                Square currSquare = this.chessboard.getSquare(rank, file);
                Piece piece = currSquare.getPiece();
                if (piece != null) {
                    // Creazione del componente grafico per il pezzo (ad esempio, ImageView)
                    ImageView pieceImageView = createPieceImageView(piece);

                    if (piece.getColor() == this.color){
                        pieceImageView.setOnMouseClicked(event -> {
                            // Nuovo pezzo selezionato
                            clearHighlightedMoves(gridPane);
                            posMoves.clear();
                            // Ottenere le possibili mosse del pezzo
                            piece.clearAvailableMoves();
                            piece.calculatePossibleMoves(this.chessboard, currSquare);
                            posMoves = piece.getAvailableMoves();
                            if (posMoves != null) {
                                // Mostrare le possibili mosse (ad esempio, evidenziando le caselle)
                                highlightPossibleMoves(gridPane);
                            }
                        });

                    }

                    // Posizionamento del componente grafico nel GridPane
                    gridPane.add(pieceImageView, 'H' - file, rank);
                }
            }
        }
    }

    private void highlightPossibleMoves(GridPane gridPane) {
        clearHighlightedMoves(gridPane);

        for (Move move : posMoves) {
            int targetRank = move.getToSquare().getRank();
            char targetFile = move.getToSquare().getFile();

            Circle targetCircle = new Circle(8, javafx.scene.paint.Color.LIGHTGREEN);
            GridPane.setHalignment(targetCircle, HPos.CENTER); // Centra il cerchio orizzontalmente
            GridPane.setValignment(targetCircle, VPos.CENTER);
            GridPane.setColumnIndex(targetCircle, 'H' - targetFile);
            GridPane.setRowIndex(targetCircle, targetRank);

            targetCircle.setOnMouseClicked(event -> {
                if (myTurn) {
                    sendMove(move);
                    wait = false;
                }
            });

            gridPane.getChildren().add(targetCircle);
            highlightedCircles.add(targetCircle);
        }
    }

    private void clearHighlightedMoves(GridPane gridPane) {
        for (Circle circle : highlightedCircles) {
            gridPane.getChildren().remove(circle);
        }
        highlightedCircles.clear();
    }

    private ImageView createPieceImageView(Piece piece) {
        // Esempio di creazione di ImageView per i pezzi
        ImageView imageView = new ImageView();

        // Imposta l'immagine del pezzo in base al tipo e al colore
        if (piece instanceof Pawn) {
            if (piece.getColor() == Color.WHITE) {
                imageView.setImage(new Image("file:src/main/img/Chess_plt60.png"));

            } else {
                imageView.setImage(new Image("file:src/main/img/Chess_pdt60.png"));
            }
        } else if (piece instanceof Rook) {
            if (piece.getColor() == Color.WHITE) {
                imageView.setImage(new Image("file:src/main/img/Chess_rlt60.png"));
            } else {
                imageView.setImage(new Image("file:src/main/img/Chess_rdt60.png"));
            }
        } else if (piece instanceof Queen) {
            if (piece.getColor() == Color.WHITE) {
                imageView.setImage(new Image("file:src/main/img/Chess_qlt60.png"));
            } else {
                imageView.setImage(new Image("file:src/main/img/Chess_qdt60.png"));
            }
        } else if (piece instanceof King) {
            if (piece.getColor() == Color.WHITE) {
                imageView.setImage(new Image("file:src/main/img/Chess_klt60.png"));
            } else {
                imageView.setImage(new Image("file:src/main/img/Chess_kdt60.png"));
            }
        } else if (piece instanceof Bishop) {
            if (piece.getColor() == Color.WHITE) {
                imageView.setImage(new Image("file:src/main/img/Chess_blt60.png"));
            } else {
                imageView.setImage(new Image("file:src/main/img/Chess_bdt60.png"));
            }
        } else if (piece instanceof Knight) {
            if (piece.getColor() == Color.WHITE) {
                imageView.setImage(new Image("file:src/main/img/Chess_nlt60.png"));
            } else {
                imageView.setImage(new Image("file:src/main/img/Chess_ndt60.png"));
            }
        }

        // Imposta la dimensione dell'ImageView (ad esempio, 50x50)
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);

        return imageView;
    }

    private void sendNickname(String nickname) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
        outputStream.writeObject(nickname);
        outputStream.flush();
    }

    public Color getColor() {
        return color;
    }

    public void sendMove(Move move) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
            outputStream.writeObject(move);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // Gestione dell'errore di invio della mossa al server
        }
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
