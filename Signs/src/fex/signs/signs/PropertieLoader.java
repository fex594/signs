package fex.signs.signs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import fex.signs.util.ConnectionInfo;

public class PropertieLoader {
	private final String path = "./plugins/Signs";

	public Properties loadProperties() {
		File file = new File(path + "/sql.properties");
		if (!file.exists()) {
			initFile();
		}

		Properties props = new Properties();
		try {
			FileInputStream in = new FileInputStream(path + "/sql.properties");
			props.load(in);
			in.close();
		} catch (IOException e) {
			System.out.println("Geben sie ihre Datenbankverbindungen an");
		}
		return props;
	}
	
	private void initFile() {
		try {
			File folder = new File(path);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			PrintWriter w = new PrintWriter(new FileOutputStream(path + "/sql.properties"));
			w.println("host=localhost");
			w.println("port=3306");
			w.println("dbName=database");
			w.println("username=root");
			w.println("pw=root");
			w.flush();
			w.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public ConnectionInfo getConnectionInfo(){
		Properties props = loadProperties();
		String host = props.getProperty("host");
		int port = Integer.valueOf(props.getProperty("port"));
		String dbName = props.getProperty("dbName");
		String username = props.getProperty("username");
		String pw = props.getProperty("pw");
		return new ConnectionInfo(host,port,dbName,username,pw);
		
	}
}
