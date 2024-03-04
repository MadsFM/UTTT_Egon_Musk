package dk.easv;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/dk/easv/gui/App.fxml"));
        primaryStage.setTitle("Ultimate Tic Tac Toe - Diamonds and Trash");
        primaryStage.setScene(new Scene(root, 900, 700));
        primaryStage.setTitle("Diamonds vs Trash");
        primaryStage.show();
    }


    public static void main(String[] args) throws IOException {
        launch(args);
    }
}
