package fex.signs.util;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class PlayerSign {
	private int ID;
	private String besitzerUUID;
	private java.sql.Date ablaufDatum;
	private int active;
	private String location;
	private String type;
	private String erstellerUUID;
	private java.sql.Date maxExpandDate;
	private String text;

	public PlayerSign(int ID, String besitzerUUID, Date ablaufDatum, int active, String location, String type,
			String erstellerUUID, Date maxExpandDate, String text) {
		this.ID = ID;
		this.besitzerUUID = besitzerUUID;
		this.ablaufDatum = ablaufDatum;
		this.active = active;
		this.location = location;
		this.type = type;
		this.erstellerUUID = erstellerUUID;
		this.maxExpandDate = maxExpandDate;
		this.text = text;
	}

	public int getID() {
		return ID;
	}

	public void setAblaufDatum(Date d) {
		this.ablaufDatum = d;
	}

	public String getBesitzerUUID() {
		return besitzerUUID;
	}

	public java.sql.Date getAblaufDatum() {
		return ablaufDatum;
	}

	public int getActive() {
		return active;
	}

	public String getLocation() {
		return location;
	}

	public String getType() {
		return type;
	}

	public String getErstellerUUID() {
		return erstellerUUID;
	}

	public java.sql.Date getMaxExpandDate() {
		return maxExpandDate;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		DateFormat df = new SimpleDateFormat("dd.MM.YYYY");
		String date = df.format(getAblaufDatum()).toString();
		String s = "§7[" + type;
		if (active == 0) {
			s += "§c";
		} else if (active == 1) {
			s += "§2";
		}
		s += " #" + ID + "§7 ]§6 Besitzer: §7" + Util.UUIDtoPlayer(besitzerUUID) + " §6Ersteller: §7"
				+ Util.UUIDtoPlayer(erstellerUUID) + "\n" + "§6Ablaufdatum: §7" + date + " §6Ort: §7" + location;
		if (text != null)
			s += "\n§3{" + text + "}";
		return s;
	}

	public String toUserString() {
		return "§7[" + type + "] §6Eigentümer: §7" + Util.UUIDtoPlayer(besitzerUUID);
	}

}
