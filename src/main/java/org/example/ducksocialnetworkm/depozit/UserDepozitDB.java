package org.example.ducksocialnetworkm.depozit;

import org.example.ducksocialnetworkm.depozit.paging.*;
import org.example.ducksocialnetworkm.domeniu.user.Persoana;
import org.example.ducksocialnetworkm.domeniu.user.Rata;
import org.example.ducksocialnetworkm.domeniu.user.TipRata;
import org.example.ducksocialnetworkm.domeniu.user.User;
import org.example.ducksocialnetworkm.domeniu.validator.ValidationException;
import org.example.ducksocialnetworkm.factory.DuckFactory;

import java.sql.*;
import java.util.*;

/**
 * Clasa {@code UserDepozitDB} gestionează persistenta utilizatorilor
 * într-o bază de date PostgreSQL. Aceasta permite adăugarea, ștergerea,
 * căutarea și listarea utilizatorilor, similar cu {@code UserDepozit}.
 */
public class UserDepozitDB implements RataDepozit{

    private final String url;
    private final String username;
    private final String password;

    /**
     * Creează un nou {@code UserDepozitDB} și încarcă datele din baza de date.
     */
    public UserDepozitDB(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Deschide o conexiune JDBC la baza de date.
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Adaugă un utilizator nou în baza de date.
     * @param user utilizatorul de adăugat
     * @throws ValidationException dacă ID-ul există deja
     */
    public void adauga(User user) {

        String sql = """
            INSERT INTO useri (id, tip, username, email, password, nume, prenume, data_nasterii,
                               ocupatie, rezistenta, viteza, tip_rata, id_card)
            VALUES (?, CAST(? AS tip_user), ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS tip_rata), ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, user.getId());
            String encryptedPassword = org.example.ducksocialnetworkm.utils.password.Password.encrypt(user.getPassword());
            if (user instanceof Persoana p) {
                ps.setString(2, "PERSOANA");
                ps.setString(3, p.getUsername());
                ps.setString(4, p.getEmail());
                ps.setString(5, encryptedPassword);
                ps.setString(6, p.getNume());
                ps.setString(7, p.getPrenume());
                ps.setDate(8, p.getDataNasterii() != null ?
                        java.sql.Date.valueOf(p.getDataNasterii()) : null);
                ps.setString(9, p.getOcupatie());
                ps.setNull(10, Types.DOUBLE);
                ps.setNull(11, Types.DOUBLE);
                ps.setNull(12, Types.VARCHAR);
                ps.setNull(13, Types.BIGINT);
            } else if (user instanceof Rata r) {
                ps.setString(2, "RATA");
                ps.setString(3, r.getUsername());
                ps.setString(4, r.getEmail());
                ps.setString(5, encryptedPassword);
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.DATE);
                ps.setNull(9, Types.VARCHAR);
                ps.setDouble(10, r.getRezistenta());
                ps.setDouble(11, r.getViteza());
                ps.setString(12, r.getTip().name());
                if (r.getIdCard() != -1L)
                    ps.setLong(13, r.getIdCard());
                else
                    ps.setNull(13, Types.BIGINT);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // duplicate key violation în PostgreSQL
                throw new ValidationException("Exista deja un user cu acest ID!");
            }
            throw new ValidationException("Eroare la inserare: " + e.getMessage());
        }
    }

    /**
     * Șterge un utilizator din baza de date.
     * @param id ID-ul utilizatorului
     * @throws ValidationException dacă nu există utilizatorul
     */
    public void sterge(Long... id) {

        String sql = "DELETE FROM useri WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id[0]);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new ValidationException("Nu exista un user cu acest ID!\n");
            }
        } catch (SQLException e) {
            throw new ValidationException("Eroare la stergere: " + e.getMessage());
        }
    }

    /**
     * Caută un utilizator după ID.
     * @param id ID-ul căutat
     * @return utilizatorul găsit
     */
    public User cauta(Long... id) {
        if (id == null || id.length == 0) {
            throw new IllegalArgumentException("ID-ul nu poate fi null");
        }

        String sql = "SELECT * FROM useri WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id[0]);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tip = rs.getString("tip");
                    Long userId = rs.getLong("id");
                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    String password = rs.getString("password");

                    if ("PERSOANA".equals(tip)) {
                        return new Persoana(
                                userId,
                                username,
                                email,
                                password,
                                rs.getString("nume"),
                                rs.getString("prenume"),
                                rs.getDate("data_nasterii") != null ? rs.getDate("data_nasterii").toString() : null,
                                rs.getString("ocupatie")
                        );
                    } else if ("RATA".equals(tip)) {
                        Rata r = DuckFactory.creeazaRata(
                                userId,
                                username,
                                email,
                                password,
                                rs.getDouble("rezistenta"),
                                rs.getDouble("viteza"),
                                rs.getString("tip_rata")
                        );
                        Long idCard = rs.getObject("id_card") != null ? rs.getLong("id_card") : -1L;
                        r.setIdCard(idCard);
                        return r;
                    } else {
                        throw new RuntimeException("Tip necunoscut în baza de date: " + tip);
                    }
                } else {
                    throw new ValidationException("Nu exista un user cu acest ID!\n");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautare: " + e.getMessage(), e);
        }
    }

    public Collection<User> getDepozit() {
        String sql = "SELECT * FROM useri";
        Set<User> lista = new HashSet<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String tip = rs.getString("tip");
                Long id = rs.getLong("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String password = rs.getString("password");

                if ("PERSOANA".equals(tip)) {
                    Persoana p = new Persoana(
                            id,
                            username,
                            email,
                            password,
                            rs.getString("nume"),
                            rs.getString("prenume"),
                            rs.getDate("data_nasterii") != null ? rs.getDate("data_nasterii").toString() : null,
                            rs.getString("ocupatie")
                    );
                    lista.add(p);

                } else if ("RATA".equals(tip)) {
                    Rata r = DuckFactory.creeazaRata(
                            id,
                            username,
                            email,
                            password,
                            rs.getDouble("rezistenta"),
                            rs.getDouble("viteza"),
                            rs.getString("tip_rata")
                    );
                    Long idCard = rs.getObject("id_card") != null ? rs.getLong("id_card") : -1L;
                    r.setIdCard(idCard);
                    lista.add(r);
                }
            }

            return lista;

        } catch (SQLException e) {
            throw new RuntimeException("Eroare la getDepozit: " + e.getMessage(), e);
        }
    }


    public void modifica(User mod) {
        User user = mod;

        String sql = """
    UPDATE useri
    SET tip = ?::tip_user, username = ?, email = ?, password = ?, 
        nume = ?, prenume = ?, data_nasterii = ?, ocupatie = ?, 
        rezistenta = ?, viteza = ?, tip_rata = ?::tip_rata, id_card = ?
    WHERE id = ?
    """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (user instanceof Persoana) {
                ps.setObject(1, "PERSOANA", Types.OTHER);
            } else if (user instanceof Rata) {
                ps.setObject(1, "RATA", Types.OTHER);
            }

            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());

            if (user instanceof Persoana p) {
                ps.setString(5, p.getNume());
                ps.setString(6, p.getPrenume());
                ps.setDate(7, p.getDataNasterii() != null ?
                        java.sql.Date.valueOf(p.getDataNasterii()) : null);
                ps.setString(8, p.getOcupatie());
                ps.setNull(9, Types.DOUBLE);
                ps.setNull(10, Types.DOUBLE);
                ps.setNull(11, Types.OTHER);
                ps.setNull(12, Types.BIGINT);
            } else if (user instanceof Rata r) {
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.DATE);
                ps.setNull(8, Types.VARCHAR);
                ps.setDouble(9, r.getRezistenta());
                ps.setDouble(10, r.getViteza());

                ps.setObject(11, r.getTip().name(), Types.OTHER);

                if (r.getIdCard() != null && r.getIdCard() != -1L) {
                    ps.setLong(12, r.getIdCard());
                } else {
                    ps.setNull(12, Types.BIGINT);
                }
            }

            ps.setLong(13, user.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la modificarea userului în baza de date: " + e.getMessage(), e);
        }
    }

    public List<Rata> getRate(TipRata tip) {
        List<Rata> lista = new ArrayList<>();

        String sql;
        boolean filtrare = tip != null;

        if (filtrare) {
            sql = "SELECT * FROM useri WHERE tip = 'RATA' AND tip_rata = ?::tip_rata";
        } else {
            sql = "SELECT * FROM useri WHERE tip = 'RATA'";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (filtrare) {
                ps.setString(1, tip.name());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    String password = rs.getString("password");

                    String tipRataStr = rs.getString("tip_rata");
                    TipRata tipRataEnum = TipRata.valueOf(tipRataStr.toUpperCase());

                    Rata r = DuckFactory.creeazaRata(
                            id,
                            username,
                            email,
                            password,
                            rs.getDouble("rezistenta"),
                            rs.getDouble("viteza"),
                            tipRataEnum.name()  // trimitem numele enum-ului
                    );

                    Long idCard = rs.getObject("id_card") != null ? rs.getLong("id_card") : -1L;
                    r.setIdCard(idCard);

                    lista.add(r);
                }
            }

            return lista;

        } catch (SQLException e) {
            throw new RuntimeException("Eroare la getRate: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return findAllFiltered(pageable, null);
    }

    @Override
    public Page<User> findAllFiltered(Pageable pageable, String tipUser) {
        List<User> lista = new ArrayList<>();
        long offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();

        int totalItems = countFiltered(tipUser);
        if (totalItems == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        String sql = "SELECT * FROM useri ";
        boolean filtrareTip = tipUser != null && (tipUser.equals("PERSOANA") || tipUser.equals("RATA"));

        if (filtrareTip) {
            sql += "WHERE tip = ?::tip_user ";
        }

        sql += "LIMIT ? OFFSET ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (filtrareTip) {
                ps.setString(paramIndex++, tipUser);
            }

            ps.setInt(paramIndex++, pageSize);
            ps.setLong(paramIndex, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tip = rs.getString("tip");
                    Long id = rs.getLong("id");
                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    String password = rs.getString("password");

                    if ("PERSOANA".equals(tip)) {
                        Persoana p = new Persoana(
                                id, username, email, password,
                                rs.getString("nume"), rs.getString("prenume"),
                                rs.getDate("data_nasterii") != null ? rs.getDate("data_nasterii").toString() : null,
                                rs.getString("ocupatie")
                        );
                        lista.add(p);
                    } else if ("RATA".equals(tip)) {
                        Rata r = DuckFactory.creeazaRata(
                                id, username, email, password,
                                rs.getDouble("rezistenta"), rs.getDouble("viteza"),
                                rs.getString("tip_rata")
                        );
                        Long idCard = rs.getObject("id_card") != null ? rs.getLong("id_card") : -1L;
                        r.setIdCard(idCard);
                        lista.add(r);
                    }
                }
            }

            return new PageImpl<>(lista, pageable, totalItems);

        } catch (SQLException e) {
            throw new RuntimeException("Eroare la paginare în baza de date: " + e.getMessage(), e);
        }
    }

    @Override
    public int countFiltered(String tipUser) {
        String sql = "SELECT COUNT(*) FROM useri";
        boolean filtrareTip = tipUser != null && (tipUser.equals("PERSOANA") || tipUser.equals("RATA"));

        if (filtrareTip) {
            sql += " WHERE tip = ?::tip_user";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (filtrareTip) {
                ps.setString(1, tipUser);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la numărarea utilizatorilor: " + e.getMessage(), e);
        }
        return 0;
    }


    public Page<Rata> findAllRateByTip(Pageable pageable, TipRata tipRata) {
        List<Rata> lista = new ArrayList<>();
        long offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();

        int totalItems = countRateByTip(tipRata);
        if (totalItems == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        String sql = """
        SELECT * FROM useri 
        WHERE tip = 'RATA'::tip_user AND tip_rata = ?::tip_rata
        LIMIT ? OFFSET ?
    """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tipRata.name());
            ps.setInt(2, pageSize);
            ps.setLong(3, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Rata r = DuckFactory.creeazaRata(
                            rs.getLong("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getDouble("rezistenta"),
                            rs.getDouble("viteza"),
                            rs.getString("tip_rata")
                    );
                    Long idCard = rs.getObject("id_card") != null ? rs.getLong("id_card") : -1L;
                    r.setIdCard(idCard);
                    lista.add(r);
                }
            }
            return new PageImpl<>(lista, pageable, totalItems);

        } catch (SQLException e) {
            throw new RuntimeException("Eroare la paginarea ratelor după tip: " + e.getMessage(), e);
        }
    }


    /**
     * Returnează numărul total de rațe (Useri de tip RATA) filtrate după tipul lor specific.
     * * @param tipRata Tipul specific de rată (FLYING, SWIMMING, etc.).
     * @return Numărul total de rațe de tipul specificat.
     */
    public int countRateByTip(TipRata tipRata) {
        String sql = """
        SELECT COUNT(*) FROM useri 
        WHERE tip = 'RATA'::tip_user 
        AND tip_rata = ?::tip_rata
    """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Setează parametrul pentru tip_rata
            ps.setString(1, tipRata.name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la numărarea ratelor după tip: " + e.getMessage(), e);
        }
        return 0;
    }


}
