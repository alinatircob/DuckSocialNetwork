package org.example.ducksocialnetworkm.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.ducksocialnetworkm.domeniu.card.Card;
import org.example.ducksocialnetworkm.domeniu.user.Rata;
import org.example.ducksocialnetworkm.domeniu.observer.Observer;
import org.example.ducksocialnetworkm.serviciu.Serviciu;
import org.example.ducksocialnetworkm.utils.events.ChangeEventType;

import java.util.stream.Collectors;

public class CardController implements Observer<ChangeEventType> {

    private Serviciu service;
    private final ObservableList<Card<? extends Rata>> model = FXCollections.observableArrayList();

    @FXML private TableView<Card<? extends Rata>> tableViewCarduri;
    @FXML private TableColumn<Card<? extends Rata>, Long> columnId;
    @FXML private TableColumn<Card<? extends Rata>, String> columnNume;
    @FXML private TableColumn<Card<? extends Rata>, String> columnTip;
    @FXML private TableColumn<Card<? extends Rata>, String> columnPerformanta;
    @FXML private TableColumn<Card<? extends Rata>, String> columnMembri;

    @FXML private TextField textIdCard;
    @FXML private TextField textNumeCard;
    @FXML private ComboBox<String> comboTipCard;
    @FXML private TextField textIdUser;

    public void setService(Serviciu service) {
        this.service = service;
        service.addObserver(this);
        initModel();
    }

    @FXML
    public void initialize() {
        comboTipCard.setItems(FXCollections.observableArrayList("SWIMMINGCARD", "FLYINGCARD"));

        columnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnNume.setCellValueFactory(new PropertyValueFactory<>("nume"));
        columnTip.setCellValueFactory(new PropertyValueFactory<>("tip"));

        columnPerformanta.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f", cellData.getValue().getPerformantaMedie()))
        );

        columnMembri.setCellValueFactory(cellData -> {
            String membri = cellData.getValue().getRate().stream()
                    .map(rata -> rata.getId().toString())
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(membri.isEmpty() ? "-" : membri);
        });

        tableViewCarduri.setItems(model);
    }

    private void initModel() {
        model.setAll(service.getToateCardurile());
    }

    @FXML
    public void handleAddCard() {
        try {
            Long id = Long.parseLong(textIdCard.getText());
            String nume = textNumeCard.getText();
            String tip = comboTipCard.getValue();

            String rezultat = service.adaugaCard(tip, id, nume);

            if (rezultat.contains("succes")) {
                showMessage(Alert.AlertType.INFORMATION, "Succes", rezultat);
                textIdCard.clear();
                textNumeCard.clear();
            } else {
                showMessage(Alert.AlertType.ERROR, "Eroare", rezultat);
            }
        } catch (NumberFormatException e) {
            showMessage(Alert.AlertType.ERROR, "Eroare Validare", "ID-ul trebuie sa fie un numar valid!");
        } catch (Exception e) {
            showMessage(Alert.AlertType.ERROR, "Eroare", e.getMessage());
        }
    }

    @FXML
    public void handleDeleteCard() {
        Card<? extends Rata> selected = tableViewCarduri.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "Atentie", "Selecteaza un card din tabel pentru a-l sterge!");
            return;
        }
        String rezultat = service.stergeCard(selected.getId());
        showMessage(Alert.AlertType.INFORMATION, "Info", rezultat);
    }

    @FXML
    public void handleAdaugaUserLaCard() {
        Card<? extends Rata> selected = tableViewCarduri.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "Atentie", "Selecteaza un card din tabel in care sa adaugi rata!");
            return;
        }
        try {
            Long idUser = Long.parseLong(textIdUser.getText());
            String rezultat = service.adaugaRataCard(idUser, selected.getId());

            if (rezultat.contains("succes")) {
                showMessage(Alert.AlertType.INFORMATION, "Succes", rezultat);
                textIdUser.clear();
            } else {
                showMessage(Alert.AlertType.ERROR, "Eroare Adaugare", rezultat);
            }
        } catch (NumberFormatException e) {
            showMessage(Alert.AlertType.ERROR, "Eroare", "ID-ul utilizatorului trebuie sa fie numar!");
        }
    }

    @FXML
    public void handleStergeUserDinCard() {
        Card<? extends Rata> selected = tableViewCarduri.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "Atentie", "Selecteaza un card din tabel din care sa scoti rata!");
            return;
        }
        try {
            Long idUser = Long.parseLong(textIdUser.getText());
            String rezultat = service.stergeRataCard(idUser, selected.getId());

            if (rezultat.contains("succes")) {
                showMessage(Alert.AlertType.INFORMATION, "Succes", rezultat);
                textIdUser.clear();
            } else {
                showMessage(Alert.AlertType.ERROR, "Eroare Stergere", rezultat);
            }
        } catch (NumberFormatException e) {
            showMessage(Alert.AlertType.ERROR, "Eroare", "ID-ul utilizatorului trebuie sa fie numar!");
        }
    }

    private void showMessage(Alert.AlertType type, String header, String text) {
        Alert alert = new Alert(type);
        alert.setTitle("Mesaj");
        alert.setHeaderText(header);
        alert.setContentText(text);
        alert.showAndWait();
    }

    @Override
    public void update(ChangeEventType event) {

        if (event == ChangeEventType.CARD || event == ChangeEventType.USER) {
            initModel();
        }
    }
}