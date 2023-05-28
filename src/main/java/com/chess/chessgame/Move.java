package com.chess.chessgame;

import java.io.Serializable;

public record Move(Square fromSquare, Square toSquare) implements Serializable {

    public Piece getPiece() {
        return this.fromSquare.getPiece();
    }
}
