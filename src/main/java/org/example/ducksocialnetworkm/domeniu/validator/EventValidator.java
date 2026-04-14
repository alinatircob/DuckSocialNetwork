package org.example.ducksocialnetworkm.domeniu.validator;

import org.example.ducksocialnetworkm.domeniu.event.*;

public class EventValidator implements ValidatorDomeniu<RaceEvent> {

    public void valideaza(RaceEvent event) throws ValidationException {

        String erori = "";

        if (event.getId() == null || event.getId() <= 0)
            erori += "ID-ul trebuie sa fie pozitiv!\n";

        if (event.getNume() == null || event.getNume().isBlank())
            erori += "Nume invalid!\n";
        if (!event.getNume().chars().allMatch(Character::isLetter))
            erori += "Numele trebuie sa contina doar litere!\n";

        for(Double d : event.getDistante()) {
            if (d < 0) {
                erori += "Distante invalide!\n";
                break;
            }
        }

//        if(event.getDistante().size() > event.getParticipanti().size()) {
//            erori += "Numar invalid de distante!\n";
//        }

        if (!erori.isEmpty())
            throw new ValidationException(erori);

    }
}
