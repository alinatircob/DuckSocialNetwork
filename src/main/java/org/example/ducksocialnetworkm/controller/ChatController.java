package org.example.ducksocialnetworkm.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.example.ducksocialnetworkm.domeniu.mesaj.Mesaj;
import org.example.ducksocialnetworkm.domeniu.mesaj.ReplyMesaj;
import org.example.ducksocialnetworkm.domeniu.observer.Observer;
import org.example.ducksocialnetworkm.domeniu.user.User;
import org.example.ducksocialnetworkm.serviciu.Serviciu;
import org.example.ducksocialnetworkm.utils.events.ChangeEventType;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class ChatController implements Observer<ChangeEventType> {

    @FXML private ListView<Mesaj> listMessages;
    @FXML private TextField txtMessage;
    @FXML private Label lblReply;
    @FXML private Label lblChatPartner;

    private Serviciu serviciu;
    private User currentUser;
    private User otherUser;
    private Mesaj selectedMessageForReply = null;

    private Stage stage;

    /**
     * Metoda esențială pentru a gestiona închiderea ferestrei.
     * Trebuie apelată din UserAccountController imediat după loader.getController()
     */
    public void setStage(Stage stage) {
        this.stage = stage;

        this.stage.setOnCloseRequest(event -> {
            if (serviciu != null) {
                serviciu.removeObserver(this);
            }
        });
    }

    public void initData(Serviciu s, User me, User other) {
        this.serviciu = s;
        this.currentUser = me;
        this.otherUser = other;

        if (lblChatPartner != null) {
            lblChatPartner.setText("Conversație cu " + other.getUsername());
        }

        s.addObserver(this);
        setupListView();
        loadMessages();
    }

    private void setupListView() {
        // Listener pentru Reply
        listMessages.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedMessageForReply = newVal;
            if (newVal != null) {
                String shortText = newVal.getText();
                if (shortText.length() > 25) shortText = shortText.substring(0, 25) + "...";

                lblReply.setText("↩ Răspunzi la: " + newVal.getFrom().getUsername() + ": \"" + shortText + "\"");
                lblReply.setVisible(true);
                lblReply.setManaged(true);
            } else {
                lblReply.setVisible(false);
                lblReply.setManaged(false);
            }
        });

        listMessages.setCellFactory(param -> new ListCell<Mesaj>() {
            @Override
            protected void updateItem(Mesaj item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setStyle(null);

                    boolean isMe = item.getFrom().getId().equals(currentUser.getId());

                    VBox bubble = new VBox(5);
                    bubble.getStyleClass().add(isMe ? "bubble-me" : "bubble-other");
                    bubble.setMaxWidth(300);

                    if (item instanceof ReplyMesaj) {
                        ReplyMesaj rm = (ReplyMesaj) item;
                        if (rm.getOriginalMessage() != null) {
                            String replyName = rm.getOriginalMessage().getFrom().getUsername();
                            String replyMsg = rm.getOriginalMessage().getText();
                            if (replyMsg.length() > 30) replyMsg = replyMsg.substring(0, 30) + "...";

                            Label replyLbl = new Label("↩ " + replyName + ": " + replyMsg);
                            replyLbl.setStyle("-fx-font-style: italic; -fx-font-size: 11px; -fx-opacity: 0.8; -fx-padding: 0 0 5 0; -fx-border-color: rgba(0,0,0,0.1); -fx-border-width: 0 0 1 0;");
                            bubble.getChildren().add(replyLbl);
                        }
                    }

                    Text textNode = new Text(item.getText());
                    textNode.setStyle(isMe ? "-fx-fill: white; -fx-font-size: 14px;" : "-fx-fill: black; -fx-font-size: 14px;");
                    TextFlow textFlow = new TextFlow(textNode);
                    bubble.getChildren().add(textFlow);

                    HBox metaBox = new HBox(5);
                    metaBox.setAlignment(Pos.CENTER_RIGHT);

                    String timeStr = item.getData().format(DateTimeFormatter.ofPattern("HH:mm"));
                    Label timeLabel = new Label(timeStr);
                    timeLabel.setStyle(isMe ? "-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 10px;"
                            : "-fx-text-fill: rgba(0,0,0,0.5); -fx-font-size: 10px;");

                    metaBox.getChildren().add(timeLabel);

                    if (isMe) {
                        Label statusLabel = new Label();
                        if ("READ".equals(item.getStatus())) {
                            statusLabel.setText("✔✔"); // Citit
                            statusLabel.setStyle("-fx-text-fill: #00e5ff; -fx-font-weight: bold; -fx-font-size: 10px;");
                        } else {
                            statusLabel.setText("✔"); // Trimis
                            statusLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 10px;");
                        }
                        metaBox.getChildren().add(statusLabel);
                    }

                    bubble.getChildren().add(metaBox);

                    HBox row = new HBox(10);
                    if (!isMe) {
                        row.setAlignment(Pos.CENTER_LEFT);
                        Circle avatar = new Circle(16);
                        avatar.setFill(Color.LIGHTGRAY);
                        Label initial = new Label(otherUser.getUsername().substring(0,1).toUpperCase());
                        initial.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
                        StackPane avatarStack = new StackPane(avatar, initial);
                        row.getChildren().addAll(avatarStack, bubble);
                    } else {
                        row.setAlignment(Pos.CENTER_RIGHT);
                        row.getChildren().add(bubble);
                    }

                    setGraphic(row);
                    setText(null);
                }
            }
        });
    }

    private void loadMessages() {
        List<Mesaj> msgs = serviciu.getConversatie(currentUser.getId(), otherUser.getId());
        msgs.sort(Comparator.comparing(Mesaj::getData));

        Platform.runLater(() -> {
            listMessages.setItems(FXCollections.observableArrayList(msgs));
            listMessages.scrollTo(msgs.size() - 1);
        });

        boolean amMesajeNecitite = msgs.stream()
                .anyMatch(m -> m.getFrom().getId().equals(otherUser.getId())
                        && "UNREAD".equals(m.getStatus()));

        if (amMesajeNecitite) {
            serviciu.markConversationAsRead(currentUser.getId(), otherUser.getId());
        }
    }

    @FXML
    private void handleSend() {
        String txt = txtMessage.getText();
        if (txt.isEmpty()) return;

        serviciu.trimiteMesaj(currentUser.getId(), otherUser.getId(), txt, selectedMessageForReply);

        txtMessage.clear();
        selectedMessageForReply = null;
        listMessages.getSelectionModel().clearSelection();
        lblReply.setVisible(false);
        lblReply.setManaged(false);
    }

    @Override
    public void update(ChangeEventType event) {
        if (event == ChangeEventType.MESSAGE) {
            Platform.runLater(() -> {
                if (stage != null && stage.isShowing()) {
                    loadMessages();
                }
            });
        }
    }
}