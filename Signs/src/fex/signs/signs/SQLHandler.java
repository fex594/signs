package fex.signs.signs;

import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mysql.jdbc.Connection;

import fex.signs.util.ConnectionInfo;
import fex.signs.util.PlayerSign;

public class SQLHandler {
	private static SQLHandler instance;
	private Connection connection;				//Datenbankverbindung
	private ConnectionInfo ci;					//Logininfos für DB
	public List<PlayerSign> active;				//Liste der aktiven Schilder
	public List<PlayerSign> abgelaufen;			//Liste der abgelaufenen Schilder
	private static int highID;

	/**
	 * Deaktiviert den Standartkonstruktor
	 */
	private SQLHandler() {
	}

	private SQLHandler(ConnectionInfo ci) {
		this.ci = ci;
		openConnection();
		setupTable();
	}

	/**
	 * Gibt eine Singleton-Instanz von SQLHandler zurück
	 * @return Instanz von SQLHandler
	 */
	public static synchronized SQLHandler getInstance() {
		if (instance == null) {

			SQLHandler.instance = new SQLHandler(new PropertieLoader().getConnectionInfo());
		}
		return SQLHandler.instance;
	}

	/**
	 * Herstellen einer Verbindung mit der Datenbank
	 * 
	 * @param ci
	 *            Verbindungsinformationen
	 */
	private void openConnection() {
		try {
			connection = (Connection) DriverManager.getConnection(
					"jdbc:mysql://" + ci.getHost() + ":" + ci.getPort() + "/" + ci.getDbName() + "?useSSL=false",
					ci.getUsername(), ci.getPw());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private void closeConnection() {
		if(connection!= null) {
			try {
				connection.commit();
				connection.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Einrichten der Datenbank, falls nicht vorhanden
	 */
	private void setupTable() {
		try {
			String string = "CREATE TABLE IF NOT EXISTS Schilder (ID int AUTO_INCREMENT NOT NULL PRIMARY KEY, Player VARCHAR(36) NOT NULL, Active int(1) NOT NULL, Text VARCHAR(255), Loc VARCHAR(60) NOT NULL, Datum Date NOT NULL, Typ VARCHAR(25) NOT NULL, Ersteller VARCHAR(36) NOT NULL, Lastdate DATE NOT NULL)";
			PreparedStatement sql = connection.prepareStatement(string);
			sql.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Legt eine neues Schild in der Datenbank an
	 * 
	 * @param name
	 *            Spielername
	 * @param date
	 *            Ablaufdatum
	 * @param active
	 *            Aktivität: 1 - Aktiv, 0 - Inaktiv
	 * @param loc
	 *            Schildposition
	 * @param text
	 *            Beschreibung zum Schild
	 * @param typ
	 *            Schild-Typ
	 * @return ID des Schilds
	 */
	public int putNewSign(String name, Date date, int active, String loc, String typ, String ersteller, Date lastDate) {
		String type = typ.replace("[", "").replace("]", "");
		try {
			String st = "INSERT INTO Schilder (Player, Active, Loc, Datum, Typ, Ersteller, Lastdate) VALUES ('" + name
					+ "', " + active + ", '" + loc + "', '" + date + "', '" + type + "', '" + ersteller + "', '"
					+ lastDate + "')";
			PreparedStatement sql = connection.prepareStatement(st);
			sql.executeUpdate();
			String sts = "SELECT MAX(ID) AS localid From Schilder";
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			int count = 0;
			while (set.next()) {
				count = set.getInt("localid");
			}
			return count;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public ResultSet getActiveSigns() {
		String sts = "SELECT *  FROM Schilder WHERE Active = 1";
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			return set;
		} catch (SQLException e) {
			return null;
		}
	}
	
	public int getMaxID() {
		String sts = "SELECT MAX(ID) AS Maximum FROM Schilder";
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			return set.getInt("Maximum");
		} catch (SQLException e) {
			return null;
		}
	}
	
	public void update() {
		this.openConnection();						//Datenbankverbindung öffnen
				
		ResultSt result = getActiveSigns();			//Aktive Schilder sortieren und einordnen
		this.active = new ArrayList<PlayerSign>();
		while(result.next()) {
			PlayerSign ps = new PlayerSign(result.getInt("ID"))		//Neue PlayerSign aus Daten de aktuellen Datenbankeintrags
			active.add(ps);						
			if(result.getDate("Datum")<=now()) {
				this.abgelaufen.add(ps);
				
			}
		}
		this.highID = getMaxID();					//Maximums-ID aktualisieren
		
		this.closeConnection();						//Datenbankverbindung schließen
	}
}
