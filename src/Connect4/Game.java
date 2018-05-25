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

    private static final int TILE_SIZE = 80;
    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final int WIDTH = (COLUMNS + 1) * TILE_SIZE;
    private static final int HEIGHT = (ROWS + 1 ) * TILE_SIZE;

    private boolean redMove = true;
    private boolean canMove = true;
    private boolean gameWithBot = false;

    private  Disc[][] grid = new Disc[COLUMNS][ROWS];

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
        pvp.setFont(Font.font("Times New Roman",FontWeight.BOLD,90));
        pvp.setOnMouseClicked(e -> {
            gameWithBot = false;
            pvp.setVisible(false);
            pvb.setVisible(false);
        });

        pvb.setMinHeight(HEIGHT / 2);
        pvb.setMaxHeight(HEIGHT / 2);
        pvb.setTranslateY(HEIGHT/2);
        pvb.setMinWidth(WIDTH);
        pvb.setMaxWidth(WIDTH);
        pvb.setAlignment(Pos.CENTER);
        pvb.setTextFill(Color.RED);
        pvb.setFont(Font.font("Times New Roman",FontWeight.BOLD,90));
        pvb.setOnMouseClicked(e -> {
            gameWithBot = true;
            pvp.setVisible(false);
            pvb.setVisible(false);
        });

        root.getChildren().add(winMessage);
        root.getChildren().add(pvp);
        root.getChildren().add(pvb);

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
        lighting.setSurfaceScale(5.0);

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


        int row = ROWS - 1;
        do{
            if(!getDisc(column,row).isPresent())
                break;

            row--;
        }while(row >= 0);

        if(row < 0)
            return;

        canMove = false;

        grid[column][row] = disc;
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
                    botMove(new Disc(redMove));
                }
            }
        });
        animation.play();
    }

    private void botMove(Disc disc){
        if(!canMove)
            return;

        System.out.println("Bot move");
        Random gen = new Random();

        while(true){

            int column = gen.nextInt(COLUMNS);

            int row = ROWS - 1;
            do{
                if(!getDisc(column,row).isPresent())
                    break;

                row--;
            }while(row >= 0);

            if(row < 0)
                continue;

            canMove = false;

            grid[column][row] = disc;
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
            return;
        }
    }

    private boolean gameEnded(int column , int row){
        List<Point2D> vertical = IntStream.rangeClosed(row - 3 , row + 3)
                .mapToObj(r -> new Point2D(column,r))
                .collect(Collectors.toList());

        List<Point2D> horizontal = IntStream.rangeClosed(column - 3 , column + 3)
                .mapToObj(c -> new Point2D(c,row))
                .collect(Collectors.toList());

        Point2D topLeft = new Point2D(column - 3 , row - 3);
        List<Point2D> diagonal1 = IntStream.rangeClosed(0,6)
                .mapToObj(i -> topLeft.add( i , i))
                .collect(Collectors.toList());

        Point2D botLeft = new Point2D(column - 3 , row + 3);
        List<Point2D> diagonal2 = IntStream.rangeClosed(0,6)
                .mapToObj(i -> botLeft.add( i , -i))
                .collect(Collectors.toList());

        return checkRange(vertical) || checkRange(horizontal) || checkRange(diagonal1) || checkRange(diagonal2);
    }

    private boolean checkRange(List<Point2D> points){
        int chain = 0;

        for(Point2D p : points){
            int column = (int)p.getX();
            int row = (int)p.getY();

            Disc disc = getDisc(column,row).orElse(new Disc(!redMove));

            if(disc.red == redMove){
                if(++chain == 4){
                    return true;
                }
            }else{
                chain = 0;
            }
        }

        return false;
    }

    private void gameOver(){
        canMove = false;
        winMessage.setVisible(true);
        winMessage.setText((redMove ? "Red" : "Yellow") + " won");
        //System.out.println((redMove ? "Red" : "Yellow") + " won");//DEBUG
    }

    private void restart(){
        //System.out.println((redMove ? "Red" : "Yellow") + " won");//DEBUG
        winMessage.setVisible(false);

        canMove = true;
        redMove = true;

        grid = new Disc[COLUMNS][ROWS];

        discRoot.getChildren().clear();
    }

    private Optional<Disc> getDisc(int column , int row){
        if(column < 0 || column >= COLUMNS || row < 0 || row >= ROWS)
            return Optional.empty();

        return Optional.ofNullable(grid[column][row]);
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
