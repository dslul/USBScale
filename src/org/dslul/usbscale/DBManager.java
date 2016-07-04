package org.dslul.usbscale;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DBManager {


	private Statement stat = null;
	private Statement stat2 = null;
    private Connection conn;
    private ResultSet resultSet;
    private ResultSet resultSet2;
    
    
	public DBManager() {
        try {
        	Class.forName("org.sqlite.JDBC");
        	String dir = System.getProperty("user.home") + "/.usbscale/";
        	new File(dir).mkdir();
			conn = DriverManager.getConnection("jdbc:sqlite:"+ dir +"data.db");
	        System.out.println("DB opened");
	        //if table does not exist, create it
	        DatabaseMetaData dbm = conn.getMetaData();
	        ResultSet tables = dbm.getTables(null, null, "UserData", null);
	        if (tables.next() == false) {
	            System.out.println("Database does not exist, creating tables...");
	            this.createTable();
	        }
        } catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isEmpty() {
		return isQueryEmpty("select * from UserData;");
	}
	
	private boolean isQueryEmpty(String query) {
		boolean result = true;
        try {
        	stat = conn.createStatement();
			resultSet = stat.executeQuery(query);
			if (resultSet.next()) {
	        	result = false;
	        }
			resultSet.close();
	        stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}        
		return result;
	}
	
	private void createTable() throws SQLException {
        stat = conn.createStatement();

        String sql1 = "CREATE TABLE `UserMeasurement` (userId INTEGER NOT NULL, "+
        "datetime INTEGER NOT NULL, weight REAL NOT NULL, bodyfat REAL NOT NULL,"+
        		" water REAL NOT NULL, muscle REAL NOT NULL, PRIMARY KEY (userId, datetime), "+
        "FOREIGN KEY (userId) REFERENCES UserData(`id`) ) WITHOUT ROWID;";
        
        String sql2 = "CREATE TABLE `UserData` (id INTEGER PRIMARY KEY NOT NULL, name TEXT, "+
        		"birthDate TEXT, height INTEGER, gender TEXT,"+
        		" activity INTEGER, lastDownload TEXT) WITHOUT ROWID;";

        stat.executeUpdate("drop table if exists UserMeasurement;");
        stat.executeUpdate("drop table if exists UserData;");
        stat.executeUpdate(sql1);
        stat.executeUpdate(sql2);

        stat.close();
        System.out.println("DB created");
    }
	
	public boolean userExists(User user) {
		return !isQueryEmpty("select * from UserData where id="+user.getId()+";");
	}
	
	public boolean measurementExists(int userid, Measurement mes) {
		return !isQueryEmpty("SELECT * FROM UserMeasurement WHERE datetime="+mes.getUnixTimestamp()+
							" AND userId="+userid+";");
	}
	
	public void addUsers(List<User> users) {
		try {
			Statement sta = conn.createStatement();
			for(User user : users) {
				if(userExists(user) == false) {
					System.out.println("Added user: " + user);
					String sql = "INSERT INTO `UserData`(`id`,`name`,`birthDate`,"+
							"`height`,`gender`,`activity`,`lastDownload`) VALUES "+
							"("+user.getId()+","+user.getName()+","+user.getBirthDate()+","+
							user.getHeight()+","+user.getGender().name()+","+user.getActivity()+",NULL);";
					sta.executeUpdate(sql);
				}
				//add measurements
				for(Measurement mes : user.getMeasurements()) {
					if(measurementExists(user.getId(), mes) == false) {
						System.out.println("Added measurement: " + mes);
						String sql = "INSERT INTO `UserMeasurement`(`userId`,`datetime`"+
								",`weight`,`bodyfat`,`water`,`muscle`) VALUES "+
								"("+user.getId()+","+mes.getUnixTimestamp()+",'"+mes.getWeight()+
								"','"+mes.getBodyfat()+"','"+mes.getWater()+"','"+mes.getMuscle()+"');";
						sta.executeUpdate(sql);
					}
				}
			}
			sta.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
        try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
	
	public void changeUserName(User user, String newName) throws SQLException {
		Statement statement = conn.createStatement();
		statement.executeUpdate("UPDATE `UserData` SET `name`='"+newName+
    			"' WHERE `id`="+user.getId()+";");
		statement.close();
	}

	public List<User> getSavedUsers() {
		List<User> users = new ArrayList<>();
        try {
        	stat = conn.createStatement();
        	resultSet = stat.executeQuery("select * from UserData;");

			while (resultSet.next()) {
				List<Measurement> measurements = new ArrayList<>();
			    int userid = resultSet.getInt("id");
			    stat2 = conn.createStatement();
	        	resultSet2 = stat2.executeQuery("select * from UserMeasurement where userId="+userid+";");
	        	while (resultSet2.next()) {
	        		Timestamp tstamp =  new Timestamp(resultSet2.getLong("datetime")*1000);
	        		Measurement mes = new Measurement(resultSet2.getDouble("weight"), 
	        											resultSet2.getDouble("bodyfat"), 
	        											resultSet2.getDouble("water"), 
	        											resultSet2.getDouble("muscle"), 
	        											tstamp.toLocalDateTime());
	        		measurements.add(mes);
	        	}
	        	resultSet2.close();
		        stat2.close();
	        	User user = new User(userid, resultSet.getString("name"), resultSet.getString("birthDate"), 
	        			resultSet.getInt("height"), resultSet.getString("gender"), 
	        			resultSet.getInt("activity"), measurements);
	        	users.add(user);
			}
		
	        resultSet.close();
	        stat.close();
        } catch (SQLException e) {
			e.printStackTrace();
		}
		return users;
	}

}
