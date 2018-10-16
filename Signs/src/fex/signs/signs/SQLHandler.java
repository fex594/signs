package fex.signs.signs;

import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.mysql.jdbc.Connection;

import fex.signs.util.ConnectionInfo;
import fex.signs.util.NoSignFoundException;
import fex.signs.util.PSConverter;
import fex.signs.util.PlayerSign;

public class SQLHandler {
	private static SQLHandler instance;
	private Connection connection; // Datenbankverbindung
	private ConnectionInfo ci; // Logininfos für DB
	private static int maxID; // Aktuelle Max-ID

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
	 * 
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

	public void closeConnection() {
		if (connection != null) {
			try {
				connection.close();
				System.out.println("Verbindung geschlossen");
			} catch (SQLException e) {
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
			return ++maxID;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	// Liefert alle aktiven Schilder zurück
	public List<PlayerSign> getActiveSigns() {
		String sts = "SELECT *  FROM Schilder WHERE Active = 1";
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			ArrayList<PlayerSign> active = new ArrayList<>();
			while (set.next()) {
				PlayerSign ps = PSConverter.convert(set); // Neue PlayerSign aus Daten de aktuellen Datenbankeintrags
				active.add(ps);
			}
			return active;
		} catch (SQLException e) {
			System.out.println("Fehler getActiveSigns");
			return null;
		}
	}
	
	public PlayerSign getSign(int ID) throws NoSignFoundException {
		openConnection();
		
		String sts = "SELECT * FROM Schilder WHERE ID = "+ID;
		PlayerSign ps = null;
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			while(set.next()) {
				ps = PSConverter.convert(set);
			}
		}catch(SQLException e) {
			e.printStackTrace();
			throw new NoSignFoundException();
		}
		return ps;
	}

	public int getMaxID() {
		openConnection();

		String sts = "SELECT MAX(ID) AS Maximum FROM Schilder";
		int returned = -1;
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			while(set.next()) {
			returned = set.getInt("Maximum");
			}
		} catch (SQLException e) {
			System.out.println("Fehler getMaxID");
			e.printStackTrace();
		}
		return returned;

	}

	public List<PlayerSign> update() throws SQLException {
		openConnection(); // Datenbankverbindung öffnen

		List<PlayerSign> result = getActiveSigns(); // Aktive Schilder sortieren und einordnen

		closeConnection(); // Datenbankverbindung schließen
		return result;
	}

	public static Date now() {
		Calendar c = Calendar.getInstance();
		return new Date(c.getTimeInMillis());
	}
}
