package com.btec.fpt.campus_expense_manager.models;

public class DataStatic {
    public static String email = "";

    // Optional: Method to set email after login
    public static void setEmail(String userEmail) {
        email = userEmail;
    }

    // Optional: Method to clear email on logout
    public static void clearEmail() {
        email = "";
    }
}