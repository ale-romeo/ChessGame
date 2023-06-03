module com.chess.chessgame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires javafx.media;


    opens com.chess.chessgame to javafx.fxml;
    exports com.chess.chessgame;
}