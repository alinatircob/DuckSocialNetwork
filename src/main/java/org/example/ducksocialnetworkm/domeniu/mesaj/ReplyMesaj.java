package org.example.ducksocialnetworkm.domeniu.mesaj;

import org.example.ducksocialnetworkm.domeniu.user.User;

import java.util.List;

public class ReplyMesaj extends Mesaj {

    private Mesaj originalMessage;

    public ReplyMesaj(User from, List<User> to, String text, Mesaj originalMessage) {
        super(from, to, text);
        this.originalMessage = originalMessage;
    }

    public Mesaj getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(Mesaj originalMessage) {
        this.originalMessage = originalMessage;
    }

    @Override
    public String toString() {
        return "ReplyMessage{" +
                "id=" + getId() +
                ", from=" + (getFrom() != null ? getFrom().getUsername() : "null") +
                ", text='" + getText() + '\'' +
                ", data=" + getData() +
                ", replyTo=" + (originalMessage != null ? originalMessage.getId() : "null") +
                '}';
    }
}
