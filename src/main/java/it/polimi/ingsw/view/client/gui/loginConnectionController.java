package it.polimi.ingsw.view.client.gui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.awt.event.MouseEvent;
import java.io.IOException;

public class loginConnectionController {

    @FXML
    public Label text;
    @FXML
    public TextField port;
    @FXML
    public TextField ipAddress;
    @FXML
    private Button connectButton;


    private String getIpAddress() {
        return ipAddress.getText();
    }

    private String getPort() {
        return port.getText();
    }


    @FXML
    public void connection(ActionEvent actionEvent) {
        Gui gui = new Gui(getIpAddress(), Integer.parseInt(getPort()), (Stage) connectButton.getScene().getWindow(), connectButton.getScene());
        Thread th = new Thread(() -> {
            gui.start();
        });
        th.start();

    }
}



