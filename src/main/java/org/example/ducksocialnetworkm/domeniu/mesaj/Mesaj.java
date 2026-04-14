package org.example.ducksocialnetworkm.domeniu.mesaj;
import org.example.ducksocialnetworkm.domeniu.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Mesaj {

    private Long id;
    private User from;
    private List<User> to;
    private String text;
    private LocalDateTime data;
    private String status;

    public Mesaj(User from, List<User> to, String text) {
        this.from = from;
        this.to = to;
        this.text = text;
        this.data = LocalDateTime.now();
        this.status = "UNREAD";
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public List<User> getTo() {
        return to;
    }

    public void setTo(List<User> to) {
        this.to = to;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }


    @Override
    public String toString() {
        return "Mesaj{" +
                "id=" + id +
                ", from=" + (from != null ? from.getUsername() : "null") +
                ", to=" + to +
                ", text='" + text + '\'' +
                ", data=" + data +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mesaj)) return false;
        Mesaj message = (Mesaj) o;
        return Objects.equals(getId(), message.getId()) &&
                Objects.equals(getFrom(), message.getFrom()) &&
                Objects.equals(getTo(), message.getTo()) &&
                Objects.equals(getText(), message.getText()) &&
                Objects.equals(getData(), message.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFrom(), getTo(), getText(), getData());
    }
}
