package org.example.ducksocialnetworkm.depozit;

import org.example.ducksocialnetworkm.domeniu.mesaj.Mesaj;
import org.example.ducksocialnetworkm.domeniu.mesaj.ReplyMesaj;
import org.example.ducksocialnetworkm.domeniu.user.Persoana;
import org.example.ducksocialnetworkm.domeniu.user.Rata;
import org.example.ducksocialnetworkm.domeniu.user.User;
import org.example.ducksocialnetworkm.factory.DuckFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MesajDepozitDB implements MesajDepozit {

    private final String url;
    private final String username;
    private final String password;

    public MesajDepozitDB(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public void save(Mesaj message) {
        if (message.getId() == null || message.getId() == 0) {
            message.setId(System.currentTimeMillis());
        }

        if (message.getStatus() == null) {
            message.setStatus("UNREAD");
        }

        String sql = "INSERT INTO mesaje (id, from_user_id, to_user_id, text, data, reply_to_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, message.getId());
            ps.setLong(2, message.getFrom().getId());
            ps.setLong(3, message.getTo().get(0).getId());
            ps.setString(4, message.getText());
            ps.setTimestamp(5, Timestamp.valueOf(message.getData()));

            if (message instanceof ReplyMesaj && ((ReplyMesaj) message).getOriginalMessage() != null) {
                ps.setLong(6, ((ReplyMesaj) message).getOriginalMessage().getId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }

            ps.setString(7, message.getStatus());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la salvarea mesajului: " + e.getMessage(), e);
        }
    }

    /**
     * Metodă nouă care extrage un utilizator direct din rândul curent al ResultSet-ului
     * generat de JOIN, folosind un prefix specific pentru alias-urile coloanelor.
     */
    private User extractUserFromJoin(ResultSet rs, String prefix) throws SQLException {
        Long id = rs.getLong(prefix + "id");
        if (id == 0) return null;

        String tip = rs.getString(prefix + "tip");
        String user = rs.getString(prefix + "username");
        String email = rs.getString(prefix + "email");
        String pass = rs.getString(prefix + "password");

        if ("PERSOANA".equals(tip)) {
            return new Persoana(
                    id, user, email, pass,
                    rs.getString(prefix + "nume"),
                    rs.getString(prefix + "prenume"),
                    rs.getDate(prefix + "data_nasterii") != null ? rs.getDate(prefix + "data_nasterii").toString() : null,
                    rs.getString(prefix + "ocupatie")
            );
        } else if ("RATA".equals(tip)) {
            Rata r = DuckFactory.creeazaRata(
                    id, user, email, pass,
                    rs.getDouble(prefix + "rezistenta"),
                    rs.getDouble(prefix + "viteza"),
                    rs.getString(prefix + "tip_rata")
            );
            Long idCard = rs.getObject(prefix + "id_card") != null ? rs.getLong(prefix + "id_card") : -1L;
            r.setIdCard(idCard);
            return r;
        }
        return null;
    }

    private Mesaj findOne(Long id) {
        if (id == null) return null;

        String sql = "SELECT m.*, " +
                "uf.id as f_id, uf.tip as f_tip, uf.username as f_username, uf.email as f_email, uf.password as f_password, uf.nume as f_nume, uf.prenume as f_prenume, uf.data_nasterii as f_data_nasterii, uf.ocupatie as f_ocupatie, uf.rezistenta as f_rezistenta, uf.viteza as f_viteza, uf.tip_rata as f_tip_rata, uf.id_card as f_id_card, " +
                "ut.id as t_id, ut.tip as t_tip, ut.username as t_username, ut.email as t_email, ut.password as t_password, ut.nume as t_nume, ut.prenume as t_prenume, ut.data_nasterii as t_data_nasterii, ut.ocupatie as t_ocupatie, ut.rezistenta as t_rezistenta, ut.viteza as t_viteza, ut.tip_rata as t_tip_rata, ut.id_card as t_id_card " +
                "FROM mesaje m " +
                "JOIN useri uf ON m.from_user_id = uf.id " +
                "JOIN useri ut ON m.to_user_id = ut.id " +
                "WHERE m.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractMessageFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Eroare la încărcarea mesajului original cu ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    private Mesaj extractMessageFromResultSet(ResultSet rs) throws SQLException {
        Long msgId = rs.getLong("id");
        String text = rs.getString("text");
        LocalDateTime date = rs.getTimestamp("data").toLocalDateTime();
        Long replyToId = rs.getObject("reply_to_id") != null ? rs.getLong("reply_to_id") : null;

        String status = rs.getString("status");
        if (status == null) status = "UNREAD";
        User from = extractUserFromJoin(rs, "f_");
        User to = extractUserFromJoin(rs, "t_");

        List<User> toList = new ArrayList<>();
        if (to != null) toList.add(to);

        Mesaj resultMsg;
        if (replyToId != null) {
            Mesaj original = findOne(replyToId);
            ReplyMesaj replyMsg = new ReplyMesaj(from, toList, text, original);
            replyMsg.setId(msgId);
            replyMsg.setData(date);
            replyMsg.setStatus(status);
            resultMsg = replyMsg;
        } else {
            Mesaj msg = new Mesaj(from, toList, text);
            msg.setId(msgId);
            msg.setData(date);
            msg.setStatus(status);
            resultMsg = msg;
        }
        return resultMsg;
    }

    @Override
    public List<Mesaj> getConversation(Long id1, Long id2) {
        List<Mesaj> messages = new ArrayList<>();

        String sql = "SELECT m.*, " +
                "uf.id as f_id, uf.tip as f_tip, uf.username as f_username, uf.email as f_email, uf.password as f_password, uf.nume as f_nume, uf.prenume as f_prenume, uf.data_nasterii as f_data_nasterii, uf.ocupatie as f_ocupatie, uf.rezistenta as f_rezistenta, uf.viteza as f_viteza, uf.tip_rata as f_tip_rata, uf.id_card as f_id_card, " +
                "ut.id as t_id, ut.tip as t_tip, ut.username as t_username, ut.email as t_email, ut.password as t_password, ut.nume as t_nume, ut.prenume as t_prenume, ut.data_nasterii as t_data_nasterii, ut.ocupatie as t_ocupatie, ut.rezistenta as t_rezistenta, ut.viteza as t_viteza, ut.tip_rata as t_tip_rata, ut.id_card as t_id_card " +
                "FROM mesaje m " +
                "JOIN useri uf ON m.from_user_id = uf.id " +
                "JOIN useri ut ON m.to_user_id = ut.id " +
                "WHERE (m.from_user_id = ? AND m.to_user_id = ?) OR (m.from_user_id = ? AND m.to_user_id = ?) " +
                "ORDER BY m.data ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id1);
            ps.setLong(2, id2);
            ps.setLong(3, id2);
            ps.setLong(4, id1);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(extractMessageFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la încărcarea conversației: " + e.getMessage(), e);
        }
        return messages;
    }


    /**
     * Returnează un Map unde cheia este ID-ul expeditorului și valoarea este numărul de mesaje necitite
     * trimise de acel expeditor către userId.
     */
    public Map<Long, Integer> getUnreadCounts(Long userId) {
        Map<Long, Integer> counts = new HashMap<>();
        String sql = "SELECT from_user_id, COUNT(*) as nr FROM mesaje WHERE to_user_id = ? AND status = 'UNREAD' GROUP BY from_user_id";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getLong("from_user_id"), rs.getInt("nr"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    /**
     * Marchează toate mesajele de la senderId către myId ca fiind CITITE ('READ').
     */
    public void markAsRead(Long myId, Long senderId) {
        String sql = "UPDATE mesaje SET status = 'READ' WHERE to_user_id = ? AND from_user_id = ? AND status = 'UNREAD'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, myId);
            ps.setLong(2, senderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}