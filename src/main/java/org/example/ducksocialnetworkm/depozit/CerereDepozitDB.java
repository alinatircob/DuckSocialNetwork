package org.example.ducksocialnetworkm.depozit;

import org.example.ducksocialnetworkm.domeniu.relatie.CererePrietenie;
import org.example.ducksocialnetworkm.domeniu.validator.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CerereDepozitDB implements CerereDepozit {

    private final String url;
    private final String username;
    private final String password;

    public CerereDepozitDB(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public void adauga(CererePrietenie cerere) {
        String sql = "INSERT INTO cereri_prietenie (id, id_expeditor, id_destinatar, status, data) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cerere.getId());
            ps.setLong(2, cerere.getIdExpeditor());
            ps.setLong(3, cerere.getIdDestinatar());
            ps.setString(4, cerere.getStatus());
            ps.setTimestamp(5, Timestamp.valueOf(cerere.getData()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la adăugarea cererii: " + e.getMessage());
        }
    }

    public boolean existaCerereActiva(Long idExp, Long idDest) {
        String sql = "SELECT count(*) FROM cereri_prietenie WHERE " +
                "((id_expeditor = ? AND id_destinatar = ?) OR (id_expeditor = ? AND id_destinatar = ?)) " +
                "AND status IN ('PENDING', 'APPROVED')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExp);
            ps.setLong(2, idDest);
            ps.setLong(3, idDest);
            ps.setLong(4, idExp);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public CererePrietenie findPendingBetween(Long idExp, Long idDest) {
        String sql = "SELECT * FROM cereri_prietenie WHERE id_expeditor = ? AND id_destinatar = ? AND status = 'PENDING'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExp);
            ps.setLong(2, idDest);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return extractFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void modifica(CererePrietenie cerere) {
        String sql = "UPDATE cereri_prietenie SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cerere.getStatus());
            ps.setLong(2, cerere.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CererePrietenie> findPendingRequests(Long idDestinatar) {
        List<CererePrietenie> list = new ArrayList<>();
        String sql = "SELECT * FROM cereri_prietenie WHERE id_destinatar = ? AND status = 'PENDING'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idDestinatar);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extractFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private CererePrietenie extractFromResultSet(ResultSet rs) throws SQLException {
        return new CererePrietenie(
                rs.getLong("id"),
                rs.getLong("id_expeditor"),
                rs.getLong("id_destinatar"),
                rs.getString("status"),
                rs.getTimestamp("data").toLocalDateTime()
        );
    }

    @Override
    public void sterge(Long... id) {
        if (id.length < 2) throw new IllegalArgumentException("Sunt necesare 2 ID-uri pentru a șterge o cerere!");

        String sql = "DELETE FROM cereri_prietenie WHERE id_expeditor = ? AND id_destinatar = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id[0]);
            ps.setLong(2, id[1]);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new ValidationException("Cererea nu a fost găsită pentru a fi ștearsă!");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la ștergerea cererii: " + e.getMessage());
        }
    }


    @Override
    public CererePrietenie cauta(Long... id) {
        if (id.length < 2) throw new IllegalArgumentException("Sunt necesare 2 ID-uri pentru a căuta o cerere!");

        String sql = "SELECT * FROM cereri_prietenie WHERE id_expeditor = ? AND id_destinatar = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id[0]);
            ps.setLong(2, id[1]);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la căutarea cererii: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Collection<CererePrietenie> getDepozit() {
        List<CererePrietenie> lista = new ArrayList<>();
        String sql = "SELECT * FROM cereri_prietenie";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(extractFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la citirea cererilor: " + e.getMessage());
        }
        return lista;
    }
}