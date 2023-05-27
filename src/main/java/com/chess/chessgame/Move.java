package com.chess.chessgame;

import java.io.Serializable;

public class Move implements Serializable {
    private Square fromSquare;
    private Square toSquare;

    public Move(Square fromSquare, Square toSquare) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
    }

    public Square getFromSquare() {
        return fromSquare;
    }

    public void setFromSquare(Square fromSquare) {
        this.fromSquare = fromSquare;
    }

    public Square getToSquare() {
        return toSquare;
    }

    public void setToSquare(Square toSquare) {
        this.toSquare = toSquare;
    }

    public Piece getPiece() {
        return this.fromSquare.getPiece();
    }
}
