package org.example.ducksocialnetworkm.domeniu.validator;
import org.example.ducksocialnetworkm.domeniu.user.Rata;
import org.example.ducksocialnetworkm.domeniu.user.TipRata;


/**
 * Clasă care validează obiecte de tip {@link Rata}.
 * Verifică dacă toate câmpurile respectă regulile de validare definite.
 */

public class RataValidator implements ValidatorDomeniu<Rata> {

    /**
     * Validează o rață verificând:
     * <ul>
     *     <li>ID pozitiv</li>
     *     <li>Username nevid</li>
     *     <li>Email valid (conține '@')</li>
     *     <li>Parolă cu minim 8 caractere</li>
     *     <li>Tip de rață valid (una dintre valorile {@link TipRata})</li>
     *     <li>Viteză și rezistență pozitive</li>
     * </ul>
     *
     * @param rata rața care trebuie validată
     * @throws ValidationException dacă există erori de validare
     */

    public void valideaza(Rata rata) throws ValidationException{

        String erori ="";

        if (rata.getId() == null || rata.getId() <= 0)
            erori+="ID-ul trebuie sa fie pozitiv!\n";

        if (rata.getUsername() == null || rata.getUsername().isBlank())
            erori+="Username invalid!\n";

        if (rata.getEmail() == null || !rata.getEmail().contains("@"))
            erori+="Email invalid!\n";

        if (rata.getPassword() == null || rata.getPassword().length() < 8)
            erori+="Parola este prea scurta!\n";

        if (rata.getTip() == null)
            erori+="Tip invalid!\n";

        if (rata.getViteza() <= 0)
            erori+="Viteza trebuie sa fie pozitiva!\n";

        if (rata.getRezistenta() <= 0)
            erori+="Rezistenta trebuie sa fie pozitiva!\n";

        if(!erori.isEmpty())
            throw new ValidationException(erori);

    }
}
