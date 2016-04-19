package com.howardpchen.aries.dao;

import java.sql.Connection;
import java.sql.DriverManager;
 
public class Database {
 
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://107.170.166.121/ariesdb?useSSL=false",
                  "ssarda", "xremdaGnbVAjJG4m");
            //Connection con = DriverManager.getConnection("jdbc:mysql://localhost/ariesdb?useSSL=false",
             //      "root", "$nCsioa@qF0&1");
            return con;
        } catch (Exception ex) {
            System.out.println("Database.getConnection() Error -->" + ex.getMessage());
            return null;
        }
    }
 
    public static void close(Connection con) {
        try {
            con.close();
        } catch (Exception ex) {
        }
    }
}
