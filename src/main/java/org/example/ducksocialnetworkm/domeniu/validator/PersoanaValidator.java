package org.example.ducksocialnetworkm.domeniu.validator;
import org.example.ducksocialnetworkm.domeniu.user.Persoana;


/**
 * Clasă responsabilă de validarea obiectelor de tip {@link Persoana}.
 * Verifică dacă toate câmpurile respectă regulile de consistență și format.
 */

public class PersoanaValidator implements ValidatorDomeniu<Persoana> {

    /**
     * Validează o persoană verificând:
     * <ul>
     *     <li>ID pozitiv</li>
     *     <li>Username nevid</li>
     *     <li>Email valid (conține '@')</li>
     *     <li>Parolă cu minim 8 caractere</li>
     *     <li>Nume și prenume doar cu litere</li>
     *     <li>Data nașterii în formatul dd-mm-yyyy</li>
     *     <li>Ocupație doar cu litere</li>
     * </ul>
     *
     * @param persoana persoana care trebuie validată
     * @throws ValidationException dacă există erori de validare
     */

    public void valideaza(Persoana persoana) throws ValidationException{

        String erori ="";

        if (persoana.getId() == null || persoana.getId() <= 0)
            erori+="ID-ul trebuie sa fie pozitiv!\n";

        if (persoana.getUsername() == null || persoana.getUsername().isBlank())
            erori+="Username invalid!\n";

        if (persoana.getEmail() == null || !persoana.getEmail().contains("@"))
            erori+="Email invalid!\n";

        if (persoana.getPassword() == null || persoana.getPassword().length() < 8)
            erori+="Parola este prea scurta!\n";

        if (persoana.getNume() == null || persoana.getNume().isBlank())
            erori+="Nume invalid!\n";
        if (!persoana.getNume().chars().allMatch(Character::isLetter))
            erori+="Numele trebuie sa contina doar litere!\n";

        if (persoana.getPrenume() == null || persoana.getPrenume().isBlank())
            erori+="Prenume invalid!\n";
        if (!persoana.getPrenume().chars().allMatch(Character::isLetter))
            erori+="Prenumele trebuie sa contina doar litere!\n";

        String data = persoana.getDataNasterii();
        if (persoana.getDataNasterii() == null || persoana.getDataNasterii().isBlank())
            erori+="Data nasterii invalida!\n";
        if (!data.matches("\\d{4}-\\d{2}-\\d{2}"))
            erori+="Data nasterii trebuie să fie de forma: yyyy-mm-dd!\n";

        if (persoana.getOcupatie() == null || persoana.getOcupatie().isBlank())
            erori+="Ocupatie invalida!\n";
        if (!persoana.getOcupatie().chars().allMatch(Character::isLetter))
            erori+="Ocupatia trebuie sa conțina doar litere!\n";

        if(!erori.isEmpty())
            throw new ValidationException(erori);

    }

}
