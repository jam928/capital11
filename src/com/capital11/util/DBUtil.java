package com.capital11.util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

 

public class DBUtil {
	
	public static Connection getConnection() throws SQLException
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			String url  = "jdbc:mysql://localhost:3306/capital_11";
			String username = "root";
			String password = "";
			return DriverManager.getConnection(url, username,password);
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;	
	}
	// Process Exception errors
	public static void processException(SQLException e)
	{
		System.out.println("Error message: " + e.getMessage());
		System.out.println("Error code: " + e.getErrorCode());
		System.out.println("SQL State: " + e.getSQLState());
	}
}
