package org.example.ducksocialnetworkm.domeniu.validator;
import org.example.ducksocialnetworkm.domeniu.card.Card;

public class CardValidator implements ValidatorDomeniu<Card>{

    public void valideaza(Card card) throws ValidationException{

        String erori ="";

        if (card.getId() == null || card.getId() <= 0)
            erori+="ID-ul trebuie sa fie pozitiv!\n";
        if (card.getNume() == null || card.getNume().isBlank())
            erori += "Nume invalid!\n";
        if (!card.getNume().chars().allMatch(Character::isLetter))
            erori += "Numele trebuie sa contina doar litere!\n";

        if(!erori.isEmpty())
            throw new ValidationException(erori);

    }

}
