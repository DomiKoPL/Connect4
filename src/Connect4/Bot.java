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
    private  List<State> result = new ArrayList<>();

    private Bot(){
    }

    public static Bot getInstance(){
        if(INSTANCE == null){
            INSTANCE = new Bot();
        }
        return INSTANCE;
    }

    private static final int COLUMNS = 7;
    private static final int ROWS = 6;

    private byte[][] grid;
    private int[] lastEmpty;


    public byte[][] getGrid(){
        return grid;
    }

    public int[] getLastEmpty(){
        return lastEmpty;
    }

    boolean made = false;

    public int getBestMove(byte[][] grid, int[] lastEmpty, int playerLastMove){
        this.grid = grid;
        this.lastEmpty = lastEmpty;

        if(!made){
            made = true;
            for (int  i = 0 ; i < COLUMNS; i++){
                runners[i] = new BotHelper(i, result);
                threads[i] = new Thread(runners[i]);
            }
        }
        return bestMove(playerLastMove);
    }


    private int bestMove(int playerLastMove){
        State bestRes = new State((byte)-1,0,0);
        int bestColumn = playerLastMove;

        int toDo = 0;
        for(int i = 0 ; i < COLUMNS; ++i) {
            if (lastEmpty[i] >= 0) {
                threads[i].run();
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

                System.out.print(res.column + " " + (res.state == 1 ? "WIN " : (res.state == -1 ? "LOSE " : "DRAW ")) + res.depth + " " + String.format("%.2f",res.probability )+ " | ");
            }
            else
                System.out.print("");//nothing
        }


        System.out.print("\n");
        return bestColumn;
    }
}
