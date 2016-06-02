package dk.simonsteinaa.connect4.screens;

import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dk.simonsteinaa.framework.interfaces.Graphics;

/**
 * Created by simon on 4/24/16.
 */
// helper class to hold info about bottom collision if any
class BottomHit {
    public int row;
    public int column;

    public BottomHit(int row, int column) {
        this.row = row;
        this.column = column;
    }
}

// helper class to hold info about player info to display
class PlayerObject {
    public String name;
    public int color;

    public PlayerObject(String name, int color) {
        this.name = name;
        this.color = color;
    }
}

public class Board {
    private Graphics graphics;
    private int width;
    private int height;
    private int numberOfRows;
    private int numberOfColumns;
    private int pieceWidth;
    private int pieceHeight;
    private int yOffset = 4;

    // the pieces currently on the field
    private List<List<Piece>> pieces;

    private List<Integer> bottom;

    // this attribute should be looked at in the GameController
    // if it is non-empty it will be an object containing a row and a column
    public BottomHit hitBottom;

    // these objects will hold a name and a color to draw if set
    public PlayerObject waitingForOpponent;
    public PlayerObject winnerFound;

    public Board(Graphics graphics, int width, int height, int rows, int columns) {
        this.graphics = graphics;
        this.width = width;
        this.height = height;
        this.numberOfRows = rows;
        this.numberOfColumns = columns;
        this.pieceWidth = width / columns;
        this.pieceHeight = height / rows;

        Log.i("width", "" + width );
        Log.i("height", "" + height );
        Log.i("rows", "" + rows );
        Log.i("columns", "" + columns );
        Log.i("pieceWidth", "" + pieceWidth );
        Log.i("pieceHeight", "" + pieceHeight );




        reset();
    }

    public BottomHit checkBottom() {
        return hitBottom;
    }

    public void nullBottom() {
        hitBottom = null;
    }

    //method to make sure player doesn't play an illegal move
    public boolean isMoveValid(int column) {
        return bottom.get(column) >= 0;
    }

    public int getWidth() {
        return width;
    }

    public int getPieceWidth() {
        return pieceWidth;
    }

    public int getHeight() {
        return height;
    }

    public void reset() {
        waitingForOpponent = null;
        winnerFound = null;

        //waitingForOpponent = new PlayerObject("Hulla Bulla", Color.MAGENTA);
        //winnerFound = new PlayerObject("Simon the Sorcerer", Color.YELLOW);

        // the board - divided into NUMBER_OF_COLUMNS subarrays - one for each column
        this.pieces = new ArrayList<>();
        for (int column = 0; column < numberOfColumns; column++) {
            pieces.add(new ArrayList());
        }

        // initialize the bottom array
        this.bottom = new ArrayList();
        for (int column = 0; column < numberOfColumns; column++) {
            bottom.add(height - pieceHeight);
        }
    }

    ;

    // raise the bottom one "level" higher for a specific column
    public void raiseBottomOneLevel(int column) {
        bottom.set(column, bottom.get(column) - this.pieceHeight);
    }

    ;

    // place the token at the given column at it's initial top position
    // the animation will make it fall to the bottom
    public void placeToken(int column, int color) {
        // place the y-coord half a height above - to show that it is falling

        pieces.get(column).add(
                new Piece(graphics, column * pieceWidth, 0 - pieceHeight / 2,
                        200, 500,
                        this.pieceWidth, this.pieceHeight, color)
        );
        Log.i("placing token at x:", "" + column * pieceWidth );
    }

    ;


    public void draw() {
        graphics.clear(Color.BLACK);



        // this draws the vertical columns
        for (int column = 1; column < numberOfColumns; column++) {

            for (int a = yOffset / 2 - yOffset; a < yOffset / 2; a++)
                //canvas.drawLine(mainColumnSize-2, 0, mainColumnSize-2, height, paint);
                graphics.drawLine(column * pieceWidth + a, 0, column * this.pieceWidth + a, this.height, Color.GRAY);
        }

        graphics.drawLine(0, height-3, width, height - 3, Color.GRAY);
        graphics.drawLine(0, height-2, width, height - 2, Color.GRAY);
        graphics.drawLine(0, height-1, width, height - 1, Color.GRAY);



        // this draws the pieces
        for (int column = 0; column < numberOfColumns; column++) {
            for (int p = 0; p < pieces.get(column).size(); p++) {
                pieces.get(column).get(p).draw();
            }
        }


        // draw a waiting message if waiting for opponent
        if (this.waitingForOpponent != null) {

            //context.fillStyle = this.waitingForOpponent.color;
            //context.font = "40px Comic Sans MS";

            graphics.drawText(this.width / 2, this.height / 4, "Waiting... ", waitingForOpponent.color, height / 25);


        } else if (winnerFound != null) { // or draw a winner message
            if (!winnerFound.name.equals("Stalemate")) {
                graphics.drawText(this.width / 2, this.height / 4, this.winnerFound.name + " wins!", winnerFound.color, height / 25);

            } else {
                graphics.drawText(this.width / 2, this.height / 4, this.winnerFound.name + " wins!", Color.WHITE, height / 25);
            }
            graphics.drawText(this.width / 2, this.height / 2, "Touch again to play a new game!", Color.GRAY, height / 25);
        }

    }

    public void update(float deltaTime) {
        for (int column = 0; column < numberOfColumns; column++) {
            for (int p = 0; p < pieces.get(column).size(); p++) {
                Piece nextPiece = pieces.get(column).get(p);
                // do the calculation of the physics
                nextPiece.update(deltaTime);
                // when we have hit the bottom of a column
                if (nextPiece.isFalling() && nextPiece.getY() > bottom.get(column)) {

                    // we already have the column - calculate the row as well
                    //var row = NUMBER_OF_ROWS - (2 + bottom[this.column] / PIECE_HEIGHT);
                    int row = bottom.get(column) / this.pieceHeight;

                    // set the hit attribute
                    this.hitBottom = new BottomHit(row, column);

                    // freeze the piece at this position
                    nextPiece.setY(bottom.get(column));
                    nextPiece.setFalling(false);
                    // now move the bottom one up
                    this.raiseBottomOneLevel(column);
                }
            }
        }
    }

}
