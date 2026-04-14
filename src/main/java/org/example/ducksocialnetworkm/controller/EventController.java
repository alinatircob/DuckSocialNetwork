package org.example.ducksocialnetworkm.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.ducksocialnetworkm.domeniu.event.Event;
import org.example.ducksocialnetworkm.domeniu.event.RaceEvent;
import org.example.ducksocialnetworkm.domeniu.observer.IObserver;
import org.example.ducksocialnetworkm.domeniu.observer.Observer;
import org.example.ducksocialnetworkm.domeniu.user.Persoana;
import org.example.ducksocialnetworkm.domeniu.user.Rata;
import org.example.ducksocialnetworkm.domeniu.user.SwimmingDuck;
import org.example.ducksocialnetworkm.domeniu.user.User;
import org.example.ducksocialnetworkm.serviciu.Serviciu;
import org.example.ducksocialnetworkm.utils.events.ChangeEventType;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EventController implements Observer<ChangeEventType> {

    @FXML private TableView<Event> activeEventsTable;
    @FXML private TableColumn<Event, String> nameColumn;
    @FXML private TableColumn<Event, String> statusColumn;
    @FXML private TableColumn<Event, String> distColumn;
    @FXML private TableColumn<Event, Void> actionColumn;

    @FXML private TableView<Event> historyTable;
    @FXML private TableColumn<Event, String> historyNameColumn;
    @FXML private TableColumn<Event, String> historyResultColumn;

    @FXML private Button createEventBtn;

    private Serviciu service;
    private User currentUser;
    private final ObservableList<Event> modelActive = FXCollections.observableArrayList();
    private final ObservableList<Event> modelHistory = FXCollections.observableArrayList();

    public void setService(Serviciu service, User user) {
        this.service = service;
        this.currentUser = user;
        this.service.addObserver(this);
        initModel();
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nume"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        distColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof RaceEvent re) {
                return new SimpleStringProperty(re.getDistante().toString());
            }
            return new SimpleStringProperty("-");
        });

        setupActionColumn();

        historyNameColumn.setCellValueFactory(new PropertyValueFactory<>("nume"));
        historyResultColumn.setCellValueFactory(cellData -> {
            String rez = cellData.getValue().getRezultatFinal();
            if(rez != null && rez.contains("\n")) return new SimpleStringProperty(rez.split("\n")[0] + "...");
            return new SimpleStringProperty(rez);
        });

        historyTable.setOnMouseClicked(event -> {
            Event ev = historyTable.getSelectionModel().getSelectedItem();
            if (ev != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Rezultate: " + ev.getNume());
                alert.setHeaderText("Detalii Eveniment Finalizat");
                TextArea area = new TextArea(ev.getRezultatFinal());
                area.setEditable(false);
                area.setWrapText(true);
                alert.getDialogPane().setContent(area);
                alert.showAndWait();
            }
        });
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnJoin = new Button("Participă");
            private final Button btnSub = new Button("Abonează-te");
            private final Button btnStart = new Button("Start");
            private final HBox container = new HBox(10, btnJoin, btnSub, btnStart);

            {
                btnJoin.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                btnStart.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold;");
                btnSub.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Event event = getTableView().getItems().get(getIndex());
                boolean isInProgress = "IN_PROGRESS".equals(event.getStatus());

                btnJoin.setVisible(false); btnJoin.setManaged(false);
                btnSub.setVisible(false); btnSub.setManaged(false);
                btnStart.setVisible(false); btnStart.setManaged(false);

                if (currentUser instanceof Persoana) {
                    if (currentUser.getId().equals(event.getIdCreator())) {
                        btnStart.setVisible(true);
                        btnStart.setManaged(true);
                        btnStart.setDisable("IN_PROGRESS".equals(event.getStatus()));
                        btnStart.setOnAction(e -> handleStart(event));
                        btnStart.setText("Start (Ești creator)");
                    } else {
                        btnStart.setVisible(false);
                        btnStart.setManaged(false);
                    }
                }

                boolean isParticipant = isParticipant(event, currentUser);
                if (currentUser instanceof Rata) {
                    if (!isParticipant) {
                        btnJoin.setVisible(true);
                        btnJoin.setManaged(true);
                        btnJoin.setText("Participă");
                        btnJoin.setDisable(isInProgress);
                        btnJoin.setOnAction(e -> handleJoin(event));
                    } else {
                        btnJoin.setVisible(true);
                        btnJoin.setManaged(true);
                        btnJoin.setText("Înscris ✅");
                        btnJoin.setDisable(true);
                    }
                }

                if (!isParticipant) {
                    btnSub.setVisible(true);
                    btnSub.setManaged(true);

                    boolean isSubscribed = isSubscribed(event, currentUser);
                    if (isSubscribed) {
                        btnSub.setText("Dezabonează-te");
                        btnSub.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");
                        btnSub.setOnAction(e -> handleUnsubscribe(event));
                    } else {
                        btnSub.setText("Abonează-te");
                        btnSub.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                        btnSub.setOnAction(e -> handleSubscribe(event));
                    }
                }

                setGraphic(container);
            }
        });
    }

    private void initModel() {
        if (createEventBtn != null) {
            createEventBtn.setVisible(currentUser instanceof Persoana);
        }

        List<Event> allEvents = (List<Event>) service.getEvents();

        List<Event> active = allEvents.stream()
                .filter(e -> !"FINISHED".equals(e.getStatus()))
                .sorted(Comparator.comparing(Event::getId).reversed())
                .collect(Collectors.toList());

        List<Event> history = service.getIstoricEvenimenteUser(currentUser.getId());

        modelActive.setAll(active);
        modelHistory.setAll(history);

        activeEventsTable.setItems(modelActive);
        historyTable.setItems(modelHistory);
    }

    @Override
    public void update(ChangeEventType eventType) {
        Platform.runLater(() -> {
            if (eventType == ChangeEventType.EVENT) {
                initModel();
            }
        });
    }

    private void handleStart(Event event) {
        service.startEvent(event.getId());
    }

    private void handleJoin(Event event) {
        String res = service.inscrieParticipant(event.getId(), currentUser.getId());
        if (!res.contains("succes")) showError(res);
    }

    private void handleSubscribe(Event event) {
        String res = service.adaugaObserver(currentUser.getId(), event.getId());
        if (!res.contains("succes")) showError(res);
    }

    private void handleUnsubscribe(Event event) {
        String res = service.stergeObserver(currentUser.getId(), event.getId());
        if (!res.contains("succes")) showError(res);
    }

    @FXML
    public void handleCreateEventAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ducksocialnetworkm/create-event-view.fxml"));

            javafx.scene.Parent root = loader.load();
            CreateEventController ctrl = loader.getController();
            ctrl.setService(service, currentUser);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Creează Eveniment");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Eroare la deschiderea ferestrei: " + e.getMessage());
        }
    }


    private boolean isSubscribed(Event ev, User user) {
        for (IObserver obs : ev.getObservers()) {
            if (obs instanceof User && ((User)obs).getId().equals(user.getId())) return true;
        }
        return false;
    }

    private boolean isParticipant(Event ev, User user) {
        if (ev instanceof RaceEvent re) {
            return re.getParticipanti().stream().anyMatch(p -> p.getId().equals(user.getId()));
        }
        return false;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.show();
    }
}