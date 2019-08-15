package fex.signs.util;

import java.sql.Date;
import java.util.Calendar;
//import java.util.HashSet;
//import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class Util {
	public static boolean isSign(Material m){
		return m.toString().endsWith("_SIGN");
//		Set<Material> signTypes = new HashSet<>();
//		signTypes.add(Material.ACACIA_SIGN);
//		signTypes.add(Material.ACACIA_WALL_SIGN);
//		signTypes.add(Material.SPRUCE_SIGN);
//		signTypes.add(Material.SPRUCE_WALL_SIGN);
//		signTypes.add(Material.OAK_SIGN);
//		signTypes.add(Material.OAK_WALL_SIGN);
//		signTypes.add(Material.DARK_OAK_SIGN);
//		signTypes.add(Material.DARK_OAK_WALL_SIGN);
//		signTypes.add(Material.JUNGLE_SIGN);
//		signTypes.add(Material.JUNGLE_WALL_SIGN);
//		signTypes.add(Material.BIRCH_SIGN);
//		signTypes.add(Material.BIRCH_WALL_SIGN);
//		return signTypes.contains(m);
	}
	public static boolean isStandingSign(Material m){
		return (m.toString().endsWith("_SIGN") && !m.toString().contains("WALL"));
//		Set<Material> signTypes = new HashSet<>();
//		signTypes.add(Material.ACACIA_SIGN);
//		signTypes.add(Material.SPRUCE_SIGN);
//		signTypes.add(Material.OAK_SIGN);
//		signTypes.add(Material.DARK_OAK_SIGN);
//		signTypes.add(Material.JUNGLE_SIGN);
//		signTypes.add(Material.BIRCH_SIGN);
//		return signTypes.contains(m);
	}
	public static boolean isWallSign(Material m){
		return m.toString().endsWith("_WALL_SIGN");
//		Set<Material> signTypes = new HashSet<>();
//		signTypes.add(Material.ACACIA_WALL_SIGN);
//		signTypes.add(Material.SPRUCE_WALL_SIGN);
//		signTypes.add(Material.OAK_WALL_SIGN);
//		signTypes.add(Material.DARK_OAK_WALL_SIGN);
//		signTypes.add(Material.JUNGLE_WALL_SIGN);
//		signTypes.add(Material.BIRCH_WALL_SIGN);
//		return signTypes.contains(m);
	}
	public static boolean isSign(Location e) {
		return isSign(e.getBlock().getType());
	}

	public static Date now() {
		Calendar c = Calendar.getInstance();
		return new Date(c.getTimeInMillis());
	}

	public static String UUIDtoPlayer(String UUID_) {
		return Bukkit.getOfflinePlayer(UUID.fromString(UUID_)).getName();
	}
}
