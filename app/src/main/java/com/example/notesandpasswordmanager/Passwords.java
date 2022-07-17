package com.example.notesandpasswordmanager;

public class Passwords {
    String password,password_name;
    // empty constructor
    public  Passwords(){}

    public String getPassword() {
        return password;
    }

    public String getPassword_name() {
        return password_name;
    }

    public Passwords(String Password, String Password_name){
        this.password  = Password;
        this.password_name= Password_name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPassword_name(String password_name) {
        this.password_name = password_name;
    }
}
