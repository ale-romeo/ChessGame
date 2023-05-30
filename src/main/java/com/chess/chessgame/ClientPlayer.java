package com.chess.chessgame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.bson.Document;
import javafx.scene.control.Alert;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import javafx.scene.layout.HBox;

public class ClientPlayer extends Application {
    private String nickname, serverAddress, end;
    private Color color;
    private Socket serverSocket;
    private int serverPort;
    private Chessboard chessboard;
    private Square kingSquare;
    private final List<Circle> highlightedCircles = new ArrayList<>();
    private List<Move> posMoves = new ArrayList<>();
    private boolean myTurn, running = true;
    private volatile boolean wait, newGame;
    private final TableView<ScoreboardEntry> scoreboardTable =new TableView<>();


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        readConfigFromXML();
        primaryStage.setTitle("Benvenuto");

        // Creazione dei controlli
        Label nicknameLabel = new Label("Nickname:");
        TextField nicknameTextField = new TextField();
        Button startButton = new Button("Inizia partita");

        // Azione del pulsante "Inizia partita"
        startButton.setOnAction(event -> {
            nickname = nicknameTextField.getText();
            if (!Objects.equals(nickname, "")) {
                // Avvia il gioco o altre azioni in base alla logica del tuo programma
                startThread(primaryStage);
            }
        });

        // Creazione del layout
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(nicknameLabel, nicknameTextField, startButton);

        // Creazione della scena
        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.getIcons().add(new Image("file:src/main/img/Chess_nlt60.png"));

        // Impostazione della scena primaria
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startThread(Stage primaryStage) {
        Thread gameThread = new Thread(() -> initGame(primaryStage));
        gameThread.start();
    }

    private void initGame(Stage primaryStage) {
        try {
            this.serverSocket = new Socket(serverAddress, serverPort);
            Platform.runLater(() -> {
                Label waitingLabel = new Label("In attesa di un avversario...");
                VBox waitingBox = new VBox(10);
                waitingBox.setPadding(new Insets(10));
                waitingBox.getChildren().add(waitingLabel);
                primaryStage.setTitle("Chess Game - " + nickname + " in attesa");
                primaryStage.setScene(new Scene(waitingBox, 300, 200));
            });
            sendNickname(nickname);
            this.color = receiveColor();

            playGame(primaryStage);
        } catch (IOException e) {
            e.printStackTrace();
            // Gestione dell'errore di connessione
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void playGame(Stage primaryStage) {
        while (running) {
            try {
                this.running = receiveStatus();
                if (!running) {
                    break;
                }
                this.chessboard = receiveChessboard();
                this.myTurn = receiveTurn();

                if (Objects.equals(end, "Promo") && myTurn) {
                    Platform.runLater(this::promoAlert);
                    continue;
                }
                Platform.runLater(() -> {
                    GridPane gridPane = createChessboard();
                    Button surrButton = new Button("Resa");

                    surrButton.setOnAction(event -> {
                        sendSurr();
                        wait = false;
                        running = false;
                    });

                    VBox root = new VBox(gridPane, surrButton);
                    root.setAlignment(Pos.CENTER);
                    root.setSpacing(10);
                    primaryStage.setTitle("Chess Game - " + nickname + " - " + this.color);
                    primaryStage.setScene(new Scene(root, 450, 500));
                    displayChessboardInGame(gridPane);
                });
                if (myTurn) {
                    while (wait) Thread.onSpinWait();
                    wait = true;
                }
            } catch (EOFException e) {
                showConnErr(primaryStage);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        if (((this.color == Color.WHITE) && Objects.equals(end, "WHITE Wins")) || ((this.color == Color.BLACK) && Objects.equals(end, "BLACK Wins"))) {
            showWin(primaryStage);
        } else if (((this.color == Color.BLACK) && Objects.equals(end, "WHITE Wins")) || ((this.color == Color.WHITE) && Objects.equals(end, "BLACK Wins"))) {
            showLose(primaryStage);
        } else if (Objects.equals(end, "conn_err")) {
            showWin(primaryStage);
        }
    }

    private void showScoreboard(Stage primaryStage) {
        try {
            List<Document> scoreboardDocuments = receiveScoreboard();
            List<ScoreboardEntry> scoreboardEntries = new ArrayList<>();
            for (Document document : scoreboardDocuments) {
                String nickname = document.getString("nickname");
                int wins = document.getInteger("wins");
                int losses = document.getInteger("losses");
                int draws = document.getInteger("draws");

                scoreboardEntries.add(new ScoreboardEntry(nickname, wins, losses, draws));
            }
            // Aggiorna la TableView con i dati della scoreboard
            scoreboardTable.getItems().setAll(scoreboardEntries);
            //this.scoreboard = receiveScoreboard();
            Platform.runLater(() -> {
                Label scoreLabel = new Label("Classifica:");
                Button newGameButton = new Button("Nuova Partita");

                // Creazione delle colonne della scoreboard
                TableColumn<ScoreboardEntry, String> nicknameColumn = new TableColumn<>("Nickname");
                TableColumn<ScoreboardEntry, Integer> winsColumn = new TableColumn<>("Wins");
                TableColumn<ScoreboardEntry, Integer> lossesColumn = new TableColumn<>("Losses");
                TableColumn<ScoreboardEntry, Integer> drawsColumn = new TableColumn<>("Draws");

                // Associazione delle proprietà dei dati alle colonne
                nicknameColumn.setCellValueFactory(new PropertyValueFactory<>("nickname"));
                winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));
                lossesColumn.setCellValueFactory(new PropertyValueFactory<>("losses"));
                drawsColumn.setCellValueFactory(new PropertyValueFactory<>("draws"));

                // Aggiungi le colonne alla TableView
                scoreboardTable.getColumns().add(nicknameColumn);
                scoreboardTable.getColumns().add(winsColumn);
                scoreboardTable.getColumns().add(lossesColumn);
                scoreboardTable.getColumns().add(drawsColumn);

                nicknameColumn.setPrefWidth(115);
                winsColumn.setPrefWidth(80);
                lossesColumn.setPrefWidth(80);
                drawsColumn.setPrefWidth(80);
                scoreboardTable.autosize();

                newGameButton.setOnAction(event -> {
                    // Riavvia la partita o altre azioni in base alla logica del tuo programma
                    newGame = true;
                });

                VBox scoreBox = new VBox(10);
                scoreBox.setPadding(new Insets(10));
                scoreBox.getChildren().addAll(scoreLabel, scoreboardTable, newGameButton);
                primaryStage.setTitle("Chess Game - Classifica");
                primaryStage.setScene(new Scene(scoreBox, 400, 300));
            });
        } catch (IOException e) {
            e.printStackTrace();
            // Gestione dell'errore di connessione
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void readConfigFromXML() {
        ConfigReader configReader = new ConfigReader();
        configReader.readConfigFromXML("config.xml");
        this.serverAddress = configReader.getServerAddress();
        this.serverPort = configReader.getServerPort();
    }

    private Color receiveColor() throws IOException, ClassNotFoundException {
        ObjectInputStream serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
        return (Color) serverInputStream.readObject();
    }

    private boolean receiveTurn() throws IOException, ClassNotFoundException {
        ObjectInputStream serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
        return serverInputStream.readObject() == this.color;
    }

    private boolean receiveStatus() throws IOException, ClassNotFoundException {
        ObjectInputStream serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
        end = (String) serverInputStream.readObject();
        return (end).equals("Running") || (end).equals("Promo");
    }

    private Chessboard receiveChessboard() throws IOException, ClassNotFoundException {
        ObjectInputStream serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
        Object receivedObject = serverInputStream.readObject();

        if (receivedObject instanceof Chessboard) {
            return (Chessboard) receivedObject;
        } else {
            return null;
        }
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

        // Inserimento dei numeri accanto a ogni riga
        for (int rank = 1; rank <= 8; rank++) {
            Label label = new Label(String.valueOf(rank));
            label.setStyle("-fx-font-weight: bold;");
            gridPane.add(label, 8, rank);
        }

        // Creazione delle caselle della scacchiera come rettangoli colorati
        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'A'; file <= 'H'; file++) {
                Rectangle square = new Rectangle(50, 50, (rank + ('H' - file)) % 2 != 0 ? javafx.scene.paint.Color.WHITE : javafx.scene.paint.Color.LIGHTGRAY);
                gridPane.add(square, 'H' - file, rank);
            }
        }

        return gridPane;
    }

    private void displayChessboardInGame(GridPane gridPane) {
        List<Square> allSquares = this.chessboard.getAllSquares();
        for (Square square : allSquares) {
            if (square.getPiece() instanceof King && square.getPiece().getColor() == this.color) {
                kingSquare = square;
                break;
            }
        }

        // Posizionamento dei pezzi sulla scacchiera grafica
        for (Square square : allSquares) {
            Piece piece = square.getPiece();
            if (piece != null) {
                // Creazione del componente grafico per il pezzo (ad esempio, ImageView)
                ImageView pieceImageView = createPieceImageView(piece);

                if (piece.getColor() == this.color){
                    pieceImageView.setOnMouseClicked(event -> {
                        // Nuovo pezzo selezionato
                        clearHighlightedMoves(gridPane);
                        posMoves.clear();
                        // Ottenere le possibili mosse del pezzo
                        piece.calculatePossibleMoves(this.chessboard, square, kingSquare);
                        posMoves = piece.getAvailableMoves();
                        if (posMoves != null) {
                            // Mostrare le possibili mosse (ad esempio, evidenziando le caselle)
                            highlightPossibleMoves(gridPane);
                        }
                    });

                }

                // Posizionamento del componente grafico nel GridPane
                gridPane.add(pieceImageView, 'H' - square.getFile(), square.getRank());
            }
        }
    }

    private void highlightPossibleMoves(GridPane gridPane) {
        clearHighlightedMoves(gridPane);

        for (Move move : posMoves) {
            int targetRank = move.toSquare().getRank();
            char targetFile = move.toSquare().getFile();

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

    private void promoAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Promozione");
        alert.setHeaderText("Seleziona pezzo:");

        ImageView wqueenImageView = new ImageView(new Image("file:src/main/img/Chess_qlt60.png"));
        ImageView bqueenImageView = new ImageView(new Image("file:src/main/img/Chess_qdt60.png"));
        ImageView wrookImageView = new ImageView(new Image("file:src/main/img/Chess_rlt60.png"));
        ImageView brookImageView = new ImageView(new Image("file:src/main/img/Chess_rdt60.png"));
        ImageView wbishopImageView = new ImageView(new Image("file:src/main/img/Chess_blt60.png"));
        ImageView bbishopImageView = new ImageView(new Image("file:src/main/img/Chess_bdt60.png"));
        ImageView wknightImageView = new ImageView(new Image("file:src/main/img/Chess_nlt60.png"));
        ImageView bknightImageView = new ImageView(new Image("file:src/main/img/Chess_ndt60.png"));


        // Aggiunta dei bottoni di scelta per i tipi di pezzo
        HBox hbox = new HBox(10);
        alert.getDialogPane().setContent(hbox);
        // Aggiunta dei bottoni di scelta per i tipi di pezzo
        alert.getButtonTypes().setAll(ButtonType.OK);
        if (this.color == Color.WHITE) {
            hbox.getChildren().addAll(wqueenImageView, wrookImageView, wbishopImageView, wknightImageView);

            wqueenImageView.setOnMouseClicked(event -> {
                sendPromo(new Queen(this.color));
                alert.setResult(ButtonType.OK); // Imposta il risultato dell'Alert su OK
            });
            wrookImageView.setOnMouseClicked(event -> {
                sendPromo(new Rook(this.color));
                alert.setResult(ButtonType.OK); // Imposta il risultato dell'Alert su OK
            });
            wbishopImageView.setOnMouseClicked(event -> {
                sendPromo(new Bishop(this.color));
                alert.setResult(ButtonType.OK); // Imposta il risultato dell'Alert su OK
            });
            wknightImageView.setOnMouseClicked(event -> {
                sendPromo(new Knight(this.color));
                alert.setResult(ButtonType.OK); // Imposta il risultato dell'Alert su OK
            });

        } else {
            hbox.getChildren().addAll(bqueenImageView, brookImageView, bbishopImageView, bknightImageView);
            bqueenImageView.setOnMouseClicked(event -> {
                sendPromo(new Queen(this.color));
                alert.setResult(ButtonType.OK); // Imposta il risultato dell'Alert su OK
            });
            brookImageView.setOnMouseClicked(event -> {
                sendPromo(new Rook(this.color));
                alert.setResult(ButtonType.OK); // Imposta il risultato dell'Alert su OK
            });
            bbishopImageView.setOnMouseClicked(event -> {
                sendPromo(new Bishop(this.color));
                alert.setResult(ButtonType.OK); // Imposta il risultato dell'Alert su OK
            });
            bknightImageView.setOnMouseClicked(event -> {
                sendPromo(new Knight(this.color));
                alert.setResult(ButtonType.OK); // Imposta il risultato dell'Alert su OK
            });

        }
        // Mostra il popup e attende la selezione dell'utente
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                alert.close();
            }
        });
    }

    private void showWin(Stage primaryStage) {
        Platform.runLater(() -> {
            Label victoryLabel = new Label("Hai vinto!");
            Button scoreboardButton = new Button("Visualizza classifica");
            Button newGameButton = new Button("Nuova Partita");

            scoreboardButton.setOnAction(event -> {
                // Riavvia la partita o altre azioni in base alla logica del tuo programma
                showScoreboard(primaryStage);
            });
            // Azione del pulsante "Nuova partita"
            newGameButton.setOnAction(event -> {
                // Riavvia la partita o altre azioni in base alla logica del tuo programma
                newGame = true;
            });

            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(10));
            vbox.getChildren().addAll(victoryLabel, scoreboardButton, newGameButton);

            primaryStage.setTitle("Vittoria");
            primaryStage.setScene(new Scene(vbox, 300, 200));
        });
        while (!newGame) Thread.onSpinWait();
        newGame = false;
        try {
            this.serverSocket.close();
            running = true;
            initGame(primaryStage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showLose(Stage primaryStage) {
        Platform.runLater(() -> {
            Label victoryLabel = new Label("Hai perso!");
            Button scoreboardButton = new Button("Visualizza classifica");
            Button newGameButton = new Button("Nuova Partita");

            scoreboardButton.setOnAction(event -> {
                // Riavvia la partita o altre azioni in base alla logica del tuo programma
                showScoreboard(primaryStage);
            });
            // Azione del pulsante "Nuova partita"
            newGameButton.setOnAction(event -> {
                // Riavvia la partita o altre azioni in base alla logica del tuo programma
                newGame = true;
            });

            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(10));
            vbox.getChildren().addAll(victoryLabel, scoreboardButton, newGameButton);

            primaryStage.setTitle("Sconfitta");
            primaryStage.setScene(new Scene(vbox, 300, 200));
        });
        while (!newGame) Thread.onSpinWait();
        newGame = false;
        try {
            this.serverSocket.close();
            running = true;
            initGame(primaryStage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showConnErr(Stage primaryStage) {
        Platform.runLater(() -> {
            Label victoryLabel = new Label("Connessione persa con l'avversario");
            Button newGameButton = new Button("Nuova Partita");

            // Azione del pulsante "Nuova partita"
            newGameButton.setOnAction(event -> {
                // Riavvia la partita o altre azioni in base alla logica del tuo programma
                newGame = true;
            });

            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(10));
            vbox.getChildren().addAll(victoryLabel, newGameButton);

            primaryStage.setTitle("Fine partita");
            primaryStage.setScene(new Scene(vbox, 300, 200));
        });
        while (!newGame) Thread.onSpinWait();
        newGame = false;
        try {
            this.serverSocket.close();
            initGame(primaryStage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ImageView createPieceImageView(Piece piece) {
        // Esempio di creazione d'ImageView per i pezzi
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

    private void sendMove(Move move) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
            outputStream.writeObject(move);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // Gestione dell'errore d'invio della mossa al server
        }
    }

    private void sendPromo(Piece piece) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
            outputStream.writeObject(piece);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // Gestione dell'errore d'invio della mossa al server
        }
    }

    private void sendSurr() {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
            outputStream.writeObject("Surrender");
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // Gestione dell'errore d'invio della mossa al server
        }
    }

    private List<Document> receiveScoreboard() throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = new ObjectInputStream(serverSocket.getInputStream());
        return (List<Document>) inputStream.readObject();
    }
}
