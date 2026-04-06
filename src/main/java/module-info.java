module com.example.j_lab8 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.j_lab8 to javafx.fxml;
    exports com.example.j_lab8;
}