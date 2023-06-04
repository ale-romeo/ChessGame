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
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

public class ClientPlayer extends Application {
    private String nickname, oppNick, serverAddress, end;
    private Color color;
    private Socket serverSocket;
    private int serverPort;
    private Chessboard chessboard;
    private Square kingSquare;
    private final List<Circle> highlightedCircles = new ArrayList<>();
    private List<Move> posMoves = new ArrayList<>();
    private boolean myTurn, running = true;
    private volatile boolean wait, newGame;
    private final TableView<ScoreboardEntry> scoreboardTable = new TableView<>();


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        readConfigFromXML();
        primaryStage.setTitle("Benvenuto");

        Label titleLabel = new Label("ChessGame");
        titleLabel.setFont(Font.font("Anton", FontWeight.EXTRA_BOLD, 28));
        ImageView startIcon = new ImageView(new Image("file:src/main/img/game-end.gif"));
        startIcon.setFitWidth(75);
        startIcon.setFitHeight(100);

        // Creazione dei controlli
        Label nicknameLabel = new Label("Nickname:");
        nicknameLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        TextField nicknameTextField = new TextField();
        nicknameTextField.setPromptText("Nickname...");
        nicknameTextField.setPrefWidth(150);
        nicknameTextField.setMaxWidth(200);

        Button startButton = new Button("Cerca partita");

        // Azione del pulsante "Inizia partita"
        startButton.setOnAction(event -> {
            nickname = nicknameTextField.getText();
            if (!Objects.equals(nickname, "")) {
                // Avvia il gioco o altre azioni in base alla logica del tuo programma
                startThread(primaryStage);
            }
        });

        // Creazione del layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        VBox topBox = new VBox(10);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(20));
        topBox.getChildren().addAll(new Label(), titleLabel, new Label(), startIcon);

        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().addAll(nicknameLabel, new Label(), nicknameTextField, new Label(), startButton);

        root.setTop(topBox);
        root.setCenter(centerBox);

        // Creazione della scena
        Scene scene = new Scene(root, 540, 580);
        scene.getStylesheets().add("file:src/main/resources/com/chess/chessgame/style.css");
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
                waitingLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 16));
                ImageView waitIcon = new ImageView(new Image("file:src/main/img/waitImage.gif"));
                waitIcon.setFitWidth(50);
                waitIcon.setFitHeight(50);
                VBox waitingBox = new VBox(10);
                waitingBox.setPadding(new Insets(10));
                waitingBox.setAlignment(Pos.CENTER);
                waitingBox.getChildren().addAll(waitingLabel, waitIcon);
                primaryStage.setTitle("Chess Game - " + nickname + " in attesa");
                Scene scene = new Scene(waitingBox, 540, 580);
                scene.getStylesheets().add("file:src/main/resources/com/chess/chessgame/style.css");
                primaryStage.setScene(scene);
            });
            sendNickname(nickname);
            this.oppNick = receiveNickname();
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
                if (this.chessboard == null) {
                    break;
                }
                AudioClip audioClip = new AudioClip(receiveSound());
                audioClip.play();
                this.myTurn = receiveTurn();

                if (Objects.equals(end, "Promo") && myTurn) {
                    Platform.runLater(this::promoAlert);
                    continue;
                }
                Platform.runLater(() -> {
                    GridPane gridPane = createChessboard();

                    VBox root = new VBox();
                    root.setStyle("-fx-background-color: radial-gradient(center 50% 45%, radius 65%, #7b68ee, #ffffff);");
                    root.setAlignment(Pos.CENTER);
                    root.setSpacing(10);
                    root.setPadding(new Insets(10));
                    GridPane bottomPane = new GridPane(), topPane = new GridPane();
                    showEatenPieces(bottomPane, topPane);
                    root.getChildren().addAll(topPane, gridPane, bottomPane);

                    primaryStage.setTitle("Chess Game - " + nickname + " - " + this.color);
                    primaryStage.setScene(new Scene(root, 540, 580));
                    displayChessboard(gridPane);
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
        try {
            if (((this.color == Color.WHITE) && Objects.equals(end, "WHITE Wins")) || ((this.color == Color.BLACK) && Objects.equals(end, "BLACK Wins"))) {
                AudioClip audioClip = new AudioClip(receiveSound());
                audioClip.play();
                showWin(primaryStage);
            } else if (((this.color == Color.BLACK) && Objects.equals(end, "WHITE Wins")) || ((this.color == Color.WHITE) && Objects.equals(end, "BLACK Wins"))) {
                AudioClip audioClip = new AudioClip(receiveSound());
                audioClip.play();
                showLose(primaryStage);
            } else if (Objects.equals(end, "Stalemate")) {
                AudioClip audioClip = new AudioClip(receiveSound());
                audioClip.play();
                showDraw(primaryStage);
            } else if (Objects.equals(end, "conn_err")) {
                showWin(primaryStage);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
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
            scoreboardTable.getColumns().clear();
            // Aggiorna la TableView con i dati della scoreboard
            scoreboardTable.getItems().setAll(scoreboardEntries);
            //this.scoreboard = receiveScoreboard();
            Platform.runLater(() -> {
                Label scoreLabel = new Label("Classifica");
                scoreLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
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

                nicknameColumn.setPrefWidth(100);
                winsColumn.setPrefWidth(81);
                lossesColumn.setPrefWidth(81);
                drawsColumn.setPrefWidth(80);
                scoreboardTable.setPrefWidth(300);
                scoreboardTable.setMaxWidth(350);

                newGameButton.setOnAction(event -> {
                    // Riavvia la partita o altre azioni in base alla logica del tuo programma
                    newGame = true;
                });

                VBox scoreBox = new VBox(10);
                scoreBox.setPadding(new Insets(10));
                scoreBox.setAlignment(Pos.CENTER);
                scoreBox.getChildren().addAll(scoreLabel, scoreboardTable, newGameButton);
                primaryStage.setTitle("Chess Game - Classifica");
                Scene scene = new Scene(scoreBox, 540, 580);
                scene.getStylesheets().add("file:src/main/resources/com/chess/chessgame/style.css");
                primaryStage.setScene(scene);
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

    private String receiveNickname() throws IOException, ClassNotFoundException {
        ObjectInputStream serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
        return (String) serverInputStream.readObject();
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

    private String receiveSound() throws IOException, ClassNotFoundException {
        ObjectInputStream serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
        return (String) serverInputStream.readObject();
    }

    private Chessboard receiveChessboard() throws IOException, ClassNotFoundException {
        ObjectInputStream serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
        Object receivedObject = serverInputStream.readObject();

        if (receivedObject instanceof Chessboard) {
            return (Chessboard) receivedObject;
        } else {
            end = (String) receivedObject;
            return null;
        }
    }

    private GridPane createChessboard() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(2);
        gridPane.setVgap(2);

        if (this.color == Color.WHITE) {// Inserimento delle lettere sotto ogni colonna
            for (char file = 'A'; file <= 'H'; file++) {
                Label label = new Label(String.valueOf(file));
                label.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
                gridPane.add(label, file - 'A' + 1, 9);
            }

            // Inserimento delle lettere sopra ogni colonna
            for (char file = 'A'; file <= 'H'; file++) {
                Label label = new Label(String.valueOf(file));
                label.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
                gridPane.add(label, file - 'A' + 1, 0);
            }

            // Inserimento dei numeri alla destra di ogni riga
            for (int rank = 8; rank >= 1; rank--) {
                Label label = new Label(String.valueOf(rank));
                label.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
                gridPane.add(label, 9, 9 - rank);
            }
            // Inserimento dei numeri alla sinistra di ogni riga
            for (int rank = 8; rank >= 1; rank--) {
                Label label = new Label(String.valueOf(rank));
                label.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
                gridPane.add(label, 0, 9 - rank);
            }

            // Creazione delle caselle della scacchiera come rettangoli colorati
            for (int rank = 8; rank >= 1; rank--) {
                for (char file = 'A'; file <= 'H'; file++) {
                    Rectangle square = new Rectangle(50, 50, (rank + (file - 'A')) % 2 != 0 ? javafx.scene.paint.Color.WHITE : javafx.scene.paint.Color.MEDIUMSLATEBLUE);
                    gridPane.add(square, file - 'A' + 1, rank);
                }
            }
        } else {
            // Inserimento delle lettere sotto ogni colonna
            for (char file = 'H'; file >= 'A'; file--) {
                Label label = new Label(String.valueOf(file));
                label.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
                gridPane.add(label, 'H' - file + 1, 9);
            }

            // Inserimento delle lettere sopra ogni colonna
            for (char file = 'H'; file >= 'A'; file--) {
                Label label = new Label(String.valueOf(file));
                label.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
                gridPane.add(label, 'H' - file + 1, 0);
            }

            // Inserimento dei numeri alla destra di ogni riga
            for (int rank = 1; rank <= 8; rank++) {
                Label label = new Label(String.valueOf(rank));
                label.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
                gridPane.add(label, 9, rank);
            }
            // Inserimento dei numeri alla sinistra di ogni riga
            for (int rank = 1; rank <= 8; rank++) {
                Label label = new Label(String.valueOf(rank));
                label.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
                gridPane.add(label, 0, rank);
            }

            // Creazione delle caselle della scacchiera come rettangoli colorati
            for (int rank = 1; rank <= 8; rank++) {
                for (char file = 'A'; file <= 'H'; file++) {
                    Rectangle square = new Rectangle(50, 50, (rank + ('H' - file)) % 2 != 0 ? javafx.scene.paint.Color.WHITE : javafx.scene.paint.Color.MEDIUMSLATEBLUE);
                    gridPane.add(square, 'H' - file + 1, rank);
                }
            }
        }

        return gridPane;
    }

    private void displayChessboard(GridPane gridPane) {
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
                if (this.color == Color.WHITE) {
                    gridPane.add(pieceImageView, square.getFile() - 'A' + 1, 9 - square.getRank());
                } else {
                    gridPane.add(pieceImageView, 'H' - square.getFile() + 1, square.getRank());
                }
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
            if (this.color == Color.WHITE) {
                GridPane.setColumnIndex(targetCircle, targetFile - 'A' + 1);
                GridPane.setRowIndex(targetCircle, 9 - targetRank);
            } else {
                GridPane.setColumnIndex(targetCircle, 'H' - targetFile + 1);
                GridPane.setRowIndex(targetCircle, targetRank);
            }

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
            ImageView endIcon = new ImageView(new Image("file:src/main/img/game-end.gif"));
            endIcon.setFitWidth(75);
            endIcon.setFitHeight(100);
            Label victoryLabel = new Label("Hai vinto!");
            victoryLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 20));
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
            vbox.setAlignment(Pos.CENTER);
            vbox.getChildren().addAll(endIcon, victoryLabel, new Label(), new Label(), newGameButton, scoreboardButton);

            primaryStage.setTitle("Vittoria");
            Scene scene = new Scene(vbox, 540, 580);
            scene.getStylesheets().add("file:src/main/resources/com/chess/chessgame/style.css");
            primaryStage.setScene(scene);
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
            ImageView endIcon = new ImageView(new Image("file:src/main/img/game-end.gif"));
            endIcon.setFitWidth(75);
            endIcon.setFitHeight(100);
            Label victoryLabel = new Label("Hai perso!");
            victoryLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 20));
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
            vbox.setAlignment(Pos.CENTER);
            vbox.getChildren().addAll(endIcon, victoryLabel, new Label(), new Label(), newGameButton, scoreboardButton);

            primaryStage.setTitle("Sconfitta");
            Scene scene = new Scene(vbox, 540, 580);
            scene.getStylesheets().add("file:src/main/resources/com/chess/chessgame/style.css");
            primaryStage.setScene(scene);
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

    private void showDraw(Stage primaryStage) {
        Platform.runLater(() -> {
            ImageView endIcon = new ImageView(new Image("file:src/main/img/game-end.gif"));
            endIcon.setFitWidth(75);
            endIcon.setFitHeight(100);
            Label victoryLabel = new Label("Patta!");
            victoryLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 20));
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
            vbox.setAlignment(Pos.CENTER);
            vbox.getChildren().addAll(endIcon, victoryLabel, new Label(), new Label(), newGameButton, scoreboardButton);

            primaryStage.setTitle("Patta");
            Scene scene = new Scene(vbox, 540, 580);
            scene.getStylesheets().add("file:src/main/resources/com/chess/chessgame/style.css");
            primaryStage.setScene(scene);
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
            victoryLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 20));

            // Azione del pulsante "Nuova partita"
            newGameButton.setOnAction(event -> {
                // Riavvia la partita o altre azioni in base alla logica del tuo programma
                newGame = true;
            });

            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(10));
            vbox.setAlignment(Pos.CENTER);
            vbox.getChildren().addAll(victoryLabel, newGameButton);

            primaryStage.setTitle("Fine partita");
            Scene scene = new Scene(vbox, 540, 580);
            scene.getStylesheets().add("file:src/main/resources/com/chess/chessgame/style.css");
            primaryStage.setScene(scene);
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

    private void showEatenPieces(GridPane bottomPane, GridPane topPane) {
        bottomPane.setHgap(10);
        topPane.setHgap(10);

        Button surrButton = new Button("Resa");
        surrButton.setStyle("-fx-background-color: #069f63; -fx-text-fill: white; -fx-font-size: 16px;");
        Label ownNickname = new Label(nickname);
        ownNickname.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        Label oppNickname = new Label(oppNick);
        oppNickname.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        HBox ownEaten = new HBox();
        HBox oppEaten = new HBox();
        HBox ownPawns = new HBox();
        HBox ownBishops = new HBox();
        HBox ownRooks = new HBox();
        HBox ownQueens = new HBox();
        HBox ownKnights = new HBox();
        HBox oppPawns = new HBox();
        HBox oppRooks = new HBox();
        HBox oppBishops = new HBox();
        HBox oppKnights = new HBox();
        HBox oppQueens = new HBox();

        ownPawns.setSpacing(-10.5);
        ownBishops.setSpacing(-10.5);
        ownRooks.setSpacing(-10.5);
        ownQueens.setSpacing(-10.5);
        ownKnights.setSpacing(-10.5);
        oppPawns.setSpacing(-10.5);
        oppRooks.setSpacing(-10.5);
        oppQueens.setSpacing(-10.5);
        oppKnights.setSpacing(-10.5);
        oppBishops.setSpacing(-10.5);

        int count = 0;
        for (Piece p : this.chessboard.getEatenPieces()) {
            ImageView im_p = createPieceImageView(p);
            im_p.setFitWidth(20);
            im_p.setFitHeight(20);
            if (p.getColor() != this.color) {
                if (p instanceof Pawn) {
                    ownPawns.getChildren().add(im_p);
                    count += 1;
                } else if (p instanceof Knight) {
                    ownKnights.getChildren().add(im_p);
                    count += 3;
                } else if (p instanceof Bishop) {
                    ownBishops.getChildren().add(im_p);
                    count += 3;
                } else if (p instanceof Queen) {
                    ownQueens.getChildren().add(im_p);
                    count += 10;
                } else if (p instanceof Rook) {
                    ownRooks.getChildren().add(im_p);
                    count += 5;
                }
            } else {
                if (p instanceof Pawn) {
                    oppPawns.getChildren().add(im_p);
                    count -= 1;
                } else if (p instanceof Knight) {
                    oppKnights.getChildren().add(im_p);
                    count -= 3;
                } else if (p instanceof Bishop) {
                    oppBishops.getChildren().add(im_p);
                    count -= 3;
                } else if (p instanceof Queen) {
                    oppQueens.getChildren().add(im_p);
                    count -= 10;
                } else if (p instanceof Rook) {
                    oppRooks.getChildren().add(im_p);
                    count -= 5;
                }
            }
        }
        ownEaten.getChildren().addAll(ownPawns, ownBishops, ownKnights, ownRooks, ownQueens);
        oppEaten.getChildren().addAll(oppPawns, oppBishops, oppKnights, oppRooks, oppQueens);

        Label ownCount = new Label(count > 0 ? "+ " + count : "");
        Label oppCount = new Label(count < 0 ? "+ " + Math.abs(count) : "");

        surrButton.setOnAction(event -> {
            sendSurr();
            wait = false;
            running = false;
        });

        HBox thisPlayer = new HBox();
        thisPlayer.getChildren().addAll(ownNickname, ownEaten, ownCount);
        thisPlayer.setSpacing(10);
        thisPlayer.setPrefWidth(300);

        HBox opponentPlayer = new HBox();
        opponentPlayer.getChildren().addAll(oppNickname, oppEaten, oppCount);
        opponentPlayer.setSpacing(10);
        opponentPlayer.setPrefWidth(300);

        bottomPane.add(thisPlayer, 2, 0);
        bottomPane.add(new Label(), 4, 0);
        bottomPane.add(surrButton, 18, 0);
        topPane.add(opponentPlayer, 2, 0);
    }
}
