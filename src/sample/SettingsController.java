package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    public String textInput;

    @FXML // fx:id="txtFieldInputPath";
    private TextField txtFieldInputPath;
    @FXML // fx:id="txtAreaSettings";
    private TextArea txtAreaSettings;
    @FXML // fx:id="btnOK";
    private Button btnOK;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        settingsText();
    }

    public String getText(){
        return textInput = txtFieldInputPath.getText();
    }

    public void closeWindow(){
        textInput = txtFieldInputPath.getText();

        // close this window...
        btnOK.getScene().getWindow().hide();
    }

    private void settingsText(){
        String defaultDescription = "Default path is " + "C:\\Users\\" + System.getProperty("user.name") + "\\Music\\iTunes"
                + "\n\nIf iTunes Music Library.xml is not found, enable it:"
                + "\niTunes -> Edit -> Preferences -> Advanced -> check \"Share iTunes Library XML with other applications\"";
        txtAreaSettings.setText(defaultDescription);
    }
}
