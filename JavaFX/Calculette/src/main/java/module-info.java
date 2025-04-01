module com.example.calculette {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.calculette to javafx.fxml;
    exports com.example.calculette;
}