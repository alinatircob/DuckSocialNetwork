package org.example.ducksocialnetworkm.domeniu.page;

import org.example.ducksocialnetworkm.domeniu.user.User;
import java.util.List;

public class ProfilePage {
    private User user;
    private List<User> prieteni;

    public ProfilePage(User user, List<User> prieteni) {
        this.user = user;
        this.prieteni = prieteni;
    }

    public User getUser() {
        return user;
    }

    public List<User> getPrieteni() {
        return prieteni;
    }
}