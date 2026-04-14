package org.example.ducksocialnetworkm.depozit;

import org.example.ducksocialnetworkm.domeniu.event.Event;
import org.example.ducksocialnetworkm.domeniu.event.RaceEvent;
import org.example.ducksocialnetworkm.domeniu.observer.IObserver;
import org.example.ducksocialnetworkm.domeniu.user.*;
import org.example.ducksocialnetworkm.domeniu.validator.ValidationException;
import org.example.ducksocialnetworkm.factory.DuckFactory;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EventDepozitDB implements IEventDepozit {

    private final String url;
    private final String username;
    private final String password;

    public EventDepozitDB(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public Event cauta(Long... id) {
        if (id == null || id.length == 0) {
            throw new IllegalArgumentException("ID-ul nu poate fi null");
        }

        String sql = "SELECT * FROM evenimente WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id[0]);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEvent(rs, conn);
                } else {
                    throw new ValidationException("Nu exista un eveniment cu acest ID!");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautare eveniment: " + e.getMessage(), e);
        }
    }

    @Override
    public Collection<Event> getDepozit() {
        String sql = "SELECT * FROM evenimente";
        List<Event> lista = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRowToEvent(rs, conn));
            }
            return lista;

        } catch (SQLException e) {
            throw new RuntimeException("Eroare la getDepozit evenimente: " + e.getMessage(), e);
        }
    }


    private Event mapRowToEvent(ResultSet rs, Connection conn) throws SQLException {
        long eventId = rs.getLong("id");
        String nume = rs.getString("nume");
        String distText = rs.getString("distante");
        String status = rs.getString("status");
        String rez = rs.getString("rezultat_final");
        Long idCreator = rs.getLong("id_creator");

        List<Double> distante = parseDistante(distText);

        RaceEvent ev = new RaceEvent(eventId, nume, distante, idCreator);
        ev.setStatus(status != null ? status : "OPEN");
        ev.setRezultatFinal(rez != null ? rez : "");

        ev.setParticipanti(getParticipantsForEvent(eventId, conn));
        for (User u : getObserversForEvent(eventId, conn)) {
            ev.subscribe(u);
        }
        return ev;
    }


    @Override
    public void adauga(Event event) {
        String sql = "INSERT INTO evenimente(id, nume, distante, status, rezultat_final, id_creator) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            RaceEvent re = (RaceEvent) event;
            ps.setLong(1, event.getId());
            ps.setString(2, event.getNume());
            ps.setString(3, serializeDistante(re.getDistante()));
            ps.setString(4, re.getStatus());
            ps.setString(5, re.getRezultatFinal());
            ps.setLong(6, event.getIdCreator());

            ps.executeUpdate();

            insertParticipants(re, conn);
            insertObservers(event, conn);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modifica(Event mod) {
        String sql = "UPDATE evenimente SET nume=?, distante=?, status=?, rezultat_final=? WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            RaceEvent re = (RaceEvent) mod;

            ps.setString(1, mod.getNume());
            ps.setString(2, serializeDistante(re.getDistante()));
            ps.setString(3, re.getStatus());
            ps.setString(4, re.getRezultatFinal());
            ps.setLong(5, mod.getId());

            ps.executeUpdate();

            try (PreparedStatement del = conn.prepareStatement("DELETE FROM event_participants WHERE event_id=?")) {
                del.setLong(1, mod.getId());
                del.executeUpdate();
            }
            insertParticipants(re, conn);

            try (PreparedStatement del = conn.prepareStatement("DELETE FROM event_observers WHERE event_id=?")) {
                del.setLong(1, mod.getId());
                del.executeUpdate();
            }
            insertObservers(mod, conn);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void adaugaParticipant(Long eventId, Long userId) {
        String sql = "INSERT INTO event_participants(event_id, user_id) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (!"23505".equals(e.getSQLState())) {
                throw new RuntimeException("Eroare la adaugare participant: " + e.getMessage());
            }
        }
    }

    @Override
    public void sterge(Long... id) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM evenimente WHERE id=?")) {
                ps.setLong(1, id[0]);
                int rows = ps.executeUpdate();
                if (rows == 0)
                    throw new ValidationException("Nu exista eveniment cu acest ID!");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Încarcă lista de participanți din tabela `event_participants`.
     * Folosește DuckFactory și filtrează doar rațele compatibile cu {@link SwimmingDuck}.
     */
    private List<SwimmingDuck> getParticipantsForEvent(Long eventId, Connection conn) throws SQLException {
        List<SwimmingDuck> list = new ArrayList<>();
        String sql = """
            SELECT u.*
            FROM useri u
            INNER JOIN event_participants ep ON ep.user_id = u.id
            WHERE ep.event_id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = extractUserFromResultSet(rs);

                    if (user instanceof SwimmingDuck) {
                        list.add((SwimmingDuck) user);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Încarcă lista de observatori din tabela `event_observers`.
     * Poate conține atât Persoane cât și Rațe.
     */
    private List<User> getObserversForEvent(Long eventId, Connection conn) throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = """
            SELECT u.*
            FROM useri u
            INNER JOIN event_observers eo ON eo.user_id = u.id
            WHERE eo.event_id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = extractUserFromResultSet(rs);
                    if (user != null) {
                        list.add(user);
                    }
                }
            }
        }
        return list;
    }

    private void insertParticipants(RaceEvent event, Connection conn) throws SQLException {
        if (event.getParticipanti() == null || event.getParticipanti().isEmpty()) return;
        String sql = "INSERT INTO event_participants(event_id, user_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (SwimmingDuck duck : event.getParticipanti()) {
                ps.setLong(1, event.getId());
                ps.setLong(2, duck.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertObservers(Event event, Connection conn) throws SQLException {
        if (event.getObservers() == null || event.getObservers().isEmpty()) return;
        String sql = "INSERT INTO event_observers(event_id, user_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (IObserver ob : event.getObservers()) {
                if (ob instanceof User) {
                    User u = (User) ob;
                    ps.setLong(1, event.getId());
                    ps.setLong(2, u.getId());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        String tip = rs.getString("tip");
        Long id = rs.getLong("id");

        if ("PERSOANA".equalsIgnoreCase(tip)) {
            return new Persoana(
                    id,
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("nume"),
                    rs.getString("prenume"),
                    rs.getDate("data_nasterii") != null ? rs.getDate("data_nasterii").toString() : null,
                    rs.getString("ocupatie")
            );
        } else if ("RATA".equalsIgnoreCase(tip)) {
            Rata r = DuckFactory.creeazaRata(
                    id,
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getDouble("rezistenta"),
                    rs.getDouble("viteza"),
                    rs.getString("tip_rata")
            );

            Long idCard = rs.getObject("id_card") != null ? rs.getLong("id_card") : -1L;
            r.setIdCard(idCard);

            return r;
        }
        return null;
    }

    private List<Double> parseDistante(String text) {
        List<Double> list = new ArrayList<>();
        if (text == null || text.isEmpty()) return list;
        for (String s : text.split(",")) {
            try {
                if (!s.trim().isEmpty()) {
                    list.add(Double.parseDouble(s.trim()));
                }
            } catch (NumberFormatException e) {
                // ignorăm valori corupte
            }
        }
        return list;
    }

    private String serializeDistante(List<Double> distante) {
        if (distante == null || distante.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < distante.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(distante.get(i));
        }
        return sb.toString();
    }
}