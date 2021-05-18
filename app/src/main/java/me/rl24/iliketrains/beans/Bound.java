package me.rl24.iliketrains.beans;

public class Bound {

    public String title;

    // Each row in the table
    public Trip[] trips;

    // Each stop from A - B. waik, para, etc. usually 13 for the KPL line
    public Stop[] stops;

}
