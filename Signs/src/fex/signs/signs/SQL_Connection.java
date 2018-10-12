package fex.signs.signs;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fex.signs.util.ConnectionInfo;

public final class SQL_Connection {
	private Connection connection;

	public SQL_Connection(ConnectionInfo ci) {
		openConnection(ci);
		setupTable();
	}


	/**
	 * Herstellen einer Verbindung mit der Datenbank
	 * 
	 * @param ci
	 *            Verbindungsinformationen
	 */
	public void openConnection(ConnectionInfo ci) {
		try {
			connection = DriverManager.getConnection(
					"jdbc:mysql://" + ci.getHost() + ":" + ci.getPort() + "/" + ci.getDbName()+"?useSSL=false", ci.getUsername(),
					ci.getPw());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Einrichten der Datenbank, falls nicht vorhanden
	 */
	public void setupTable() {
		try {
		String string = "CREATE TABLE IF NOT EXISTS Schilder (ID int AUTO_INCREMENT NOT NULL PRIMARY KEY, Player VARCHAR(36) NOT NULL, Active int(1) NOT NULL, Text VARCHAR(255), Loc VARCHAR(60) NOT NULL, Datum Date NOT NULL, Typ VARCHAR(25) NOT NULL, Ersteller VARCHAR(36) NOT NULL, Lastdate DATE NOT NULL)";
		PreparedStatement sql = connection.prepareStatement(string);
		sql.executeUpdate();

	} catch (SQLException e) {
		e.printStackTrace();
	}
	}

	/**
	 * Liefert Informationen zu einem bestimmten Schild
	 * 
	 * @param ID
	 *            Schildnummer
	 * @param name
	 *            Spieler
	 * @return String
	 */
	public String getInfos(int ID, Player name) {
		try {

			Statement sql = connection.createStatement();
			ResultSet result = sql.executeQuery(
					"SELECT Player, ID, Active, Text, Datum, Ersteller, Typ FROM Schilder WHERE ID=" + ID);
			String out = "";
			while (result.next()) {
				String player = result.getString("Player");
				if (player.equals(name.getUniqueId().toString()) || name.hasPermission("signs.support")) {
					int act = result.getInt("Active");
					String active = "";
					if (act == 1) {
						DateFormat df = new SimpleDateFormat("dd/MM/YYYY");
						String date = df.format(result.getDate("Datum")).toString();
						active = "läuft am §2" + date + "§6 ab";
					} else {
						active = "ist §cinaktiv";
					}
					String pName = Bukkit.getOfflinePlayer(UUID.fromString(result.getString("Player"))).getName();
					String erName = Bukkit.getOfflinePlayer(UUID.fromString(result.getString("Ersteller"))).getName();
					out = "§6Ein §2" + result.getString("Typ") + "§6-Schild für §2" + pName + "§6 mit der ID §2"
							+ result.getString("ID") + "§6 " + active + "§6. Ersteller: §2" + erName;
					if (!(result.getString("Text") == null)) {
						out = out + " §6(Grund: §c" + result.getString("Text") + "§6)";
					}
				} else {
					String pName = Bukkit.getOfflinePlayer(UUID.fromString(result.getString("Player"))).getName();
					out = "Typ: §2" + result.getString("Typ") + "§6, Spieler: " + pName;
				}

			}
			if (out.isEmpty()) {
				out = "Kein Schild gefunden";
			}
			return out;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gibt die Anzahl offener Schilder einses Spielers zurück
	 * 
	 * @param player
	 *            Spielername
	 * @return Anzahl Schilder des Spielers
	 */
	public int getActiveSigns(String player) {
		String sts = "SELECT count(id) AS Anzahl FROM Schilder WHERE Active = 1 AND Player = '" + player + "';";
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			int count = 0;
			// int count = set.getInt("Anzahl");
			while (set.next()) {
				count = set.getInt("Anzahl");
			}
			return count;
		} catch (SQLException e) {
			return -1;
		}
	}

	/**
	 * Gets the number of all active Signs registered to the Server
	 * 
	 * @return number of active signs
	 */
	public int getActiveSigns() {
		String sts = "SELECT count(id) AS Anzahl FROM Schilder WHERE Active = 1;";
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			int count = 0;
			// int count = set.getInt("Anzahl");
			while (set.next()) {
				count = set.getInt("Anzahl");
			}
			return count;
		} catch (SQLException e) {
			return -1;
		}
	}

	public int getActiveAbgelaufeneSigns() {
		Date d = new Date(Calendar.getInstance().getTimeInMillis());
		String sts = "SELECT count(id) AS Anzahl FROM Schilder WHERE Active = 1 AND Datum <= '" + d.toString() + "'";
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			int count = 0;
			// int count = set.getInt("Anzahl");
			while (set.next()) {
				count = set.getInt("Anzahl");
			}
			return count;
		} catch (SQLException e) {
			return -1;
		}
	}

	public int getActiveAbgelaufeneSigns(String name) {
		Date d = new Date(Calendar.getInstance().getTimeInMillis());
		String sts = "SELECT count(id) AS Anzahl FROM Schilder WHERE Active = 1 AND Datum <= '" + d.toString()
				+ "' AND Player = '" + name + "'";
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			int count = 0;
			// int count = set.getInt("Anzahl");
			while (set.next()) {
				count = set.getInt("Anzahl");
			}
			return count;
		} catch (SQLException e) {
			return -1;
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
	public int putNewSign(String name, Date date, int active, String loc, String typ, String ersteller,
			Date lastDate) {
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

	public Date getDate(int id) {
		Date date = null;
		try {
			String st = "SELECT Datum FROM Schilder WHERE ID = " + id;
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(st);
			while (set.next()) {
				date = set.getDate("Datum");
			}
		} catch (Exception e) {
		}
		return date;
	}

	public String getLocation(int id) {
		String loc = "";
		try {
			String st = "SELECT Loc FROM Schilder WHERE ID = " + id;
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(st);
			while (set.next()) {
				loc = set.getString("Loc");
			}
		} catch (Exception e) {
		}
		System.out.println("Location received: " + loc);
		return loc;
	}

	public boolean setDate(int id, Date date, boolean isAdminCommand) {
		boolean ok = true;
		Date last = null;
		try {
			String st = "SELECT Lastdate FROM Schilder WHERE ID = " + id;
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(st);
			while (set.next()) {
				last = set.getDate("Lastdate");
			}
		} catch (Exception e) {
		}
		if (date.after(last) && !isAdminCommand) {
			date = last;
			ok = false;
		}
		try {
			String s = "UPDATE Schilder SET Datum = '" + date + "' WHERE ID=" + id;
			PreparedStatement sql = connection.prepareStatement(s);
			sql.executeUpdate();
		} catch (SQLException e) {
		}
		return ok;
	}

	/**
	 * Setzt ein Schild inaktiv
	 * 
	 * @param id
	 *            Schildnummer
	 */
	public void setInaktivSign(int id) {
		String s = "UPDATE Schilder SET Active=0 WHERE ID=" + id;
		try {
			PreparedStatement sql = connection.prepareStatement(s);
			sql.executeUpdate();
		} catch (SQLException e) {
		}
	}

	public boolean isInArea(int id) {
		String sts = "SELECT MAX(ID) AS Maximum FROM Schilder WHERE ID = " + id;
		int state = -1;
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			while (set.next()) {
				state = set.getInt("Maximum");
			}
		} catch (SQLException e) {
		}
		if (id <= state)
			return true;
		return false;
	}

	public boolean isActive(int id) {
		String sts = "SELECT Active FROM Schilder WHERE ID = " + id;
		int state = -1;
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			while (set.next()) {
				state = set.getInt("Active");
			}
		} catch (SQLException e) {
		}
		if (state == 1)
			return true;
		return false;
	}

	/**
	 * Löscht ein Schild aus der Datenbank Nicht empfehlenswert!
	 * 
	 * @param id
	 *            Schildnummer
	 */
	public void deleteSign(int id) {
		try {
			String st = "DELETE FROM Schilder WHERE ID=" + id;
			PreparedStatement sql = connection.prepareStatement(st);
			sql.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Schließt die Verbindung zur Datenbank
	 */
	public void disable() {
		try {
			if (connection != null && connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Liefert alle offenen Schilder zurück
	 * 
	 * @return ArrayList mit offenen Schildern
	 */
	public ArrayList<String> getActive() {
		String sts = "SELECT ID, Player, Loc, Typ FROM Schilder WHERE Active = 1";
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			ArrayList<String> list = new ArrayList<String>();
			while (set.next()) {
				String pName = Bukkit.getOfflinePlayer(UUID.fromString(set.getString("Player"))).getName();
				list.add("Ein Schild für §2" + pName + "§6 mit der ID §2" + set.getString("ID") + "§6, Position: §2"
						+ set.getString("Loc") + "§6, Typ: §2" + set.getString("Typ"));
			}
			return list;
		} catch (SQLException e) {
			return null;
		}
	}

	public ArrayList<String> getActive(String name) {
		String sts = "SELECT ID, Loc, Typ FROM Schilder WHERE Active = 1 AND Player = '" + name + "'";
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			ArrayList<String> list = new ArrayList<String>();
			while (set.next()) {
				list.add("ID: §2" + set.getString("ID") + "§6: Position: §2" + set.getString("Loc") + "§6, Typ: §2"
						+ set.getString("Typ"));
			}
			return list;
		} catch (SQLException e) {
			return null;
		}
	}

	public ArrayList<String> getAbgelaufen() {
		Date d = new Date(Calendar.getInstance().getTimeInMillis());
		String sts = "SELECT ID, Player, Loc, Datum, Typ  FROM Schilder WHERE Active = 1 AND Datum <= '" + d.toString()
				+ "'";
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			ArrayList<String> list = new ArrayList<String>();
			while (set.next()) {
				list.add("Schild-ID: §2" + set.getString("ID") + "§6, Position: §2" + set.getString("Loc")
						+ ", §6seit: §2" + set.getString("Datum") + "§6, Typ: §2" + set.getString("Typ"));
			}
			return list;
		} catch (SQLException e) {
			return null;
		}
	}

	public ArrayList<String> getAbgelaufen(String name) {
		Date d = new Date(Calendar.getInstance().getTimeInMillis());
		String sts = "SELECT ID, Loc, Datum, Typ  FROM Schilder WHERE Active = 1 AND Datum <= '" + d.toString()
				+ "' AND Player = '" + name + "'";
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			ArrayList<String> list = new ArrayList<String>();
			while (set.next()) {
				list.add("Schild-ID: §2" + set.getString("ID") + "§6, Position: §2" + set.getString("Loc")
						+ ", §6seit: §2" + set.getString("Datum") + "§6, Typ: §2" + set.getString("Typ"));
			}
			return list;
		} catch (SQLException e) {
			return null;
		}
	}

	/**
	 * Verändert die Beschreibung eines Schildes
	 * 
	 * @param id
	 *            Schildnummer
	 * @param text
	 *            Neue Beschreibung
	 * @return Erfolg des Vorgangs
	 */
	public boolean commentSign(int id, String text) {
		String s = "UPDATE Schilder SET Text = '" + text + "' WHERE ID=" + id;
		try {
			PreparedStatement sql = connection.prepareStatement(s);
			sql.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public ArrayList<String> deleteAllActiveSigns(String name) {
		String sts = "SELECT ID, Loc  FROM Schilder WHERE Active = 1 AND Player = '" + name + "'";
		ArrayList<String> outputLocations = new ArrayList<String>();
		try {
			Statement stm = connection.createStatement();
			ResultSet set = stm.executeQuery(sts);
			ArrayList<Integer> list = new ArrayList<Integer>();
			while (set.next()) {
				list.add(set.getInt("ID"));
				outputLocations.add(set.getString("Loc"));
			}
			for(int i = 0; i < list.size(); i++) {
				setInaktivSign(list.get(i));
			}
		} catch (SQLException e) {
		}
		return outputLocations;
	}	
}
