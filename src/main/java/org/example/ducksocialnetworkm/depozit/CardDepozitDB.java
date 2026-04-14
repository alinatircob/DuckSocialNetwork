package org.example.ducksocialnetworkm.depozit;

import org.example.ducksocialnetworkm.domeniu.card.Card;
import org.example.ducksocialnetworkm.domeniu.card.FlyingCard;
import org.example.ducksocialnetworkm.domeniu.card.SwimmingCard;
import org.example.ducksocialnetworkm.domeniu.user.Rata;
import org.example.ducksocialnetworkm.domeniu.validator.ValidationException;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CardDepozitDB implements Depozit<Card<? extends Rata>> {

    private final String url;
    private final String username;
    private final String password;


    public CardDepozitDB(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }


    public void adauga(Card<? extends Rata> card) {
        String tipDomeniu;
        switch (card.getTip()) {
            case "Zburator":
                tipDomeniu = "FLYINGCARD";
                break;
            case "Inotator":
                tipDomeniu = "SWIMMINGCARD";
                break;
            default:
                tipDomeniu = card.getTip(); // fallback
        }

        String sql = "INSERT INTO carduri(id, nume, tip) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, card.getId());
            ps.setString(2, card.getNume());
            PGobject enumObj = new PGobject();
            enumObj.setType("tip_card");
            enumObj.setValue(tipDomeniu);

            ps.setObject(3, enumObj);
            ps.executeUpdate();


        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) { // cod pentru UNIQUE violation
                throw new ValidationException("Exista deja un card cu acest ID!");
            }
            throw new RuntimeException("Eroare la inserarea cardului: " + e.getMessage(), e);
        }
    }


    public void sterge(Long... id) {

        String sql = "DELETE FROM carduri WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id[0]);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new ValidationException("Nu exista un card cu acest ID!");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la stergerea cardului: " + e.getMessage(), e);
        }
    }

    public Card<? extends Rata> cauta(Long... id) {
        String sql = "SELECT * FROM carduri WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id[0]);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long cardId = rs.getLong("id");
                    String nume = rs.getString("nume");
                    String tip = rs.getString("tip");

                    if ("FLYINGCARD".equalsIgnoreCase(tip)) {
                        return new FlyingCard(cardId, nume, "Zburator");
                    } else if ("SWIMMINGCARD".equalsIgnoreCase(tip)) {
                        return new SwimmingCard(cardId, nume, "Inotator");
                    } else {
                        throw new RuntimeException("Tip card necunoscut: " + tip);
                    }
                } else {
                    throw new ValidationException("Nu exista un card cu acest ID!\n");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea cardului: " + e.getMessage(), e);
        }
    }

    /**
     * Returnează toate cardurile direct din baza de date.
     */
    public Collection<Card<? extends Rata>> getDepozit() {
        Map<Long, Card<? extends Rata>> allCards = new HashMap<>();
        String sql = "SELECT * FROM carduri";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Long id = rs.getLong("id");
                String nume = rs.getString("nume");
                String tip = rs.getString("tip");

                if ("FLYINGCARD".equalsIgnoreCase(tip)) {
                    allCards.put(id, new FlyingCard(id, nume, "Zburator"));
                } else if ("SWIMMINGCARD".equalsIgnoreCase(tip)) {
                    allCards.put(id, new SwimmingCard(id, nume, "Inotator"));
                } else {
                    throw new RuntimeException("Tip card necunoscut: " + tip);
                }
            }
            return allCards.values();

        } catch (SQLException e) {
            throw new RuntimeException("Eroare la getDepozit: " + e.getMessage(), e);
        }
    }

    public void modifica(Card<? extends Rata> mod) {

    }
}
