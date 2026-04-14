package org.example.ducksocialnetworkm.domeniu.card;

import org.example.ducksocialnetworkm.domeniu.user.FlyingDuck;

/**
 * Clasa {@code FlyingCard} reprezintă un card dedicat rațelor care zboară.
 * Extinde clasa abstractă {@link Card}.
 */
public class FlyingCard extends Card<FlyingDuck> {

    /**
     * Creează un nou card pentru rațe zburătoare.
     * @param id identificatorul unic al cardului
     * @param numeCard  numele cardului
     * @param tip tipul cardului (ex. "Zburator")
     */
    public FlyingCard(Long id, String numeCard,  String tip) {
        super(id, numeCard, tip);
    }


    @Override
    public String toString() {
        return "FLYINGCARD" + ";" + super.toString();
    }
}
