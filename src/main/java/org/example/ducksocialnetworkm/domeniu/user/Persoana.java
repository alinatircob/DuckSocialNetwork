package org.example.ducksocialnetworkm.domeniu.user;


/**
 * Clasă care reprezintă o persoană din sistem, extinzând clasa abstractă {@link User}.
 * Conține informații personale suplimentare precum nume, prenume, data nașterii,
 * ocupație și nivel de empatie.
 */

public class Persoana extends User {

    private String nume;
    private String prenume;
    private String dataNasterii;
    private String ocupatie;
    private Integer nivelEmpatie;

    /**
     * Constructor care inițializează o persoană cu datele de bază și cele personale.
     *
     * @param id identificatorul unic al utilizatorului: Long
     * @param username numele de utilizator: String
     * @param email adresa de email: String
     * @param password parola utilizatorului: String
     * @param nume numele persoanei: String
     * @param prenume prenumele persoanei: String
     * @param dataNasterii data nașterii: String
     * @param ocupatie ocupația persoanei: String
     */

    public Persoana(Long id, String username, String email, String password, String nume, String prenume, String dataNasterii, String ocupatie) {
        super(id, username, email, password);
        this.nume = nume;
        this.prenume = prenume;
        this.dataNasterii = dataNasterii;
        this.ocupatie = ocupatie;
    }

    /** @return numele persoanei: String */

    public String getNume() {
        return nume;
    }

    /** @param nume: String
     * setează numele persoanei */

    public void setNume(String nume) {
        this.nume = nume;
    }

    /** @return prenumele persoanei: String */

    public String getPrenume() {
        return prenume;
    }

    /** @param prenume: String
     * setează prenumele persoanei */

    public void setPrenume(String prenume) {
        this.prenume = prenume;
    }

    /** @return data nașterii persoanei: String */

    public String getDataNasterii() {
        return dataNasterii;
    }

    /** @param dataNasterii: String
     * setează data nașterii persoanei */

    public void setDataNasterii(String dataNasterii) {
        this.dataNasterii = dataNasterii;
    }

    /** @return ocupația persoanei: String */

    public String getOcupatie() {
        return ocupatie;
    }

    /** @param ocupatie: String
     * setează ocupația persoanei */

    public void setOcupatie(String ocupatie) {
        this.ocupatie = ocupatie;
    }

    /** @return nivelul de empatie al persoanei: Integer */

    public Integer getNivelEmpatie() {
        return nivelEmpatie;
    }

    /** @param nivelEmpatie: Integer
     * setează nivelul de empatie al persoanei */

    public void setNivelEmpatie(Integer nivelEmpatie) {
        this.nivelEmpatie = nivelEmpatie;
    }

    /**
     * Creează o reprezentare textuală a persoanei, incluzând toate câmpurile relevante.
     * @return un șir cu toate datele persoanei separate prin ';'
     */

    @Override
    public String toString() {
        return "PERSOANA;" + super.toString() + nume + ";" + prenume + ";" + dataNasterii + ";" + ocupatie;
    }
}
