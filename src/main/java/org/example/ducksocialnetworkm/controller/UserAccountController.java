package org.example.ducksocialnetworkm.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.ducksocialnetworkm.domeniu.event.Event;
import org.example.ducksocialnetworkm.domeniu.observer.IObserver;
import org.example.ducksocialnetworkm.domeniu.page.ProfilePage;
import org.example.ducksocialnetworkm.domeniu.relatie.CererePrietenie;
import org.example.ducksocialnetworkm.domeniu.user.Persoana;
import org.example.ducksocialnetworkm.domeniu.user.Rata;
import org.example.ducksocialnetworkm.domeniu.user.User;
import org.example.ducksocialnetworkm.serviciu.Serviciu;
import org.example.ducksocialnetworkm.utils.events.ChangeEventType;
import org.example.ducksocialnetworkm.domeniu.observer.Observer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserAccountController implements Observer<ChangeEventType> {

    private Serviciu serviciu;
    private User loggedUser;
    private ProfilePage profilePage;

    private Map<Long, String> lastEventStatus = new HashMap<>();
    private int eventNotificationCount = 0;

    @FXML private EventController eventViewController;

    @FXML private Circle avatarCircle;
    @FXML private Label lblAvatarText;
    @FXML private Label lblNumeComplet;
    @FXML private Label lblUsername;
    @FXML private Label lblEmail;
    @FXML private Label lblTip;

    @FXML private Tab tabConversatii;
    @FXML private Tab tabCereri;

    @FXML private Tab tabEvenimente;

    @FXML private ListView<User> listViewPrieteni;
    @FXML private ListView<CererePrietenie> listViewCereri;


    @FXML private TableView<User> tableViewUsers;
    @FXML private TableColumn<User, Long> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colNume;
    @FXML private TableColumn<User, String> colPrenume;
    @FXML private TableColumn<User, String> colEmail;

    public void setService(Serviciu serviciu, User user) {
        this.serviciu = serviciu;
        this.serviciu.addObserver(this);

        this.loggedUser = reloadUserWithFriends(user.getId());
        this.profilePage = new ProfilePage(loggedUser, loggedUser.getPrieteni());

        loadProfileData();
        loadFriendsList();
        loadRequests();
        loadAllUsers();

        if (eventViewController != null) {
            eventViewController.setService(serviciu, loggedUser);
        } else {
            System.err.println("AVERTISMENT: EventController nu a fost injectat! Verificați fx:include în FXML.");
        }

        for (Event e : serviciu.getEvents()) {
            if (isSubscribed(e)) {
                lastEventStatus.put(e.getId(), e.getStatus());
            }
        }

        if (tabEvenimente != null) {
            tabEvenimente.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    resetEventBadge();
                }
            });
        }
    }

    private void checkEventStatusChanges() {
        for (Event e : serviciu.getEvents()) {
            if (isSubscribed(e)) {
                String oldStatus = lastEventStatus.getOrDefault(e.getId(), "OPEN");
                String newStatus = e.getStatus();

                if ("OPEN".equals(oldStatus) && "IN_PROGRESS".equals(newStatus)) {
                    showNotificationAlert("🏁 Cursa a început!", "Evenimentul '" + e.getNume() + "' este acum în desfășurare.");
                    incrementEventBadge();
                }

                if ("FINISHED".equals(newStatus) && !"FINISHED".equals(oldStatus)) {
                    String rez = e.getRezultatFinal();
                    String shortRez = rez.length() > 100 ? rez.substring(0, 100) + "..." : rez;

                    showNotificationAlert("🏆 Cursa s-a terminat!",
                            "Evenimentul '" + e.getNume() + "' s-a încheiat.\n\n" + shortRez);
                    //incrementEventBadge();
                }

                lastEventStatus.put(e.getId(), newStatus);
            }
        }
    }

    private boolean isSubscribed(Event e) {
        return e.getObservers().stream()
                .anyMatch(obs -> obs instanceof User && ((User) obs).getId().equals(loggedUser.getId()));
    }

    private void incrementEventBadge() {
        if (tabEvenimente.isSelected()) return;

        eventNotificationCount++;
        updateEventTabTitle();
    }

    private void resetEventBadge() {
        eventNotificationCount = 0;
        updateEventTabTitle();
    }

    private void updateEventTabTitle() {
        if (eventNotificationCount > 0) {
            tabEvenimente.setText("🏁 Evenimente (" + eventNotificationCount + ")");
            tabEvenimente.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
        } else {
            tabEvenimente.setText("🏁 Evenimente");
            tabEvenimente.setStyle("");
        }
    }

    private void showNotificationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notificare Eveniment");
        alert.setHeaderText(title);
        alert.setContentText(content);

        alert.show();
    }

    private User reloadUserWithFriends(Long userId) {
        Iterable<User> allUsers = serviciu.getUseriCuPrieteni();
        return StreamSupport.stream(allUsers.spliterator(), false)
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElse(loggedUser);
    }

    private void loadProfileData() {
        User u = profilePage.getUser();
        lblUsername.setText("@" + u.getUsername());
        lblEmail.setText(u.getEmail());

        if (u instanceof Persoana p) {
            lblNumeComplet.setText(p.getNume() + " " + p.getPrenume());
            lblTip.setText("Tip: Persoană (" + p.getOcupatie() + ")");
        } else if (u instanceof Rata r) {
            lblNumeComplet.setText("Rata " + r.getUsername());
            lblTip.setText("Tip: Rata (" + r.getTip() + ")");
        }

        generateDynamicAvatar(u.getUsername(), avatarCircle, lblAvatarText);
    }

    private void generateDynamicAvatar(String username, Circle circle, Label label) {
        if (username != null && !username.isEmpty()) {
            label.setText(username.substring(0, 1).toUpperCase());
        } else {
            label.setText("?");
        }
        String[] culori = {
                "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5",
                "#2196F3", "#009688", "#4CAF50", "#FF9800", "#795548", "#607D8B"
        };
        int index = Math.abs(username.hashCode()) % culori.length;
        circle.setFill(Color.web(culori[index]));
    }


    private void loadFriendsList() {
        List<User> prieteni = profilePage.getPrieteni();
        if (prieteni == null) return;

        Map<Long, Integer> allUnreadCounts = serviciu.getUnreadCounts(loggedUser.getId());

        long totalConversatiiNecitite = allUnreadCounts.keySet().stream()
                .filter(senderId -> prieteni.stream().anyMatch(friend -> friend.getId().equals(senderId)))
                .count();

        if (tabConversatii != null) {
            if (totalConversatiiNecitite > 0) {
                tabConversatii.setText("💬 Conversații (" + totalConversatiiNecitite + ")");
                tabConversatii.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
            } else {
                tabConversatii.setText("💬 Conversații");
                tabConversatii.setStyle("");
            }
        }

        ObservableList<User> friendsModel = FXCollections.observableArrayList(prieteni);
        friendsModel.sort((u1, u2) -> {
            boolean u1Unread = allUnreadCounts.containsKey(u1.getId());
            boolean u2Unread = allUnreadCounts.containsKey(u2.getId());
            return Boolean.compare(u2Unread, u1Unread);
        });

        listViewPrieteni.setItems(friendsModel);

        listViewPrieteni.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setStyle(null);

                    HBox root = new HBox(10);
                    root.setAlignment(Pos.CENTER_LEFT);
                    root.setPadding(new javafx.geometry.Insets(5));

                    Circle smallCircle = new Circle(20);
                    Label smallLabel = new Label();
                    smallLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
                    generateDynamicAvatar(item.getUsername(), smallCircle, smallLabel);
                    StackPane avatarStack = new StackPane(smallCircle, smallLabel);

                    String nameText = item.getUsername();
                    if (item instanceof Persoana p) nameText += " (" + p.getNume() + ")";
                    Label lblName = new Label(nameText);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    if (allUnreadCounts.containsKey(item.getId())) {
                        int count = allUnreadCounts.get(item.getId());
                        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                        Label badge = new Label(String.valueOf(count));
                        badge.setStyle(
                                "-fx-background-color: #25D366;" +
                                        "-fx-text-fill: white;" +
                                        "-fx-background-radius: 10;" +
                                        "-fx-padding: 2 7 2 7;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-font-size: 11px;"
                        );

                        root.getChildren().addAll(avatarStack, lblName, spacer, badge);
                    } else {
                        lblName.setStyle("-fx-font-weight: normal; -fx-font-size: 14px;");
                        root.getChildren().addAll(avatarStack, lblName, spacer);
                    }

                    setGraphic(root);
                }
            }
        });
    }

    private void loadRequests() {
        List<CererePrietenie> cereri = serviciu.getCereriPrimite(loggedUser.getId());
        listViewCereri.setItems(FXCollections.observableArrayList(cereri));

        if (tabCereri != null) {
            if (!cereri.isEmpty()) {
                tabCereri.setText("🔔 Cereri (" + cereri.size() + ")");
                tabCereri.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22;");
            } else {
                tabCereri.setText("Cereri Primite");
                tabCereri.setStyle("");
            }
        }

        listViewCereri.setCellFactory(param -> new ListCell<CererePrietenie>() {
            @Override
            protected void updateItem(CererePrietenie item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setStyle(null);
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);
                    Label label = new Label("📩 De la ID: " + item.getIdExpeditor() + "\n📅 " + item.getData().toLocalDate());
                    label.setStyle("-fx-font-size: 12px;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button btnAccept = new Button("✔");
                    btnAccept.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
                    btnAccept.setOnAction(e -> handleRaspundeCerere(item, "APPROVED"));

                    Button btnReject = new Button("✖");
                    btnReject.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
                    btnReject.setOnAction(e -> handleRaspundeCerere(item, "REJECTED"));

                    box.getChildren().addAll(label, spacer, btnAccept, btnReject);
                    setGraphic(box);
                }
            }
        });
    }

    private void loadAllUsers() {
        Iterable<User> users = serviciu.getUseriCuPrieteni();
        List<User> list = StreamSupport.stream(users.spliterator(), false)
                .filter(u -> !u.getId().equals(loggedUser.getId()))
                .collect(Collectors.toList());

        tableViewUsers.setItems(FXCollections.observableArrayList(list));

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colNume.setCellValueFactory(cellData -> {
            if(cellData.getValue() instanceof Persoana p) return new javafx.beans.property.SimpleStringProperty(p.getNume());
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colPrenume.setCellValueFactory(cellData -> {
            if(cellData.getValue() instanceof Persoana p) return new javafx.beans.property.SimpleStringProperty(p.getPrenume());
            return new javafx.beans.property.SimpleStringProperty("-");
        });
    }


    @FXML
    public void handleOpenSelectedChat() {
        User friend = listViewPrieteni.getSelectionModel().getSelectedItem();
        if (friend == null) {
            showAlert(Alert.AlertType.WARNING, "Selectează un prieten din listă!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ducksocialnetworkm/chat-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Chat: " + loggedUser.getUsername() + " ➡ " + friend.getUsername());

            ChatController ctrl = loader.getController();
            ctrl.initData(serviciu, loggedUser, friend);
            ctrl.setStage(stage);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Nu s-a putut deschide fereastra de chat: " + e.getMessage());
        }
    }

    @FXML
    public void handleTrimiteCerere() {
        User target = tableViewUsers.getSelectionModel().getSelectedItem();
        if (target == null) {
            showAlert(Alert.AlertType.WARNING, "Selectează un utilizator din tabel!");
            return;
        }
        String res = serviciu.trimiteCererePrietenie(loggedUser.getId(), target.getId());
        showAlert(res.contains("succes") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR, res);
    }

    private void handleRaspundeCerere(CererePrietenie item, String status) {
        try {
            serviciu.raspundeCerere(item.getIdExpeditor(), loggedUser.getId(), status);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    public void handleLogout() {
        serviciu.removeObserver(this);
        ((Stage) lblUsername.getScene().getWindow()).close();
    }

    @Override
    public void update(ChangeEventType event) {
        Platform.runLater(() -> {
            if (event == ChangeEventType.FRIENDSHIP) {
                this.loggedUser = reloadUserWithFriends(loggedUser.getId());
                this.profilePage = new ProfilePage(loggedUser, loggedUser.getPrieteni());
                loadFriendsList();
                loadRequests();
            }
            if (event == ChangeEventType.MESSAGE) {
                loadFriendsList();
            }

            if (event == ChangeEventType.EVENT) {
                checkEventStatusChanges();
            }
        });
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg).show();
    }
}