package org.example.ducksocialnetworkm.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.ducksocialnetworkm.depozit.paging.Page;
import org.example.ducksocialnetworkm.depozit.paging.Pageable;
import org.example.ducksocialnetworkm.depozit.paging.PageableImpl;
import org.example.ducksocialnetworkm.domeniu.observer.Observer;
import org.example.ducksocialnetworkm.domeniu.user.Persoana;
import org.example.ducksocialnetworkm.domeniu.user.Rata;
import org.example.ducksocialnetworkm.domeniu.user.TipRata;
import org.example.ducksocialnetworkm.domeniu.user.User;
import org.example.ducksocialnetworkm.serviciu.Serviciu;
import org.example.ducksocialnetworkm.utils.events.ChangeEventType;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UserController implements Observer<ChangeEventType> {

    private Serviciu serviciu;
    private ObservableList<User> model = FXCollections.observableArrayList();

    private int currentPage = 0;
    private final int pageSize = 10;
    private String selectedUserType = null;
    private TipRata selectedRataTip = null;
    private String currentAddType = "PERSOANA";

    @FXML private TableView<User> tableView;
    @FXML private TableColumn<User, Long> tableColumnId;
    @FXML private TableColumn<User, String> tableColumnUsername;
    @FXML private TableColumn<User, String> tableColumnEmail;
    @FXML private TableColumn<User, String> tableColumnType;
    @FXML private TableColumn<User, String> tableColumnDetalii;
    @FXML private TableColumn<User, String> tableColumnCardInfo;

    @FXML private ComboBox<String> comboBoxFilterUserType;
    @FXML private ComboBox<TipRata> comboBoxFilterRataTip;

    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;
    @FXML private Button deleteButton;
    @FXML private Button friendsButton;
    @FXML private Button btnShowPass;
    @FXML private Button btnChat;

    @FXML private ComboBox<String> comboBoxAddType;
    @FXML private TextField textFieldId;
    @FXML private TextField textFieldUsername;
    @FXML private TextField textFieldEmail;
    @FXML private PasswordField passwordField;

    @FXML private Label labelNume, labelPrenume, labelDataNasterii, labelOcupatie;
    @FXML private TextField textFieldNume, textFieldPrenume, textFieldOcupatie;
    @FXML private DatePicker datePickerDataNasterii;

    @FXML private Label labelRezistenta, labelViteza, labelTipRataAdd;
    @FXML private TextField textFieldRezistenta, textFieldViteza;
    @FXML private ComboBox<TipRata> comboBoxTipRataAdd;


    public void setService(Serviciu serviciu) {
        this.serviciu = serviciu;
        this.serviciu.addObserver(this);
        initFilterCombos();
        initAddForm();
        loadPage(0);
    }

    @Override
    public void update(ChangeEventType event) {
        if (event == ChangeEventType.USER || event == ChangeEventType.CARD) {
            loadPage(currentPage);
        }
    }

    @FXML
    public void initialize() {
        // Configurarea coloanelor tabelului
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableColumnType.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new SimpleStringProperty(user instanceof Persoana ? "Persoană" : (user instanceof Rata ? "Rață" : "Necunoscut"));
        });

        tableColumnDetalii.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            if (user instanceof Persoana p) {
                return new SimpleStringProperty(p.getNume() + " " + p.getPrenume() + (p.getOcupatie() != null ? " (" + p.getOcupatie() + ")" : ""));
            } else if (user instanceof Rata r) {
                return new SimpleStringProperty(String.format("Tip: %s, R: %.1f, V: %.1f",
                        (r.getTip() != null ? r.getTip().name() : "N/A"), r.getRezistenta(), r.getViteza()));
            }
            return new SimpleStringProperty("");
        });

        tableColumnCardInfo.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            if (user instanceof Rata r) {
                Long cardId = r.getIdCard();
                return new SimpleStringProperty((cardId != null && cardId != -1L) ? "ID Card: " + cardId : "Fără Card");
            }
            return new SimpleStringProperty("N/A");
        });

        tableView.setItems(model);

        prevButton.setOnAction(event -> loadPage(currentPage - 1));
        nextButton.setOnAction(event -> loadPage(currentPage + 1));
    }

    private void initFilterCombos() {
        ObservableList<String> userTypes = FXCollections.observableArrayList(Arrays.asList("Toți", "PERSOANA", "RATA"));
        comboBoxFilterUserType.setItems(userTypes);
        comboBoxFilterUserType.setValue("Toți");

        comboBoxFilterUserType.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedUserType = newVal.equals("Toți") ? null : newVal;
            if (!"RATA".equals(selectedUserType)) {
                selectedRataTip = null;
                comboBoxFilterRataTip.setValue(null);
            }
            loadPage(0);
        });

        ObservableList<TipRata> tipuriRata = FXCollections.observableArrayList(TipRata.values());
        tipuriRata.add(0, null);
        comboBoxFilterRataTip.setItems(tipuriRata);

        comboBoxFilterRataTip.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedRataTip = newVal;
            if ("RATA".equals(selectedUserType)) {
                loadPage(currentPage);
            }
        });

        comboBoxFilterRataTip.disableProperty().bind(
                comboBoxFilterUserType.valueProperty().isNotEqualTo("RATA")
        );
    }

    private void initAddForm() {
        comboBoxAddType.setItems(FXCollections.observableArrayList("PERSOANA", "RATA"));
        comboBoxAddType.setValue(currentAddType);
        comboBoxTipRataAdd.setItems(FXCollections.observableArrayList(TipRata.values()));

        handleUserTypeChange();
    }

    private void loadPage(int pageIndex) {
        if (pageIndex < 0) return;
        Pageable pageable = new PageableImpl(pageIndex, pageSize);
        Page<? extends User> pageResult;

        try {
            boolean isOptimizedRateFilter = "RATA".equals(selectedUserType) && selectedRataTip != null;

            if (isOptimizedRateFilter) {
                pageResult = serviciu.getPaginaRateByTip(pageable, selectedRataTip);
            } else {
                pageResult = serviciu.getPaginaUseri(pageable, selectedUserType);
            }

            model.setAll((List<User>) pageResult.getContent());

            currentPage = pageResult.getPageNumber();
            int totalPages = pageResult.getTotalPages();

            if (pageIndex >= totalPages && totalPages > 0) {
                loadPage(totalPages - 1);
                return;
            }

            prevButton.setDisable(currentPage == 0 || totalPages == 0);
            nextButton.setDisable(totalPages == 0 || currentPage >= totalPages - 1);

            if (totalPages == 0) {
                pageLabel.setText("0 rezultate");
            } else {
                pageLabel.setText("Pagina " + (currentPage + 1) + " din " + totalPages);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Eroare Paginare", e.getMessage());
        }
    }

    @FXML
    private void handleUserTypeChange() {
        String type = comboBoxAddType.getValue();
        if (type == null) return;
        currentAddType = type;

        boolean isPersoana = "PERSOANA".equals(type);
        boolean isRata = "RATA".equals(type);

        toggleField(labelNume, textFieldNume, isPersoana);
        toggleField(labelPrenume, textFieldPrenume, isPersoana);
        toggleField(labelDataNasterii, datePickerDataNasterii, isPersoana);
        toggleField(labelOcupatie, textFieldOcupatie, isPersoana);

        toggleField(labelRezistenta, textFieldRezistenta, isRata);
        toggleField(labelViteza, textFieldViteza, isRata);
        toggleField(labelTipRataAdd, comboBoxTipRataAdd, isRata);
    }

    private void toggleField(Control label, Control field, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
        field.setVisible(visible);
        field.setManaged(visible);
    }


    @FXML
    private void handleAddUser() {
        try {
            if (textFieldId.getText().isEmpty()) throw new IllegalArgumentException("ID-ul este obligatoriu.");
            if (textFieldUsername.getText().isEmpty()) throw new IllegalArgumentException("Username-ul este obligatoriu.");

            Long id = Long.parseLong(textFieldId.getText());
            String username = textFieldUsername.getText();
            String email = textFieldEmail.getText();
            String password = passwordField.getText();
            String response;

            if ("PERSOANA".equals(currentAddType)) {
                String nume = textFieldNume.getText();
                String prenume = textFieldPrenume.getText();
                LocalDate date = datePickerDataNasterii.getValue();
                String dataNasterii = (date != null) ? date.toString() : null;
                String ocupatie = textFieldOcupatie.getText();

                response = serviciu.adaugaPersoana(id, username, email, password, nume, prenume, dataNasterii, ocupatie);

            } else if ("RATA".equals(currentAddType)) {
                if (textFieldRezistenta.getText().isEmpty() || textFieldViteza.getText().isEmpty())
                    throw new IllegalArgumentException("Rezistența și Viteza sunt obligatorii.");

                Double rezistenta = Double.parseDouble(textFieldRezistenta.getText());
                Double viteza = Double.parseDouble(textFieldViteza.getText());
                TipRata tipRata = comboBoxTipRataAdd.getValue();

                if (tipRata == null) throw new IllegalArgumentException("Selectați Tipul Raței.");

                response = serviciu.adaugaRata(id, username, email, password, rezistenta, viteza, tipRata.name());
            } else {
                throw new IllegalArgumentException("Tip invalid.");
            }

            showAlert(Alert.AlertType.INFORMATION, "Adaugare", response);
            clearAddFields();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Eroare Format", "Verificați ca ID-ul și valorile numerice să fie corecte.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Eroare", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = tableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Atenție", "Selectați un utilizator din tabel.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmare Ștergere");
        confirm.setHeaderText("Sigur doriți să ștergeți utilizatorul?");
        confirm.setContentText(selectedUser.getUsername() + " (ID: " + selectedUser.getId() + ")");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String response = serviciu.stergeUser(selectedUser.getId());
                showAlert(Alert.AlertType.INFORMATION, "Info", response);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Eroare", e.getMessage());
            }
        }
    }

    @FXML
    public void handleShowPass() {
        User u = tableView.getSelectionModel().getSelectedItem();
        if (u == null) {
            showAlert(Alert.AlertType.WARNING, "Selectie", "Selectați un user pentru a vedea parola.");
            return;
        }
        String realPass = serviciu.getDecryptedPassword(u.getId());

        TextInputDialog dialog = new TextInputDialog(realPass);
        dialog.setTitle("Parolă Utilizator");
        dialog.setHeaderText("Parola pentru " + u.getUsername());
        dialog.setContentText("Parola:");
        dialog.getEditor().setEditable(false);
        dialog.getDialogPane().getButtonTypes().remove(ButtonType.CANCEL);
        dialog.showAndWait();
    }

    @FXML
    private void handleOpenFriendsWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/ducksocialnetworkm/prietenii-view.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Prietenii în Rețea");
            stage.setScene(new Scene(fxmlLoader.load()));

            PrietenieController prietenieController = fxmlLoader.getController();
            prietenieController.setService(serviciu);

            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Eroare", "Nu s-a putut deschide fereastra: " + e.getMessage());
        }
    }

    @FXML
    public void handleOpenChat() {
        try {
            User u1 = showLogin("Login Utilizator 1 (Tu)");
            if (u1 == null) return;

            User u2 = showLogin("Login Utilizator 2 (Partener)");
            if (u2 == null) return;

            if (u1.getId().equals(u2.getId())) {
                showAlert(Alert.AlertType.ERROR, "Eroare", "Nu poți vorbi singur!");
                return;
            }

            openChatWindow(u1, u2); // Fereastra mea
            openChatWindow(u2, u1); // Fereastra partenerului

        } catch (Exception e) { e.printStackTrace(); }
    }

    private User showLogin(String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ducksocialnetworkm/login-view.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        stage.setTitle(title);
        LoginController ctrl = loader.getController();
        ctrl.setService(serviciu, stage, title);
        stage.showAndWait();
        return ctrl.getLoggedUser();
    }

    private void openChatWindow(User me, User other) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ducksocialnetworkm/chat-view.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Chat: " + me.getUsername() + " ➡ " + other.getUsername());

        ChatController ctrl = loader.getController();
        ctrl.initData(serviciu, me, other);

        try {
            String css = getClass().getResource("/org/example/ducksocialnetworkm/style.css").toExternalForm();
            stage.getScene().getStylesheets().add(css);
        } catch(Exception ex) {
            System.err.println("CSS-ul nu a fost găsit în User Controller!");
        }

        stage.show();
    }

    @FXML
    public void handleOpenUserAccount() {
        try {
            User user = showLogin("Autentificare pentru Contul Meu");

            if (user == null) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ducksocialnetworkm/user-account-view.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Contul Meu - " + user.getUsername());
            stage.setScene(new Scene(loader.load()));

            UserAccountController ctrl = loader.getController();
            ctrl.setService(serviciu, user);

            try {
                String css = getClass().getResource("/org/example/ducksocialnetworkm/style.css").toExternalForm();
                stage.getScene().getStylesheets().add(css);
            } catch(Exception ex) {
                System.err.println("CSS lipsa!");
            }

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Eroare", "Nu s-a putut deschide contul: " + e.getMessage());
        }
    }

    @FXML
    public void handleOpenCardsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ducksocialnetworkm/card-view.fxml"));
            AnchorPane root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestiune Carduri");
            stage.setScene(new Scene(root));

            CardController cardController = loader.getController();
            cardController.setService(serviciu);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearAddFields() {
        textFieldId.clear();
        textFieldUsername.clear();
        textFieldEmail.clear();
        passwordField.clear();
        textFieldNume.clear();
        textFieldPrenume.clear();
        datePickerDataNasterii.setValue(null);
        textFieldOcupatie.clear();
        textFieldRezistenta.clear();
        textFieldViteza.clear();
        comboBoxTipRataAdd.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}