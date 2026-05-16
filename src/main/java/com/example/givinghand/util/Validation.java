package com.example.givinghand.util;

import java.time.LocalDate;

//the birthday and email should have extra validation
public class Validation {
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");// ^ start of string ,$ end

    }


    public static boolean isValidBirthday(String birthday) {

        try {
            LocalDate date = LocalDate.parse(birthday);

            if (date.isAfter(LocalDate.now())) { // if date in the future false
                return false;
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }

}
