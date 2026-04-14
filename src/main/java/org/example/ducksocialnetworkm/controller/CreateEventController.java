package org.example.ducksocialnetworkm.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.ducksocialnetworkm.domeniu.user.User;
import org.example.ducksocialnetworkm.serviciu.Serviciu;

import java.util.ArrayList;
import java.util.List;

public class CreateEventController {

    @FXML private TextField nameField;
    @FXML private TextField distancesField;

    private Serviciu service;
    private User currentUser;

    public void setService(Serviciu service, User user) {
        this.service = service;
        this.currentUser = user;
    }

    @FXML
    public void handleSave() {
        String nume = nameField.getText();
        String distStr = distancesField.getText();

        if (nume.isEmpty() || distStr.isEmpty()) {
            showError("Toate câmpurile sunt obligatorii!");
            return;
        }

        List<Double> distante = new ArrayList<>();
        try {
            for (String s : distStr.split(",")) {
                distante.add(Double.parseDouble(s.trim()));
            }
        } catch (NumberFormatException e) {
            showError("Format distanțe invalid! Folosește numere separate prin virgulă (ex: 50.5, 100).");
            return;
        }

        Long id = System.currentTimeMillis() / 1000;

        String result = service.adaugaEvent(id, nume, distante, currentUser.getId());

        if (result.contains("succes")) {
            ((Stage) nameField.getScene().getWindow()).close();
        } else {
            showError(result);
        }
    }

    @FXML
    public void handleCancel() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }
}