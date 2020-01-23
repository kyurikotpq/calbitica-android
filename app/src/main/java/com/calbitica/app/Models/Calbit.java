package com.calbitica.app.Models;

// Models ONE calbit
public class Calbit {
    String _id; // MongoDB ID
    String userID; // MongoDB ID
    String calendarID; // MongoDB Calendar ID
    String googleID; // Google Event ID, can be null - only exists on Habitica [not supported yet]

    boolean isDump;  // true if brain dump or not assigned date time yet
    boolean display; // false if you don't want it displayed

    String summary; // Title
    String description; // description
    String location;

    CalendarDate start;
    CalendarDate end;

    Completed completed;
}