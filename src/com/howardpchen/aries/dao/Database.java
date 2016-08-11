package com.howardpchen.aries.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 

public class Database {
 
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            String url = AriesDatabaseConfig.DB_URL;
            String user = AriesDatabaseConfig.DB_USERNAME;
            String pass = AriesDatabaseConfig.DB_PASSWORD;

            Connection con = DriverManager.getConnection(url, user, pass);
            return con;
        } 
        catch (ClassNotFoundException ex) {
          System.out.println("Class com.mysql.jdbc.Driver not found!");
          System.out.println("Make sure that mysql_connector-java-5.1.38-bin.jar is added to the project libraries as an external jar, and also copy that file to the tomcat/lib/ directory");
          return null;
        }
        catch (SQLException ex) {
        	System.out.println("SQLException" + ex.getMessage() );
        	System.out.println("SQLState: " + ex.getSQLState());
        	return null;
        }
        catch (Exception ex) {
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
