package org.example.ducksocialnetworkm.depozit;

import org.example.ducksocialnetworkm.domeniu.event.Event;

public interface IEventDepozit extends Depozit<Event>{

    public void adaugaParticipant(Long eventId, Long userId);

}
