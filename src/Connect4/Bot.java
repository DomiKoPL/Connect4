package Connect4;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Bot {
    private static Bot INSTANCE;

    private Bot(){

    }

    public static Bot getInstance(){
        if(INSTANCE == null){
            INSTANCE = new Bot();
        }
        return INSTANCE;
    }

    private static final int maxDepth = 8;

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

    public int getBestMove(byte[][] grid, int[] lastEmpty){
        this.grid = grid;
        this.lastEmpty = lastEmpty;
        return bestMove();
    }

    private int bestMove(){
        State bestRes = new State(LOSE,0);
        List<Integer>bestMoves = new ArrayList<>();

        for(int i = 0 ; i < COLUMNS; ++i){
            if(lastEmpty[i] >= 0){
                State res = simulateMovements(i,PLAYER2,1);
                if(res.state > bestRes.state){
                    bestMoves = new ArrayList<>();
                    bestMoves.add(i);
                    bestRes = res;
                }else if(res.state == bestRes.state && res.depth > bestRes.depth){
                    bestRes = res;
                    bestMoves = new ArrayList<>();
                    bestMoves.add(i);
                }else if(res.depth == bestRes.depth){
                    bestMoves.add(i);
                }
                System.out.print((res.state == WIN ? "WIN " : (res.state == LOSE ? "LOSE " : "DRAW ")) + res.depth + " ");
            }
        }
        System.out.print("\n");
        return bestMoves.get(0);
    }

    private State simulateMovements(int column,byte player, int depth){
        int row = lastEmpty[column]--;
        grid[column][row] = player;
        boolean win = gameEnded(column,row,player);

        if(player == PLAYER2 && win){
            lastEmpty[column]++;
            grid[column][row] = EMPTY;
            return new State(WIN,depth);
        }else if(player == PLAYER1 && win){
            lastEmpty[column]++;
            grid[column][row] = EMPTY;
            return new State(LOSE,depth);
        }


        byte nextPlayer = (player == PLAYER1 ? PLAYER2 : PLAYER1);
        State bestRes = new State(LOSE,depth);
        boolean canMove = false;
        int nDepth = depth + 1;
        for(int i = 0 ; i < COLUMNS; i++){
            if(lastEmpty[i] >= 0 && depth < maxDepth){

                State res = simulateMovements(i,nextPlayer,nDepth);
                if(player == PLAYER2){
                    if(!canMove || res.state < bestRes.state){
                        bestRes = res;
                    }
                    else if(res.state == bestRes.state && res.depth < bestRes.depth){
                        bestRes = res;
                    }
                }else{
                    if(!canMove || res.state > bestRes.state){
                        bestRes = res;
                    }else if(res.state == bestRes.state && res.depth < bestRes.depth){
                        bestRes = res;
                    }
                }
                canMove = true;
            }
        }

        lastEmpty[column]++;
        grid[column][row] = EMPTY;

        if(!canMove){
            return new State(DRAW,depth);
        }

        if(player == PLAYER2){
            return  bestRes;
        }

        if(bestRes.state == WIN){
            return new State(LOSE,bestRes.depth);
        }else if(bestRes.state == LOSE){
            return new State(WIN,bestRes.depth);
        }
        return new State(DRAW,bestRes.depth);
    }

    private byte getDisc(int column , int row){
        if(column < 0 || column >= COLUMNS || row < 0 || row >= ROWS)
            return 0;

        return grid[column][row];
    }

    private boolean gameEnded(int column , int row, byte player){
        return checkRange(column,row - 3 , 0, 1,player)
                || checkRange(column - 3, row , 1,0,player)
                || checkRange(column - 3 , row -3,1,1,player)
                || checkRange(column-3,row + 3, 1,-1,player);
    }

    private boolean checkRange(int column, int row , int dColumn ,int dRow,byte player){
        int chain = 0;

        for(int i = 0 ; i < 7; i++){
            byte disc = getDisc(column,row);

            if(disc == player){
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

    class State{
        final byte state;
        final int depth;
        State(byte state, int depth){
            this.state = state;
            this.depth = depth;
        }
    }
}
