package org.example.ducksocialnetworkm.depozit;

import org.example.ducksocialnetworkm.domeniu.mesaj.Mesaj;
import java.util.List;
import java.util.Map;

public interface MesajDepozit {
    /**
     * Salvează un mesaj în mediul de stocare.
     * @param message mesajul de salvat
     */
    void save(Mesaj message);

    /**
     * Returnează conversația dintre doi utilizatori, ordonată cronologic.
     * @param id1 ID-ul primului utilizator
     * @param id2 ID-ul celui de-al doilea utilizator
     * @return lista de mesaje
     */
    List<Mesaj> getConversation(Long id1, Long id2);

    public void markAsRead(Long myId, Long senderId);

    public Map<Long, Integer> getUnreadCounts(Long userId);
}