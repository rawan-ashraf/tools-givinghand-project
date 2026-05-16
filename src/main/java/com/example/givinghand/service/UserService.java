package com.example.givinghand.service;

import com.example.givinghand.dto.Request;
import com.example.givinghand.dto.login;
import com.example.givinghand.dto.update;
import com.example.givinghand.entity.user;
import com.example.givinghand.util.Validation;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.sql.Date;

@Stateless
public class UserService {

    @PersistenceContext(unitName = "givingHandPU")
    private EntityManager em;

    // ================= REGISTER =================
    public String register(Request req) {

        if (req.email == null || req.password == null || req.name == null || req.role == null) {
            return "Missing required fields";
        }

        if (!Validation.isValidEmail(req.email))
            return "Invalid email";

        if (!Validation.isValidBirthday(req.birthday))
            return "Invalid birthday";

        if (findByEmail(req.email) != null)
            return "Email already exists";

        String role = req.role.toLowerCase();
        if (!role.equals("donor") && !role.equals("organization"))
            return "Role must be 'donor' or 'organization'";

        user user = new user();
        user.setEmail(req.email);
        user.setPassword(req.password);
        user.setName(req.name);
        user.setBirthday(Date.valueOf(req.birthday));
        user.setBio(req.bio);
        user.setRole(role);

        em.persist(user);

        return "User registered successfully.";
    }

    // ================= LOGIN =================
    public user login(login req) {

        if (req.email == null || req.password == null)
            return null;

        user user = findByEmail(req.email);

        if (user == null)
            return null;

        if (!user.getPassword().equals(req.password))
            return null;

        return user;
    }

    // ================= UPDATE =================
    public String updateProfile(update req) {

        if (req.name == null || req.name.trim().isEmpty())   // replaced isBlank()
            return "Missing or invalid name";

        user user = em.find(user.class, req.userId);

        if (user == null)
            return "User not found";

        user.setName(req.name);
        user.setBio(req.bio);

        em.merge(user);

        return "Profile updated successfully";
    }

    // ================= HELPERS =================

    public user findByEmail(String email) {
        return em.createQuery(
                        "SELECT u FROM user u WHERE u.email = :email",
                        user.class
                )
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public user findUserById(int id) {
        return em.find(user.class, id);
    }
}