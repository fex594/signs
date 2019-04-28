package fex.signs.listeners;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import fex.signs.signs.CommandTransformer;
import fex.signs.util.Messages;
import fex.signs.util.Util;

public class BlockBreakListener extends MyListener implements Listener {

	/**
	 * Wird getriggert, wenn ein Block abgebaut wird
	 * 
	 * @param e
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent e) {
		if (Util.isSign(e.getBlock().getType())) {
			Sign s = (Sign) e.getBlock().getState();
			String line = s.getLine(0);

			// Wenn Block Schild mit richtigem Tag in der 1. Zeile ist, und User Permission
			// hat, wird das Schild entfernt
			if (line.equalsIgnoreCase("§4[Bauregeln]") || line.equalsIgnoreCase("§4[Abriss]")
					|| line.equalsIgnoreCase("§4[Verschönern]") || line.equalsIgnoreCase("§4[Weiterbauen]")) {
				if (e.getPlayer().hasPermission("signs.support")) {
					CommandTransformer.getInstance().setInaktiv(Integer.parseInt(s.getLine(1).replace("§2#", "")));

					mess.toPlayer(e.getPlayer(), "Schild erfolgreich entfernt");
				} else {
					mess.toPlayer(e.getPlayer(), "Keine Berechtigung, das Schild zu entfernen", Messages.IMPORTANT);
					e.setCancelled(true);
				}
			}
		}
	}
}
