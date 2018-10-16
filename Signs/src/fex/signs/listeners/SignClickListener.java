package fex.signs.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import fex.signs.signs.CommandTransformer;
import fex.signs.util.PlayerSign;

public class SignClickListener extends MyListener implements Listener {


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
						int ID = Integer.parseInt(s.getLine(1).replace("§2#", ""));
						for(PlayerSign ps: CommandTransformer.getInstance().getActive(null)) {
							if(ps.getID()== ID){
								String out = "";
								if(ps.getBesitzerUUID().equals(e.getPlayer().getUniqueId().toString()) || e.getPlayer().hasPermission("signs.support")) {
										out = ps.toString();
									} else {
										out = ps.toUserString();
									}
								mess.toPlayer(e.getPlayer(), out);
							}
						}
					}
				}

			}
		}

	}

}
