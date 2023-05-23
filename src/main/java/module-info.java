module com.chess.chessgame {
    requires javafx.controls;
    requires javafx.fxml;
            
                            
    opens com.chess.chessgame to javafx.fxml;
    exports com.chess.chessgame;
}