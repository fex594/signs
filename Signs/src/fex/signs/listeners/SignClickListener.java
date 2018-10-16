package fex.signs.listeners;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import fex.signs.util.PlayerSign;

public class SignClickListener extends MyListener implements Listener {

//	public SignClickListener(SQL_Connection sql) {
//		super(sql);
//	}

	@EventHandler
	public void onSignClick(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getPlayer().hasPermission("signs.user")) {
				Block b = e.getClickedBlock();
				if (b.getType() == Material.WALL_SIGN
						|| b.getType() == Material.SIGN) {
					Sign s = (Sign) b.getState();
					String firstLine = s.getLine(0);
					if (firstLine.equalsIgnoreCase("§4[Bauregeln]") || (firstLine.equalsIgnoreCase("§4[Verschönern]"))
							|| (firstLine.equalsIgnoreCase("§4[Abriss]"))
							|| (firstLine.equalsIgnoreCase("§4[Weiterbauen]"))) {
						for(PlayerSign ps: _sql.active) {
							int ID = Integer.parseInt(s.getLine(1).replace("§2#", ""));
							if(ps.getID()== ID){
								String out = "";
								if(ps.getBesitzerUUID().equals(e.getPlayer().getUniqueId().toString()) || e.getPlayer().hasPermission("signs.support")) {
										int act = ps.getActive();
										String active = "";
										if (act == 1) {
											DateFormat df = new SimpleDateFormat("dd.MM.YYYY");
											String date = df.format(ps.getAblaufDatum()).toString();
											active = "läuft am §2" + date + "§6 ab";
										} else {
											active = "ist §cinaktiv";
										}
										String pName = Bukkit.getOfflinePlayer(UUID.fromString(ps.getBesitzerUUID())).getName();
										String erName = Bukkit.getOfflinePlayer(UUID.fromString(ps.getErstellerUUID())).getName();
										out = "§6Ein §2" + ps.getType() + "§6-Schild für §2" + pName + "§6 mit der ID §2"
												+ ID + "§6 " + active + "§6. Ersteller: §2" + erName;
										if (!(ps.getText() == null)) {
											out = out + " §6(Grund: §c" + ps.getText() + "§6)";
										}
									} else {
										String pName = Bukkit.getOfflinePlayer(UUID.fromString(ps.getErstellerUUID())).getName();
										out = "Typ: §2" + ps.getType() + "§6, Spieler: " + pName;
									}
							}
						}
	//					mess.toPlayer(e.getPlayer(), sql.getInfos(Integer.parseInt(s.getLine(1).replace("§2#", "")), e.getPlayer()));
	//Wurde ersetzt
					}
				}

			}
		}

	}

}
