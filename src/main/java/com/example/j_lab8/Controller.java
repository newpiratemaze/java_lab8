package com.example.j_lab8;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class Controller {
    @FXML private TextField usernameFld, udpField, ipFriendFld, portFriendFld;
    @FXML private Button connectBtn;
    @FXML private ProgressBar volumeBar;
    @FXML private TextField statusTxt;

    private Phone phone = new Phone();
    private boolean isConnected = false;

    @FXML
    public void initialize() {
        statusTxt.setText("Ожидание вызова...");

        if (!isConnected) {
            try {
                int myPort = Integer.parseInt(udpField.getText());
                String targetIp = ipFriendFld.getText();
                int targetPort = Integer.parseInt(portFriendFld.getText());

                // Устанавливаем статус попытки подключения
                statusTxt.setText("Поиск собеседника...");

                phone.startCall(myPort, targetIp, targetPort, vol ->
                        Platform.runLater(() -> {
                            volumeBar.setProgress(vol);
                            // Если пошел звук от микрофона, значит мы активны
                            if (vol > 0.01 && statusTxt.getText().equals("Поиск собеседника...")) {
                                statusTxt.setText("Вы в эфире");
                            }
                        })
                );

                connectBtn.setText("Завершить");
                isConnected = true;
            } catch (Exception e) {
                statusTxt.setText("Ошибка данных!");
            }
        } else {
            phone.stopCall();
            volumeBar.setProgress(0);
            statusTxt.setText("Ожидание вызова...");
            connectBtn.setText("Подключиться");
            isConnected = false;
        }
    }
}