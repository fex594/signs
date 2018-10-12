package fex.signs.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import fex.signs.signs.SQL_Connection;

public class SignClickListener extends MyListener implements Listener {

	public SignClickListener(SQL_Connection sql) {
		super(sql);
	}

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
						mess.toPlayer(e.getPlayer(), sql.getInfos(Integer.parseInt(s.getLine(1).replace("§2#", "")), e.getPlayer()));
					}
				}

			}
		}

	}

}
