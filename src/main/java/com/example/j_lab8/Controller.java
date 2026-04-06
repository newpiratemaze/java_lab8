package com.example.j_lab8;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class Controller {
    @FXML private TextField usernameFld;
    @FXML private TextField udpField;
    @FXML private TextField ipFriendFld;
    @FXML private TextField portFriendFld;
    @FXML private Button connectBtn;

    private Phone phone;
    private boolean isConnected = false;

    @FXML
    public void initialize() {
        phone = new Phone();
        if (!isConnected) {
            try {
                String nick = usernameFld.getText();
                int myPort = Integer.parseInt(udpField.getText());
                String targetIp = ipFriendFld.getText();
                int targetPort = Integer.parseInt(portFriendFld.getText());

                phone.startCall(myPort, targetIp, targetPort);
                connectBtn.setText("Отключиться");
                isConnected = true;
            } catch (Exception e) {
                System.err.println("Ошибка параметров: " + e.getMessage());
            }
        } else {
            phone.stopCall();
            connectBtn.setText("Подключиться");
            isConnected = false;
        }
    }



}
