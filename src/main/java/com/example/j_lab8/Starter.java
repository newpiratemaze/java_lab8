package com.example.j_lab8;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Starter extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Starter.class.getResource("lab8View.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 550, 400);
        stage.setTitle("Telefon megafon saxofon patifon florty flor januliji buflon");
        stage.setScene(scene);
        stage.show();
    }
}
