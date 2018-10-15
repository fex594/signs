package fex.signs.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PSConverter {
	public static PlayerSign convert(ResultSet r) {
		try {
		int ID = r.getInt("ID");
		String besitzerUUID = r.getString("Player");
		java.sql.Date ablaufDatum = r.getDate("Datum");
		int active = r.getInt("Active");
		String location = r.getString("Loc");
		String type = r.getString("Typ");
		String erstellerUUID = r.getString("Ersteller");
		java.sql.Date maxExpandDate = r.getDate("Lastdate");
		String text = r.getString("Text");	
		
		PlayerSign ps = new PlayerSign(ID, besitzerUUID, ablaufDatum, active, location, type, erstellerUUID, maxExpandDate, text);
		return ps;
		
		}catch(SQLException e) {
			System.out.println("Fehler beim Konvertieren des Results in Schilddaten");
		}
		return null;
	}
}
