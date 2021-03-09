package finalproject.db;
import java.sql.*;
import java.util.ArrayList;

import finalproject.entities.Person;

public class DBInterface {


	
	Connection conn;
	
	public DBInterface() {
		
	}
	
	public Connection getConn() {
		return this.conn;
	}
	
	public void setConnection() throws SQLException {
		//this.conn =
	}
	
}
