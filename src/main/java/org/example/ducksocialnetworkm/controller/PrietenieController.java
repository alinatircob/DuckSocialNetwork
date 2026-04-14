package org.example.ducksocialnetworkm.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.ducksocialnetworkm.depozit.paging.Page;
import org.example.ducksocialnetworkm.depozit.paging.Pageable;
import org.example.ducksocialnetworkm.depozit.paging.PageableImpl;
import org.example.ducksocialnetworkm.domeniu.observer.IObserver;
import org.example.ducksocialnetworkm.domeniu.observer.Observer;
import org.example.ducksocialnetworkm.domeniu.relatie.Relatie;
import org.example.ducksocialnetworkm.serviciu.Serviciu;
import org.example.ducksocialnetworkm.utils.events.ChangeEventType;
import org.example.ducksocialnetworkm.domeniu.user.User;

import java.util.List;
import java.util.Optional;

public class PrietenieController implements Observer<ChangeEventType> {

    private Serviciu serviciu;
    private ObservableList<Relatie> model = FXCollections.observableArrayList();

    private int currentPage = 0;
    private final int pageSize = 15;

    @FXML private TableView<Relatie> tableViewPrietenii;
    @FXML private TableColumn<Relatie, Long> tableColumnId1;
    @FXML private TableColumn<Relatie, Long> tableColumnId2;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    @FXML private TextField textFieldId1;
    @FXML private TextField textFieldId2;

    public void setService(Serviciu serviciu) {
        this.serviciu = serviciu;
        this.serviciu.addObserver(this);
        loadPage(0);
    }

    @FXML
    public void initialize() {
        tableColumnId1.setCellValueFactory(new PropertyValueFactory<>("id1"));
        tableColumnId2.setCellValueFactory(new PropertyValueFactory<>("id2"));
        tableViewPrietenii.setItems(model);

        prevButton.setOnAction(event -> loadPage(currentPage - 1));
        nextButton.setOnAction(event -> loadPage(currentPage + 1));
    }


    @Override
    public void update(ChangeEventType event) {
        if (event == ChangeEventType.FRIENDSHIP) {
            loadPage(currentPage);
        }
    }


    private void loadPage(int pageIndex) {
        if (pageIndex < 0) return;

        Pageable pageable = new PageableImpl(pageIndex, pageSize);

        try {
            Page<Relatie> page = serviciu.getPaginaPrietenii(pageable);
            model.setAll(page.getContent());

            currentPage = page.getPageNumber();
            int totalPages = page.getTotalPages();

            prevButton.setDisable(currentPage == 0 || totalPages == 0);
            nextButton.setDisable(totalPages == 0 || currentPage >= totalPages - 1);

            if (totalPages == 0) {
                pageLabel.setText("Pagina 0 din 0");
            } else {
                pageLabel.setText("Pagina " + (currentPage + 1) + " din " + totalPages);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Eroare Paginare", e.getMessage());
        }
    }


    private Long parseId(TextField field) throws NumberFormatException {
        if (field.getText().isEmpty()) {
            throw new IllegalArgumentException("Câmpul ID nu poate fi gol.");
        }
        return Long.parseLong(field.getText());
    }

    @FXML
    private void handleAddPrietenie() {
        try {
            Long id1 = parseId(textFieldId1);
            Long id2 = parseId(textFieldId2);

            String response = serviciu.adaugaPrietenie(id1, id2);

            showAlert(Alert.AlertType.INFORMATION, "Adaugare", response);
            textFieldId1.clear();
            textFieldId2.clear();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Eroare Format", "ID-urile trebuie să fie numere valide.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Eroare Adăugare", e.getMessage());
        }
    }

    @FXML
    private void handleDeletePrietenie() {
        Relatie selectedRelatie = tableViewPrietenii.getSelectionModel().getSelectedItem();
        if (selectedRelatie == null) {
            showAlert(Alert.AlertType.WARNING, "Atenție", "Selectați o prietenie din tabelă pentru ștergere.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmare Ștergere");
        confirm.setHeaderText("Sigur doriți să ștergeți această prietenie?");
        confirm.setContentText("Între ID " + selectedRelatie.getId1() + " și ID " + selectedRelatie.getId2());

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String response = serviciu.stergePrietenie(selectedRelatie.getId1(), selectedRelatie.getId2());
                showAlert(Alert.AlertType.INFORMATION, "Stergere", response);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Eroare Ștergere", e.getMessage());
            }
        }
    }


    @FXML
    private void handleShowNrComunitati() {
        try {
            int nrComunitati = serviciu.numarComunitati();
            showAlert(Alert.AlertType.INFORMATION,
                    "Număr Comunități",
                    "Rețeaua curentă are: " + nrComunitati + " comunități."
            );
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Eroare Comunități", e.getMessage());
        }
    }

    @FXML
    private void handleShowSociableComunitate() {
        try {
            List<Long> idMembers = serviciu.ceaMaiSociabilaComunitate();

            if (idMembers.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Comunitate Sociabilă", "Nu există prietenii în rețea.");
                return;
            }

            StringBuilder memberList = new StringBuilder();
            for (Long id : idMembers) {

                memberList.append("ID: ").append(id).append("\n");
            }

            showAlert(Alert.AlertType.INFORMATION,
                    "Cea Mai Sociabilă Comunitate",
                    "Membrii celei mai sociabile comunități sunt:\n\n" + memberList.toString()
            );

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Eroare Comunități", e.getMessage());
        }
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}