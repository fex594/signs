package fex.signs.signs;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import fex.signs.util.Messages;
import fex.signs.util.NoSignFoundException;
import fex.signs.util.PlayerSign;
import fex.signs.util.Util;

public class CommandTransformer {

	private List<PlayerSign> active;
	private List<PlayerSign> abgelaufen;
	private List<PlayerSign> inactive;
	private int maxID;
	private Messages mess = Messages.getInstance();
	private static CommandTransformer instance;

	private CommandTransformer() {
		update();
	}

	public static CommandTransformer getInstance() {
		if (instance == null) {
			instance = new CommandTransformer();
		}
		return CommandTransformer.instance;
	}

	public List<PlayerSign> getActive(String name) {
		if (name == null) {
			return active;
		} else {
			List<PlayerSign> pActive = new ArrayList<PlayerSign>();
			for (PlayerSign ps : active) {
				if (ps.getBesitzerUUID().equals(name)) {
					pActive.add(ps);
				}
			}
			return pActive;
		}
	}

	/**
	 * Wenn Name null: Alle Schilder, sonst nur von Name
	 * 
	 * @param name UUID des Spielers
	 * @return Liste mit allen abgelaufenen Schildern (eines Spielers)
	 */
	public List<PlayerSign> getAbgelaufen(String name) {
		if (name == null) {
			return abgelaufen;
		} else {
			List<PlayerSign> pAbgelaufen = new ArrayList<PlayerSign>();
			for (PlayerSign ps : abgelaufen) {
				if (ps.getBesitzerUUID().equals(name)) {
					pAbgelaufen.add(ps);
				}
			}
			return pAbgelaufen;
		}
	}

	public List<PlayerSign> getInactive(String name) {
		if (name == null) {
			return inactive;
		} else {
			List<PlayerSign> pInactive = new ArrayList<PlayerSign>();
			for (PlayerSign ps : inactive) {
				if (ps.getBesitzerUUID().equals(name)) {
					pInactive.add(ps);
				}
			}
			return pInactive;
		}
	}

	/**
	 * Infos über aktives Schild, Kennung über ID
	 * 
	 * @param ID ID
	 */
	public void getInfo(int ID, Player p) {
		if (ID > maxID) {
			mess.toPlayer(p, "Kein Schild mit der Nummer " + ID + " vorhanden!", Messages.IMPORTANT);
		} else {
			PlayerSign ps = null;
			for (PlayerSign pa : active) {
				if (pa.getID() == ID) {
					ps = pa;
					break;
				}
			}
			if (ps == null) {
				ps = SQLHandler.getInstance().sendResultStatements("SELECT * FROM Schilder WHERE ID = " + ID).get(0);
			}
			if (p.hasPermission("signs.support") || p.getUniqueId().toString().equals(ps.getBesitzerUUID())) {
				mess.toPlayer(p, ps.toString(), Messages.NORMAL);
			} else {
				mess.toPlayer(p, ps.toUserString(), Messages.NORMAL);
			}
		}
	}

	public boolean isActive(int ID) {
		for (PlayerSign ps : active) {
			if (ps.getID() == ID) {
				return true;
			}
		}
		return false;
	}

	public boolean setInaktiv(int ID) {
		String args = "UPDATE Schilder SET Active=0 WHERE ID=" + ID;
		boolean result = SQLHandler.getInstance().sendStatement(args);
		update();

		return result;

	}

	public void update() {
		Bukkit.getScheduler().runTaskAsynchronously(Main.getProvidingPlugin(getClass()), new Runnable() {

			@Override
			public void run() {
				// for(Player p : Bukkit.getServer().getOnlinePlayers()) {
				// p.sendMessage("Update Gestartet");
				// }
				// System.out.println("Update gestartet");
				try {
					SQLHandler handle = SQLHandler.getInstance();
					active = handle.update(); // Update aktive Schilder
					abgelaufen = new ArrayList<PlayerSign>();
					for (PlayerSign ps : active) // Update abgelaufene Schilder
						if (ps.getAblaufDatum().before(Util.now())) {
							abgelaufen.add(ps);
						}

					maxID = handle.getMaxID(); // Update MaxID
					if (maxID == 0)
						maxID = 1;
					inactive = handle.getInactiveSigns();

				} catch (SQLException e) {
					e.printStackTrace();
				}
				// for(Player p : Bukkit.getServer().getOnlinePlayers()) {
				// p.sendMessage("Update Beendet");
				// }
				// System.out.println("Update beendet");
			}

		});

	}

	public boolean commentSign(int ID, String text) {

		String output = "UPDATE Schilder SET Text = '" + text + "' WHERE ID = " + ID;
		boolean x = SQLHandler.getInstance().sendStatement(output);
		update();
		return x;
	}

	public java.sql.Date getDate(int ID) throws NoSignFoundException {
		java.sql.Date date = getSignOutOfList(ID, active).getAblaufDatum();
		if (date != null)
			return date;
		else {
			throw new NoSignFoundException("Schild ist nicht aktiv");
		}
	}

	public String setDate(int ID, int days, boolean override) {
		PlayerSign p = getSignOutOfList(ID, active);
		if (p == null) {
			return ChatColor.RED + "Schild ist nicht mehr aktiv";
		}
		// java.sql.Date d = new
		// java.sql.Date(CommandTransformer.getInstance().getDate(ID).getTime());
		Calendar c = Calendar.getInstance();
		c.setTime(p.getAblaufDatum());
		c.add(Calendar.DAY_OF_MONTH, days);
		java.sql.Date d = new java.sql.Date(c.getTimeInMillis());

		int finished = 0;
		if (d.before(p.getMaxExpandDate()) || override) {
			p.setAblaufDatum(d);
			finished = 2;

		} else if (d.after(p.getMaxExpandDate()) && p.getAblaufDatum().before(p.getMaxExpandDate())) {
			finished = 1;
			p.setAblaufDatum(p.getMaxExpandDate());
		} else {
			p.setAblaufDatum(p.getMaxExpandDate());
		}
		String args = "UPDATE Schilder SET Datum = '" + p.getAblaufDatum() + "' WHERE ID=" + ID;
		SQLHandler.getInstance().sendStatement(args);
		update();
		if (finished == 2) {
			return "Schild erfolgreich um " + days + " Tage verlängert";
		} else if (finished == 1) {
			return "Schild bis maximale Zeit verlängert";
		} else {
			return "Maximale Verlängerungszeit erreicht!";
		}
	}

	public int getMaxID() {
		return this.maxID;
	}

	public String getLocation(int ID) {
		String s = null;
		for (PlayerSign ps : active) {
			if (ps.getID() == ID) {
				s = ps.getLocation();
				break;
			}
		}
		return s;
	}

	public String getLocationInaktive(int ID) {
		String s = null;
		for (PlayerSign ps : inactive) {
			if (ps.getID() == ID) {
				s = ps.getLocation();
				break;
			}
		}
		return s;
	}

	public PlayerSign getSignOutOfList(int ID, List<PlayerSign> list) {
		PlayerSign p = null;
		for (PlayerSign ps : list) {
			if (ps.getID() == ID) {
				p = ps;
				break;
			}
		}
		return p;
	}

	/**
	 * Legt eine neues Schild in der Datenbank an
	 * 
	 * @param name   Spielername
	 * @param date   Ablaufdatum
	 * @param active Aktivität: 1 - Aktiv, 0 - Inaktiv
	 * @param loc    Schildposition
	 * @param text   Beschreibung zum Schild
	 * @param typ    Schild-Typ
	 * @return ID des Schilds
	 */
	public int putNewSign(String name, Date date, int active, String loc, String typ, String ersteller, Date lastDate) {
		String type = typ.replace("[", "").replace("]", "");
		// String st = "INSERT INTO Schilder (ID, Player, Active, Loc, Datum, Typ,
		// Ersteller, Lastdate) VALUES (" + maxID
		// + ", " + "'" + name + "', " + active + ", '" + loc + "', '" + date + "', '" +
		// type + "', '" + ersteller
		// + "', '" + lastDate + "')";
		String st = "INSERT INTO Schilder (Player, Active, Loc, Datum, Typ, Ersteller, Lastdate) VALUES ('" + name
				+ "', " + active + ", '" + loc + "', '" + date + "', '" + type + "', '" + ersteller + "', '" + lastDate
				+ "')";
		SQLHandler.getInstance().sendStatement(st);
		update();
		return maxID;
	}

	/**
	 * Not yet implementet
	 * 
	 * @param s
	 * @return
	 */
	public List<String> deleteAllSigns(String s) {
		List<String> list = new ArrayList<String>();
		List<PlayerSign> delete = new ArrayList<PlayerSign>();
		for (PlayerSign ps : active) {
			if (ps.getBesitzerUUID().equals(s)) {
				list.add(ps.getLocation());
				delete.add(ps);
			}
		}
		for (PlayerSign ps : delete) {
			if (ps.getBesitzerUUID().equals(s)) {
				active.remove(ps);
				abgelaufen.remove(ps);
			}
		}
		SQLHandler.getInstance().sendStatement("UPDATE Schilder SET Active = 0 WHERE Player = '" + s + "'");
		return list;
	}

	/**
	 * Returns a List with active Signs of UserUUID user
	 * 
	 * @param user UUID of called User
	 * @return ResultList, can be null
	 */
	public List<PlayerSign> getUserActive(String user) {
		List<PlayerSign> result = new ArrayList<PlayerSign>();
		for (PlayerSign ps : active) {
			if (ps.getBesitzerUUID().equals(user)) {
				result.add(ps);
			}
		}
		return result;
	}

	public List<PlayerSign> getCorruptedSigns() {
		List<PlayerSign> result = new ArrayList<>();
		for (PlayerSign ps : active) {
			if (!checkSignLocation(ps))
				result.add(ps);
		}
		return result;
	}

	/**
	 * Returns true if Block at Sign's position is a sign or a wallsign
	 * 
	 * @param sign
	 * @return
	 */
	private boolean checkSignLocation(PlayerSign sign) {
		String location = sign.getLocation();
		location = location.replace("(", "").replace(")", "");
		String world = location.substring(0, location.indexOf(":")).replace(":", "");
		double erster = Integer.parseInt(
				location.substring(location.indexOf(":"), location.indexOf("/")).replace(":", "").replace("/", ""))
				+ 0.5;
		double zweiter = Integer
				.parseInt(location.substring(location.indexOf("/"), location.lastIndexOf("/")).replace("/", ""));
		double dritter = Integer.parseInt(location.substring(location.lastIndexOf("/")).replace("/", "")) + 0.5;
		Location l = new Location(Bukkit.getServer().getWorld(world), erster, zweiter, dritter);
		return Util.isSign(l);
	}
}
