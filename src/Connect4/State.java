package Connect4;

public class State{
    byte state;
    int depth;
    double probability;
    int column;
    State(byte state, int depth, double probability){
        this.state = state;
        this.depth = depth;
        this.probability = probability;
    }
    State(byte state, int depth, double probability, int column){
        this(state,depth,probability);
        this.column = column;
    }
}