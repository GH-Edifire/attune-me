package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class CompleteWindow {
    @FXML // fx:id="btnOK";
    private Button btnOK;

    public void closeWindow(){
        btnOK.getScene().getWindow().hide();
    }
}
