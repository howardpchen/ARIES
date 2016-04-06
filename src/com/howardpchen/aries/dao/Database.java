package com.howardpchen.aries.dao;

import java.sql.Connection;
import java.sql.DriverManager;

import com.howardpchen.aries.AriesConfig;

public class Database {
 
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(AriesConfig.DB_URL, 
            		AriesConfig.DB_USERNAME, AriesConfig.DB_PASSWORD);

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
