package com.example.givinghand.entity;

import jakarta.persistence.*;
import java.util.Date;
@Entity
@Table(name = "users")// fy data base ykon 2smo users

public class user {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    private int id;
    @Column(unique = true, nullable = false) // required , and dont duplicate
    private String email;
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Temporal(TemporalType.DATE) // as 20-4-2026
    private Date birthday;

    @Column(nullable = false) // donor wla organization
    private String role;

    private String bio;

    public int getId() { // mlosh set hwa auto inc
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Date getBirthday() {
        return birthday;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
    public String getRole() {
        return role;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }


}

