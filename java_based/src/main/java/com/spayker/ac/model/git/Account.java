package com.spayker.ac.model.git;

public class Account {

    private String email;
    private String password;

    private Account() {}

    public Account(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
