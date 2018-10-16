package fex.signs.signs;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Connection;

import fex.signs.util.ConnectionInfo;
import fex.signs.util.Messages;
import fex.signs.util.NoSignFoundException;
import fex.signs.util.PSConverter;
import fex.signs.util.PlayerSign;

public class SQLHandler {
	private static SQLHandler instance;
	private Connection connection; // Datenbankverbindung
	private ConnectionInfo ci; // Logininfos für DB

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
	
	public List<PlayerSign> sendResultStatements(String args) {
		openConnection(); // Datenbankverbindung öffnen

		ArrayList<PlayerSign> result = new ArrayList<>(); 
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(args);
			while(set.next()) {
				result.add(PSConverter.convert(set));
			}}catch(SQLException e) {
				Messages.getInstance().toConsole("Fehler beim Senden/Empfangen von Daten: "+args);
				e.printStackTrace();
			}

		closeConnection(); // Datenbankverbindung schließen
		return result;
	}
	
	public boolean sendStatement(String args) {
		boolean success = false;
		
		openConnection();
		
		try {
			PreparedStatement sql = connection.prepareStatement(args);
			sql.executeUpdate();
			success = true;
		}catch(SQLException e) {
			Messages.getInstance().toConsole("Fehler beim Ausführend des Statements: "+args);
		}
		
		closeConnection();
		
		return success;
	}
}
