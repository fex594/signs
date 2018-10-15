package fex.signs.util;

import java.sql.Date;

public class PlayerSign {
	private int ID;
	private String besitzerUUID;
	private java.sql.Date ablaufDatum;
	private int active;
	private String location;
	private String type;
	private String erstellerUUID;
	private java.sql.Date maxExpandDate;
	
	public PlayerSign(int ID, String besitzerUUID, Date ablaufDatum, int active, String location, String type,
			String erstellerUUID, Date maxExpandDate) {
		this.ID = ID;
		this.besitzerUUID = besitzerUUID;
		this.ablaufDatum = ablaufDatum;
		this.active = active;
		this.location = location;
		this.type = type;
		this.erstellerUUID = erstellerUUID;
		this.maxExpandDate = maxExpandDate;
	}

	public int getID() {
		return ID;
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
	};
	
	
}
