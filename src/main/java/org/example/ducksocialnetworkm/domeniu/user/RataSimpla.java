package org.example.ducksocialnetworkm.domeniu.user;

/**
 * Clasă care reprezintă o rață simplă, fără comportamente speciale (nu zboară sau înoată suplimentar).
 */

public class RataSimpla extends Rata {

    /**
     * Creează o nouă instanță de rață simplă.
     * @param id identificatorul unic al raței
     * @param username numele de utilizator
     * @param email adresa de email
     * @param password parola asociată
     * @param rezistenta valoarea rezistenței raței
     * @param viteza valoarea vitezei raței
     * @param tip tipul raței (vezi {@link TipRata})
     */
    public RataSimpla(Long id, String username, String email, String password, Double rezistenta, Double viteza, TipRata tip) {
        super(id, username, email, password, rezistenta, viteza, tip);
    }

    /**
     * Afișează acțiunea specifică unei rațe simple.
     */
    @Override
    public void actiune() {
        System.out.println(getId() + "este o rata simpla!");
    }

}
