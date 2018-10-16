package fex.signs.signs;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bukkit.entity.Player;

import fex.signs.util.Messages;
import fex.signs.util.PlayerSign;
import fex.signs.util.Util;

public class CommandTransformer {

	private List<PlayerSign> active;
	private List<PlayerSign> abgelaufen;
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
	 * @param name
	 *            UUID des Spielers
	 * @return Liste mit allen abgelaufenen Schildern (eines Spielers)
	 */
	public List<PlayerSign> getAbgelaufen(String name) {
		if (name == null) {
			return abgelaufen;
		} else {
			List<PlayerSign> pAbgelaufen = new ArrayList<PlayerSign>();
			for (PlayerSign ps : abgelaufen) {
				if (ps.getBesitzerUUID() == name) {
					pAbgelaufen.add(ps);
				}
			}
			return pAbgelaufen;
		}
	}

	/**
	 * Infos über aktives Schild, Kennung über ID
	 * 
	 * @param ID
	 *            ID
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
				ps = SQLHandler.getInstance().sendResultStatements("SELECT * FROM SCHILDER WHERE ID = " + ID).get(0);
			}
			mess.toPlayer(p, ps.toString(), Messages.NORMAL);
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
		return SQLHandler.getInstance().sendStatement(args);

	}

	public void update() {
		try {
			SQLHandler handle = SQLHandler.getInstance();
			this.active = handle.update(); // Update aktive Schilder
			// System.out.println(active.size());
			this.abgelaufen = new ArrayList<PlayerSign>();
			for (PlayerSign ps : active) // Update abgelaufene Schilder
				if (ps.getAblaufDatum().before(Util.now())) {
					this.abgelaufen.add(ps);
				}

			this.maxID = handle.getMaxID(); // Update MaxID

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public boolean commentSign(int ID, String text) {
		String output = "UPDATE Schilder SET Text = '" + text + "' WHERE ID = " + ID;
		return SQLHandler.getInstance().sendStatement(output);
	}

	public java.sql.Date getDate(int ID) {
		return getSignOutOfList(ID).getAblaufDatum();
	}

	public String setDate(int ID, int days, boolean override) {
		PlayerSign p = getSignOutOfList(ID);
		java.sql.Date d = new java.sql.Date(CommandTransformer.getInstance().getDate(ID).getTime());
		Calendar c = Calendar.getInstance();
		c.setTime(p.getAblaufDatum());
		c.add(Calendar.DAY_OF_MONTH, days);
		d = new java.sql.Date(c.getTimeInMillis());

		boolean finished = false;
		if (d.before(p.getMaxExpandDate()) || override) {
			p.setAblaufDatum(d);
			finished = true;

		} else {
			p.setAblaufDatum(p.getMaxExpandDate());
		}
		String args = "UPDATE Schilder SET Datum = '" + p.getAblaufDatum() + "' WHERE ID=" + ID;
		SQLHandler.getInstance().sendStatement(args);
		if (finished) {
			return "Schild erfolgreich um " + days + " Tage verlängert";
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

	public PlayerSign getSignOutOfList(int ID) {
		PlayerSign p = null;
		for (PlayerSign ps : active) {
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
		String st = "INSERT INTO Schilder (Player, Active, Loc, Datum, Typ, Ersteller, Lastdate) VALUES ('" + name
				+ "', " + active + ", '" + loc + "', '" + date + "', '" + type + "', '" + ersteller + "', '" + lastDate
				+ "')";
		SQLHandler.getInstance().sendStatement(st);
		return ++maxID;
	}

	/**
	 * Not yet implementet
	 * 
	 * @param s
	 * @return
	 */
	public List<String> deleteAllSigns(String s) {
		return null;
	}
}
