package org.example.ducksocialnetworkm.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.ducksocialnetworkm.domeniu.user.User;
import org.example.ducksocialnetworkm.serviciu.Serviciu;

public class LoginController {
    @FXML private Label lblTitle;
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;

    private Serviciu serviciu;
    private Stage stage;
    private User loggedUser;

    public void setService(Serviciu serviciu, Stage stage, String title) {
        this.serviciu = serviciu;
        this.stage = stage;
        this.lblTitle.setText(title);
    }

    @FXML
    private void handleLogin() {
        String u = txtUser.getText();
        String p = txtPass.getText();
        User user = serviciu.login(u, p);
        if (user != null) {
            loggedUser = user;
            stage.close();
        } else {
            new Alert(Alert.AlertType.ERROR, "Date incorecte!").show();
        }
    }

    public User getLoggedUser() { return loggedUser; }
}