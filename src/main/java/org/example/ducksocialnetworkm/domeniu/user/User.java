package org.example.ducksocialnetworkm.domeniu.user;

import org.example.ducksocialnetworkm.domeniu.observer.IObserver;

import java.util.ArrayList;
import java.util.List;


/**
 * Clasă abstractă care modelează un utilizator generic din sistem.
 * Conține informații de bază precum id, username, email, parolă și lista de prieteni.
 */

public abstract class User implements IObserver {

    private Long id;
    private String username;
    private String email;
    private String password;
    private List<User> prieteni;

    /**
     * Constructor pentru inițializarea unui utilizator.
     * @param id identificatorul unic: Long
     * @param username numele de utilizator: String
     * @param email adresa de email: String
     * @param password parola: String
     */

    public User(Long id, String username, String email, String password)  {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.prieteni = new ArrayList<>();
    }

    /** @return identificatorul utilizatorului: Long */

    public Long getId() {
        return id;
    }

    /** @param id: Long
     * setează identificatorul utilizatorului */

    public void setId(Long id) {
        this.id = id;
    }

    /** @return numele de utilizator: String */

    public String getUsername() {
        return username;
    }

    /** @param username: String
     *  setează numele de utilizator */

    public void setUsername(String username) {
        this.username = username;
    }

    /** @return adresa de email: String */

    public String getEmail() {
        return email;
    }

    /** @param email:String
     *  setează adresa de email */

    public void setEmail(String email) {
        this.email = email;
    }

    /** @return parola utilizatorului: String */

    public String getPassword() {
        return password;
    }

    /** @param password: String
     * setează parola utilizatorului */

    public void setPassword(String password) {
        this.password = password;
    }

    /** @return lista de prieteni ai utilizatorului */

    public List<User> getPrieteni() {
        return prieteni;
    }

    public void adaugaPrieten(User user) {
        this.prieteni.add(user);
    }

    public void stergePrieten(User user) {
        this.prieteni.remove(user);
    }

    /** @param prieteni: User
     * setează lista de prieteni */

    public void setPrieteni(List<User> prieteni) {
        this.prieteni = prieteni;
    }

    /**
     * @return o reprezentare textuală a utilizatorului sub formă de șir delimitat prin ';'
     */

    @Override
    public String toString() {
        return id + ";" + username + ";" + email + ";" + password + ";";
    }

    @Override
    public void update(String mesaj) {
        System.out.println(getUsername() + " a primit notificare: " + mesaj);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id.equals(user.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
