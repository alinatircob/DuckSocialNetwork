package org.example.ducksocialnetworkm.domeniu.user;

/**
 * Clasă care reprezintă o rață din sistem, extinzând clasa abstractă {@link User}.
 * Include atribute specifice precum tipul raței, rezistența și viteza.
 */

public abstract class Rata extends User {

    private TipRata tip;
    private Double rezistenta;
    private Double viteza;
    private Long idCard;


    /**
     * Constructor pentru inițializarea unei rațe cu datele sale.
     *
     * @param id identificatorul unic: Long
     * @param username numele de utilizator: String
     * @param email adresa de email: String
     * @param password parola: String
     * @param rezistenta nivelul de rezistență: Double
     * @param viteza viteza raței: Double
     * @param tip tipul raței: TipRata
     */

    public Rata(Long id, String username, String email, String password, Double rezistenta, Double viteza, TipRata tip) {
        super(id, username, email, password);
        this.rezistenta = rezistenta;
        this.viteza = viteza;
        this.tip = tip;
    }

    /** @return rezistența raței: Double */

    public Double getRezistenta() {
        return rezistenta;
    }

    /** @param rezistenta: Double
     * setează rezistența raței */

    public void setRezistenta(Double rezistenta) {
        this.rezistenta = rezistenta;
    }

    /** @return viteza raței: Double */

    public Double getViteza() {
        return viteza;
    }

    /** @param viteza: Double
     * setează viteza raței */

    public void setViteza(Double viteza) {
        this.viteza = viteza;
    }

    /** @return tipul raței: TipRata */

    public TipRata getTip() {
        return tip;
    }

    /** @param tip: TipRata
     * setează tipul raței */

    public void setTip(TipRata tip) {
        this.tip = tip;
    }

    /**
     * @return o reprezentare textuală a raței sub forma "RATA;id;username;email;password;rezistenta;viteza;tip"
     */

    public void setIdCard(Long idCard) {
        this.idCard = idCard;
    }

    public Long getIdCard() {
        return idCard;
    }

    public TipRata getTipRata(){
        return tip;
    }

    @Override
    public String toString() {
        return "RATA;" + super.toString() + rezistenta + ";" + viteza + ";" + tip + ";" + idCard;
    }

    public abstract void actiune();


}
