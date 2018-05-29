package Connect4;

import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Game {
    private static Game INSTANCE;

    private Game(){

    }

    public static Game getInstance(){
        if(INSTANCE == null){
            INSTANCE = new Game();
        }
        return INSTANCE;
    }

    public void start(Stage stage){
        stage.setScene(new Scene(createContent()));
        stage.show();
    }


    private static final byte EMPTY = 0;
    private static final byte PLAYER1 = 1;
    private static final byte PLAYER2 = 2;

    private static final int TILE_SIZE = 80;
    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final int WIDTH = (COLUMNS + 1) * TILE_SIZE;
    private static final int HEIGHT = (ROWS + 1 ) * TILE_SIZE;

    private boolean redMove = true;
    private boolean canMove = true;
    private boolean gameWithBot = false;
    private boolean draw = false;

    private  byte[][] grid = new byte[COLUMNS][ROWS];
    private int[] lastEmpty = new int[COLUMNS];
    private Pane discRoot = new Pane();

    Label winMessage;

    private Parent createContent(){
        Pane root = new Pane();
        root.getChildren().add(discRoot);

        Shape gridShape = makeGrid();
        root.getChildren().add(gridShape);
        root.getChildren().addAll(makeColumns());

        winMessage = new Label("Red won");
        winMessage.setMinHeight(HEIGHT);
        winMessage.setMaxHeight(HEIGHT);
        winMessage.setMinWidth(WIDTH);
        winMessage.setMaxWidth(WIDTH);
        winMessage.setAlignment(Pos.CENTER);
        winMessage.setTextFill(Color.BLACK);
        winMessage.setFont(Font.font("Times New Roman",FontWeight.BOLD,90));
        winMessage.setVisible(false);
        winMessage.setOnMouseClicked(e -> restart());

        Label pvp = new Label("Player vs Player");
        Label pvb = new Label("Player vs Bot");

        pvp.setMinHeight(HEIGHT / 2);
        pvp.setMaxHeight(HEIGHT / 2);
        pvp.setMinWidth(WIDTH);
        pvp.setMaxWidth(WIDTH);
        pvp.setAlignment(Pos.CENTER);
        pvp.setTextFill(Color.RED);
        pvp.setStyle("-fx-background-color: white;");
        pvp.setFont(Font.font("Times New Roman",FontWeight.BOLD,90));
        pvp.setOnMouseClicked(e -> {
            gameWithBot = false;
            pvp.setVisible(false);
            pvb.setVisible(false);
        });
        pvp.setOnMouseEntered(e ->{
            pvp.setTextFill(Color.rgb(180,17,0));
        });
        pvp.setOnMouseExited(e ->{
            pvp.setTextFill(Color.RED);
        });

        pvb.setMinHeight(HEIGHT / 2);
        pvb.setMaxHeight(HEIGHT / 2);
        pvb.setTranslateY(HEIGHT/2);
        pvb.setMinWidth(WIDTH);
        pvb.setMaxWidth(WIDTH);
        pvb.setAlignment(Pos.CENTER);
        pvb.setTextFill(Color.RED);
        pvb.setStyle("-fx-background-color: white;");
        pvb.setFont(Font.font("Times New Roman",FontWeight.BOLD,90));
        pvb.setOnMouseClicked(e -> {
            gameWithBot = true;
            pvp.setVisible(false);
            pvb.setVisible(false);
        });
        pvb.setOnMouseEntered(e ->{
            pvb.setTextFill(Color.rgb(180,17,0));
        });
        pvb.setOnMouseExited(e ->{
            pvb.setTextFill(Color.RED);
        });

        root.getChildren().add(winMessage);
        root.getChildren().add(pvp);
        root.getChildren().add(pvb);

        restart();

        return root;
    }

    private Shape makeGrid(){
        Shape shape = new Rectangle((COLUMNS + 1) * TILE_SIZE, (ROWS + 1) * TILE_SIZE);

        for(int y = 0 ; y < ROWS; y++){
            for(int x = 0 ; x < COLUMNS; x++){
                Circle circle = new Circle(TILE_SIZE/2);
                circle.setCenterX(TILE_SIZE/2);
                circle.setCenterY(TILE_SIZE/2);
                circle.setTranslateX(x * (TILE_SIZE + 5) + TILE_SIZE/3);
                circle.setTranslateY(y * (TILE_SIZE + 5) + TILE_SIZE/3);

                shape = Shape.subtract(shape,circle);
            }
        }

        Light.Distant light = new Light.Distant();
        light.setAzimuth(45.0);
        light.setElevation(30.0);

        Lighting lighting = new Lighting();
        lighting.setLight(light);
        lighting.setSurfaceScale(3.0);

        shape.setFill(Color.BLUE);
        shape.setEffect(lighting);

        return shape;
    }

    private List<Rectangle> makeColumns(){
        List<Rectangle> list = new ArrayList<>();

        for(int x = 0; x < COLUMNS; x++){
            Rectangle rect = new Rectangle(TILE_SIZE, (ROWS + 1) * TILE_SIZE);
            rect.setTranslateX(x * (TILE_SIZE + 5) + TILE_SIZE/3);
            rect.setFill(Color.TRANSPARENT);
            rect.setOnMouseEntered(e -> rect.setFill(Color.rgb(200,200,50,0.3)));
            rect.setOnMouseExited(e -> rect.setFill(Color.TRANSPARENT));

            final int column = x;

            rect.setOnMouseClicked(e -> placeDisc(new Disc(redMove),column));

            list.add(rect);
        }
        return list;
    }

    private void placeDisc(Disc disc, int column){
        if(!canMove)
            return;

        if(gameWithBot && !redMove)
            return;

        if(lastEmpty[column] < 0)
            return;

        int row = lastEmpty[column]--;

        canMove = false;

        grid[column][row] = (disc.red ? PLAYER1 : PLAYER2);
        discRoot.getChildren().add(disc);
        disc.setTranslateX(column * (TILE_SIZE + 5) + TILE_SIZE / 3);

        final int currentRow = row;

        TranslateTransition animation = new TranslateTransition(Duration.seconds(0.5),disc);
        animation.setToY(row * (TILE_SIZE + 5) + TILE_SIZE/3);
        animation.setOnFinished(e -> {
            if(gameEnded(column,currentRow)){
                gameOver();
            }
            else{
                redMove = !redMove;
                canMove = true;

                if(gameWithBot && !redMove){
                    botMove(new Disc(redMove),column);
                }
            }
        });
        animation.play();
    }

    private void botMove(Disc disc, int playerLastMove){
        if(!canMove)
            return;

        int column = Bot.getInstance().getBestMove(grid,lastEmpty,playerLastMove);
        int row = lastEmpty[column]--;

        canMove = false;

        grid[column][row] = (disc.red ? PLAYER1 : PLAYER2);
        discRoot.getChildren().add(disc);
        disc.setTranslateX(column * (TILE_SIZE + 5) + TILE_SIZE / 3);

        final int currentRow = row;

        TranslateTransition animation = new TranslateTransition(Duration.seconds(0.5),disc);
        animation.setToY(row * (TILE_SIZE + 5) + TILE_SIZE/3);
        animation.setOnFinished(e -> {
           if(gameEnded(column,currentRow)){
               gameOver();
           }
           else{
               redMove = !redMove;
               canMove = true;
           }
        });
        animation.play();
    }

    private boolean gameEnded(int column , int row){

        draw = true;
        for (int i = 0 ; i < COLUMNS;i++){
            if(lastEmpty[i] >= 0){
                draw = false;
                break;
            }
        }

        return draw
                || checkRange(column,row - 3 , 0, 1)
                || checkRange(column - 3, row , 1,0)
                || checkRange(column - 3 , row -3,1,1)
                || checkRange(column-3,row + 3, 1,-1);
    }

    private boolean checkRange(int column, int row , int dColumn ,int dRow){
        int chain = 0;

        for(int i = 0 ; i < 7; i++){
            byte disc = getDisc(column,row);

            if((disc == PLAYER1 && redMove) || (disc == PLAYER2 && !redMove)){
                if(++chain == 4){
                    return true;
                }
            }else{
                chain = 0;
            }

            column += dColumn;
            row += dRow;
        }

        return false;
    }

    private void gameOver(){
        canMove = false;
        winMessage.setVisible(true);
        if(draw){
            winMessage.setText("Draw");
        }
        else {
            winMessage.setText((redMove ? "Red" : "Yellow") + " won");
        }
    }

    private void restart(){
        //System.out.println((redMove ? "Red" : "Yellow") + " won");//DEBUG
        winMessage.setVisible(false);

        canMove = true;
        redMove = true;

        grid = new byte[COLUMNS][ROWS];

        lastEmpty = new int[COLUMNS];

        for(int i = 0 ; i < COLUMNS; i++){
            lastEmpty[i] = ROWS - 1;
        }

        discRoot.getChildren().clear();
    }

    private byte getDisc(int column , int row){
        if(column < 0 || column >= COLUMNS || row < 0 || row >= ROWS)
            return 0;

        return grid[column][row];
    }

    private static class Disc extends Circle {
        private  final boolean red;
        public Disc(boolean red){
            super(TILE_SIZE/2,red ? Color.RED : Color.YELLOW);
            this.red = red;
            setCenterX(TILE_SIZE/2);
            setCenterY(TILE_SIZE/2);
        }

    }
}
