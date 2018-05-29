package Connect4;

import javafx.geometry.Point2D;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Bot {
    private static Bot INSTANCE;
    private Thread[] threads = new Thread[7];
    private Runnable[] runners = new Runnable[7];

    private Bot(){

    }


    public static Bot getInstance(){
        if(INSTANCE == null){
            INSTANCE = new Bot();
        }
        return INSTANCE;
    }

    private static final int maxDepth = 9;

    private static final byte EMPTY = 0;
    private static final byte PLAYER1 = 1;
    private static final byte PLAYER2 = 2;//BOT

    private static final int COLUMNS = 7;
    private static final int ROWS = 6;

    private static final byte LOSE = -1;
    private static final byte DRAW = 0;
    private static final byte WIN = 1;

    private byte[][] grid;
    private int[] lastEmpty;

    public int getBestMove(byte[][] grid, int[] lastEmpty, int playerLastMove){
        this.grid = grid;
        this.lastEmpty = lastEmpty;
        return bestMove(playerLastMove);
    }

    private int bestMove(int playerLastMove){
        State bestRes = new State(LOSE,0,0);
        int bestColumn = playerLastMove;

        List<State> result = new ArrayList<State>();
        int toDo = 0;
        for(int i = 0 ; i < COLUMNS; ++i) {
            if (lastEmpty[i] >= 0) {
                byte[][] newGrid = new byte[COLUMNS][ROWS];
                int[] newLastEmpty = Arrays.copyOf(lastEmpty,COLUMNS);


                for(int y = 0 ; y < COLUMNS; y++){
                    newGrid[y] = Arrays.copyOf(grid[y],ROWS);
                }

                runners[i] = new BotHelper(i, PLAYER2, 1, newGrid, newLastEmpty, result);
                threads[i] = new Thread(runners[i]);
                threads[i].start();
                //threads[i].run();
                toDo++;
            }
        }
        while(toDo > 0){
            if(result.size() > 0){
                toDo--;
                State res = result.get(0);
                result.remove(0);

                if(res == null)
                    continue;

                if(res.state > bestRes.state){
                    bestRes = res;
                    bestColumn = res.column;
                }else if(res.state == bestRes.state && res.depth > bestRes.depth){
                    bestRes = res;
                    bestColumn = res.column;
                }else if(res.depth == bestRes.depth && res.probability > bestRes.probability){
                    bestRes = res;
                    bestColumn = res.column;
                }
                System.out.print(res.column + " " + (res.state == WIN ? "WIN " : (res.state == LOSE ? "LOSE " : "DRAW ")) + res.depth + " " + String.format("%.2f",res.probability )+ " ");
            }
            else
                System.out.print(" ");
        }


        System.out.print("\n");
        return bestColumn;
    }
}
