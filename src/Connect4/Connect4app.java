package Connect4;

import javafx.application.Application;
import javafx.stage.Stage;

public class Connect4app  extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        Game.getInstance().start(stage);
    }

    public  static void main(String[] args){
        launch(args);
    }
}
