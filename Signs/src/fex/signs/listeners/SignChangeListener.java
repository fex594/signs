package fex.signs.listeners;

import java.sql.Date;
import java.util.Calendar;
//import java.util.HashSet;
import java.util.List;
//import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
//import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.util.Vector;

import fex.signs.signs.CommandTransformer;
//import fex.signs.signs.SQL_Connection;
import fex.signs.util.Messages;

public class SignChangeListener extends MyListener implements Listener {

	private final int MAX_EXPAND_DAYS = 31;

//	public SignChangeListener(SQL_Connection connection) {
//		super(connection);
//	}

	//Überarbeiten!!!
	/**
	 * Triggered beim Erstellen des Schildes, wenn Text gesendet wird
	 * @param e
	 */
	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if (e.getPlayer().hasPermission("signs.support")) {
			Location l = e.getBlock().getLocation();

			String localtype = e.getLine(0);
			localtype = localtype.toLowerCase();
			if (localtype.isEmpty() || localtype.length() <= 3) {

			} else {
				localtype = localtype.substring(0, 2).toUpperCase() + localtype.substring(2);
				String lastLong = e.getLine(3);
				if (localtype.equalsIgnoreCase("[Bauregeln]") || localtype.equalsIgnoreCase("[Verschönern]")
						|| localtype.equalsIgnoreCase("[Weiterbauen]") || localtype.equalsIgnoreCase("[Abriss]")) { // Test:
					// Bauregelschild?
					if (!canBePlaced(e.getBlock(), e.getPlayer())) {
						cancelChangeEvent(e, "Unerlaubter Untergrund");
					} else {
						String name = e.getLine(1) + e.getLine(2); // Spielername
						if (name.isEmpty()) {
							cancelChangeEvent(e, "Du musst einen Namen eingeben");
							return;
						}
						@SuppressWarnings("deprecation")
						OfflinePlayer p2 = Bukkit.getServer().getOfflinePlayer(name);
						if (p2.getLastPlayed() != 0 || p2.isOnline()) { // Test: Spieler richtig?
							e.setLine(2, "§6" + name);
							try {
								Player p = (Player) p2;
								mess.toPlayer(p, "Du hast ein neues Schild erhalten, Koordinaten: §6(" + l.getBlockX()
										+ "/" + l.getBlockY() + "/" + l.getBlockZ() + ")", Messages.IMPORTANT);
							} catch (Exception ex) {
							}

							// Datum = Ablaufdatum
							int intLong = 14;	//Standardzeit bis Schild abgelaufen
							if (!lastLong.isEmpty()) {
								try {
									intLong = Integer.parseInt(lastLong);
									if (intLong > MAX_EXPAND_DAYS) {
										intLong = MAX_EXPAND_DAYS;
										mess.toPlayer(e.getPlayer(), "Maximal " + MAX_EXPAND_DAYS + " Tage erlaubt",
												Messages.IMPORTANT);
									}
								} catch (NumberFormatException ex) {
								}
							}
							Calendar c = Calendar.getInstance();
							c.add(Calendar.DAY_OF_MONTH, intLong);
							Date date = new Date(c.getTimeInMillis());				//Ablaufdatum
							c = Calendar.getInstance();
							c.add(Calendar.DAY_OF_MONTH, MAX_EXPAND_DAYS);
							Date lastDate = new Date(c.getTimeInMillis());			//Spätestes Ablaufdatum
							String loc = "(" + e.getBlock().getLocation().getWorld().getName() + ":"
									+ e.getBlock().getLocation().getBlockX() + "/"
									+ e.getBlock().getLocation().getBlockY() + "/"
									+ e.getBlock().getLocation().getBlockZ() + ")";
							e.setLine(0, "§4" + localtype);
							e.setLine(1, "§2#" + CommandTransformer.getInstance().putNewSign(p2.getUniqueId().toString(), date, 1, loc, localtype,
									e.getPlayer().getUniqueId().toString(), lastDate));
							e.setLine(2, "§6" + p2.getName());
							e.setLine(3, "§[Klick mich]");
						} else {
							mess.toPlayer(e.getPlayer(), "Spieler " + name + " war noch nie auf dem Server",
									Messages.IMPORTANT);
							// Entfernt falsches Schild wieder
							l.getBlock().setType(Material.AIR);
						}
					}
				}
			}
		}

	}

	public boolean canBePlaced(Block b, Player p) {
		if (b.getType() == Material.SIGN) {
			Block down = b.getLocation().add(0, -1, 0).getBlock();
			return isType(down.getType());
		} else if (b.getType() == Material.WALL_SIGN) {

		
			/**
			 * Tempor�re L�sung, Achtung erkennt unerlaubten Untergrund nicht, wenn man direkt im Schildblock steht
			 */
//			Set<Material> blockedList = new HashSet<Material>();
//			blockedList.add(Material.SIGN);
//			blockedList.add(Material.WALL_SIGN);
//			blockedList.add(Material.AIR);
			List<Block> targets = p.getLastTwoTargetBlocks(null, 10);
			Block target = targets.get(0);
//			Block target = p.getTargetBlock(null, 20);
			BlockFace blockFace = b.getFace(target);
			
			/**
			 * Fehler! CraftSign --> WallSign
			 */
//			 WallSign s = (WallSign) b.getState();
//			 BlockFace blockFace = s.getFacing();// s.getAttachedFace();
			
			Vector v = null;
			if (blockFace == BlockFace.NORTH) {
				v = new Vector(0, 0, 1);
			} else if (blockFace == BlockFace.EAST) {
				v = new Vector(-1, 0, 0);
			} else if (blockFace == BlockFace.SOUTH) {
				v = new Vector(0, 0, -1);
			} else if (blockFace == BlockFace.WEST) {
				v = new Vector(1, 0, 0);
			} else {
				v = new Vector(0, 0, 0);
			}

			Block face = b.getLocation().add(v).getBlock();
			return isType(face.getType());

		} else
			return true;

	}

	public boolean isType(Material m) {
		if (m == Material.SAND || m == Material.GRAVEL || m == Material.ANVIL || m == Material.ICE
				|| m == Material.FROSTED_ICE || m == Material.BLACK_CONCRETE_POWDER
				|| m == Material.BLUE_CONCRETE_POWDER || m == Material.BROWN_CONCRETE_POWDER
				|| m == Material.CYAN_CONCRETE_POWDER || m == Material.GRAY_CONCRETE_POWDER
				|| m == Material.GREEN_CONCRETE_POWDER || m == Material.LIGHT_BLUE_CONCRETE_POWDER
				|| m == Material.LIGHT_GRAY_CONCRETE_POWDER || m == Material.LIME_CONCRETE_POWDER
				|| m == Material.MAGENTA_CONCRETE_POWDER || m == Material.ORANGE_CONCRETE_POWDER
				|| m == Material.PINK_CONCRETE_POWDER || m == Material.PURPLE_CONCRETE_POWDER
				|| m == Material.RED_CONCRETE_POWDER || m == Material.WHITE_CONCRETE_POWDER
				|| m == Material.YELLOW_CONCRETE_POWDER || m == Material.AIR || m == Material.BLUE_ICE)
			return false;
		return true;
	}

	public void cancelChangeEvent(SignChangeEvent e, String grund) {
		e.getBlock().setType(Material.AIR);
		mess.toPlayer(e.getPlayer(), grund, Messages.IMPORTANT);
	}

}
