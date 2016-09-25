package com.originalsbytoto.bukkit.sqlperms;

import java.sql.*;
import java.util.UUID;

import org.bukkit.entity.Player;

public class SQLManager {

	Connection conn = null;
	
	String DBUser;
	String DBPass;
	String DBHost;
	String DBName;
	int DBPort;
	
	String url;
	
	public SQLManager(String user, String pass, String host, String bdd, int port) {
		
		this.DBUser = user;
		this.DBPass = pass;
		this.DBHost = host;
		this.DBName = bdd;
		this.DBPort = port;
		
	}
	
	public void SetDefaultGroup(String group, Player player) {
		
		UUID uuid = player.getUniqueId();
		
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet res = st.executeQuery("SELECT COUNT(*) AS total FROM users WHERE uuid='" + uuid.toString() + "'; ");
			res.next();
			if(res.getInt("total") == 0) {
				
				st.executeUpdate( "INSERT INTO users (id, username, uuid, groups) VALUES (NULL, '" + player.getName() + "', '" + uuid.toString() + "', '" + group + "');" );
				
			} else {
				
				//System.out.print("already");
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public boolean Init() {
		
		Statement stmt = null;

		this.url = "jdbc:mysql://" + this.DBHost + ":" + DBPort +"/" + this.DBName;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, DBUser, DBPass);
			stmt = conn.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS `users` (`id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,`uuid` text NOT NULL,`username` text NOT NULL,`groups` text NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;"; 
	        stmt.executeUpdate(sql);
		} catch (ClassNotFoundException e) {
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
	public String[] getGroups(UUID uuid) {
		
		try {
			Statement statement = conn.createStatement();
			ResultSet resultat = statement.executeQuery("SELECT groups FROM `users` WHERE uuid='" + uuid.toString() + "';");
			resultat.next();
			return resultat.getString("groups").split(";");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
}
