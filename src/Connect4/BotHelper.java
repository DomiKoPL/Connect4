package Connect4;

import java.util.Arrays;
import java.util.List;

public class BotHelper implements Runnable{


    private static final int maxDepth = 8;

    private static final byte EMPTY = 0;
    private static final byte PLAYER1 = 1;
    private static final byte PLAYER2 = 2;//BOT

    private static final int COLUMNS = 7;
    private static final int ROWS = 6;

    private static final byte LOSE = -1;
    private static final byte DRAW = 0;
    private static final byte WIN = 1;
    private int column, depth;
    private byte player;

    private byte[][] grid = new byte[COLUMNS][ROWS];
    private int[] lastEmpty;
    private final List<State> result;


    @Override
    public void run() {
        //System.out.println("Bot run");

        this.lastEmpty = Arrays.copyOf(Bot.getInstance().getLastEmpty(),COLUMNS);
        byte[][]copyGrid = Bot.getInstance().getGrid();
        for (int i = 0 ; i < COLUMNS; i++){
            this.grid[i] = Arrays.copyOf(copyGrid[i],ROWS);
        }

        State temp = simulateMovements(column,player,depth);
        temp.column = column;
        result.add(temp);
        //System.out.println("Bot stop");
    }

    public BotHelper(int column , List<State> result){

        this.column = column;
        this.player = PLAYER2;
        this.depth = 1;
        this.result = result;
    }

    private State simulateMovements(int column, byte player, int depth){
        int row = lastEmpty[column]--;
        grid[column][row] = player;
        boolean win = gameEnded(column,row,player);

        if(player == PLAYER2 && win){
            lastEmpty[column]++;
            grid[column][row] = EMPTY;
            return new State(WIN,depth,1);
        }else if(player == PLAYER1 && win){
            lastEmpty[column]++;
            grid[column][row] = EMPTY;
            return new State(LOSE,depth,1);
        }


        byte nextPlayer = (player == PLAYER1 ? PLAYER2 : PLAYER1);
        State bestRes = new State(LOSE,depth,1);
        boolean canMove = false;
        int nDepth = depth + 1;
        for(int i = 0 ; i < COLUMNS; i++){
            if(lastEmpty[i] >= 0 && depth < maxDepth){

                State res = simulateMovements(i,nextPlayer,nDepth);
                if(player == PLAYER2){
                    if(!canMove || res.state < bestRes.state){
                        bestRes = res;
                        bestRes.probability = (res.probability / (double) COLUMNS);
                    }
                    else if(res.state == bestRes.state ){
                        if(res.depth < bestRes.depth)
                            bestRes.depth = res.depth;

                        bestRes.probability += (res.probability / (double) COLUMNS);
                    }
                }else{
                    if(!canMove || res.state > bestRes.state){
                        bestRes = res;
                        bestRes.probability = (res.probability / (double) COLUMNS);
                    }else if(res.state == bestRes.state){
                        if(res.depth < bestRes.depth)
                            bestRes.depth = res.depth;

                        bestRes.probability += (res.probability / (double) COLUMNS);
                    }
                }
                canMove = true;
            }
        }

        lastEmpty[column]++;
        grid[column][row] = EMPTY;

        if(!canMove){
            return new State(DRAW,depth,1);
        }

        if(player == PLAYER2){
            return  bestRes;
        }

        if(bestRes.state == WIN){
            bestRes.state = LOSE;
        }else if(bestRes.state == LOSE){
            bestRes.state = WIN;
        }
        return bestRes;
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

}
