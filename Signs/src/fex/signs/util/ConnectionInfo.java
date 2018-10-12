package fex.signs.util;

public class ConnectionInfo {
	private String host;
	private int port;
	private String dbName;
	private String username;
	private String pw;
	public ConnectionInfo(String host, int port, String dbName, String username, String pw) {
		super();
		this.host = host;
		this.port = port;
		this.dbName = dbName;
		this.username = username;
		this.pw = pw;
	}
	public String getHost() {
		return host;
	}
	public int getPort() {
		return port;
	}
	public String getDbName() {
		return dbName;
	}
	public String getUsername() {
		return username;
	}
	public String getPw() {
		return pw;
	}
	
	
}
