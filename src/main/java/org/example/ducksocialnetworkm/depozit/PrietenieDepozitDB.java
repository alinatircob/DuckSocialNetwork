package org.example.ducksocialnetworkm.depozit;

import org.example.ducksocialnetworkm.depozit.paging.PagingRelatieRepository;
import org.example.ducksocialnetworkm.domeniu.relatie.Prietenie;
import org.example.ducksocialnetworkm.domeniu.relatie.Relatie;
import org.example.ducksocialnetworkm.domeniu.validator.ValidationException;
import org.example.ducksocialnetworkm.depozit.paging.*;

import java.sql.*;
import java.util.*;

/**
 * Clasa {@code PrietenieDepozitDB} gestionează persistenta prieteniilor
 * în baza de date PostgreSQL, păstrând API-ul clasic al PrietenieDepozit.
 */
public class PrietenieDepozitDB implements PagingRelatieRepository {

    private String url;
    private String user;
    private String password;

    public PrietenieDepozitDB(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void adauga(Relatie prietenie) {
        String sql = "INSERT INTO prietenii (id1, id2) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, prietenie.getId1());
            ps.setLong(2, prietenie.getId2());

            ps.executeUpdate();


        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // codul PostgreSQL pentru UNIQUE violation
                throw new ValidationException("Exista deja prietenie intre cei 2 useri!");
            }
            throw new RuntimeException("Eroare la inserarea prieteniei: " + e.getMessage(), e);
        }
    }

    public void sterge(Long... id) {
        String sql = "DELETE FROM prietenii WHERE (id1=? AND id2=?) OR (id1=? AND id2=?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id[0]);
            stmt.setLong(2, id[1]);
            stmt.setLong(3, id[1]);
            stmt.setLong(4, id[0]);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new ValidationException("Nu exista prietenie intre cei 2 useri!");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la stergerea prieteniei din baza de date: " + e.getMessage(), e);
        }
    }

    public Relatie cauta(Long... id) {
        String sql = "SELECT id1, id2 FROM prietenii " +
                "WHERE (id1 = ? AND id2 = ?) OR (id1 = ? AND id2 = ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id[0]);
            ps.setLong(2, id[1]);
            ps.setLong(3, id[1]);
            ps.setLong(4, id[0]);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new ValidationException("Nu exista prietenie intre cei 2 useri!\n");
                }

                Long id1 = rs.getLong("id1");
                Long id2 = rs.getLong("id2");
                return new Prietenie(id1, id2);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea prieteniei: " + e.getMessage(), e);
        }
    }


    public Collection<Relatie> getDepozit() {
        List<Relatie> rezultat = new ArrayList<>();

        String sql = "SELECT id1, id2 FROM prietenii";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Long id1 = rs.getLong("id1");
                Long id2 = rs.getLong("id2");
                rezultat.add(new Prietenie(id1, id2));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Eroare la obtinerea prieteniilor: " + e.getMessage(), e);
        }

        return rezultat;
    }


    public void stergePUser(Long id) {
        String sql = "DELETE FROM prietenii WHERE id1=? OR id2=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la stergerea prieteniilor userului din baza: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<Relatie> findAll(Pageable pageable) {
        List<Relatie> lista = new ArrayList<>();
        long offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();

        int totalItems = countAll();
        if (totalItems == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        String sql = "SELECT id1, id2 FROM prietenii LIMIT ? OFFSET ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pageSize);
            ps.setLong(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long id1 = rs.getLong("id1");
                    Long id2 = rs.getLong("id2");
                    lista.add(new Prietenie(id1, id2));
                }
            }
            return new PageImpl<>(lista, pageable, totalItems);

        } catch (SQLException e) {
            throw new RuntimeException("Eroare la paginarea prieteniilor: " + e.getMessage(), e);
        }
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM prietenii";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la numărarea prieteniilor: " + e.getMessage(), e);
        }
        return 0;
    }

    public void modifica(Relatie mod){}
}
