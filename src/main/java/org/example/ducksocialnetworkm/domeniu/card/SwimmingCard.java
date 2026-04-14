package org.example.ducksocialnetworkm.domeniu.card;
import org.example.ducksocialnetworkm.domeniu.user.SwimmingDuck;

/**
 * Clasa {@code SwimmingCard} reprezintă un card pentru rațe înotătoare.
 * Extinde clasa abstractă {@link Card}.
 */
public class SwimmingCard extends Card<SwimmingDuck> {

    /**
     * Creează un nou card pentru rațe înotătoare.
     * @param id  identificatorul unic al cardului
     * @param numeCard  numele cardului
     * @param tip tipul cardului (ex. "Inotator")
     */
    public SwimmingCard(Long id, String numeCard,  String tip) {
        super(id, numeCard, tip);
    }

    @Override
    public String toString() {
        return "SWIMMINGCARD" + ";" + super.toString();
    }
}