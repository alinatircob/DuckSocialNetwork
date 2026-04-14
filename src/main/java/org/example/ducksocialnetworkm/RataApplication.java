package org.example.ducksocialnetworkm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.ducksocialnetworkm.controller.UserController;
import org.example.ducksocialnetworkm.depozit.*;
import org.example.ducksocialnetworkm.serviciu.Serviciu;

import java.io.IOException;

public class RataApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        String url = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:postgresql://localhost:5432/useri";
        String username = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "postgres";
        String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "postgres";

        EventDepozitDB eventDepozit = new EventDepozitDB(url, username, password);
        CardDepozitDB cardDepozit = new CardDepozitDB(url, username, password);
        PrietenieDepozitDB prietenieDepozit = new PrietenieDepozitDB(url, username, password);
        UserDepozitDB userDepozit = new UserDepozitDB(url, username, password);
        MesajDepozitDB messageDepozit = new MesajDepozitDB(url, username, password);
        CerereDepozitDB cerereDepozit = new CerereDepozitDB(url, username, password);
        Serviciu serviciu = new Serviciu(userDepozit, prietenieDepozit, cardDepozit, eventDepozit);
        serviciu.setMessageDepozit(messageDepozit);
        serviciu.setCerereDepozit(cerereDepozit);


        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/ducksocialnetworkm/user-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Users");
        stage.setScene(scene);

        UserController userController = fxmlLoader.getController();
        userController.setService(serviciu);

        stage.show();
    }
}
