package fex.signs.util;

import java.sql.Date;
import java.util.Calendar;

import org.bukkit.Location;
import org.bukkit.Material;

public class Util {
	public static boolean isSign(Location e) {
		if (e.getBlock().getType() == Material.WALL_SIGN || e.getBlock().getType() == Material.SIGN)
			return true;
		else
			return false;
	}
	
	public static Date now() {
		Calendar c = Calendar.getInstance();
		return new Date(c.getTimeInMillis());
	}

}
