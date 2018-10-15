package fex.signs.listeners;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;

import fex.signs.util.Messages;

public class BlockBreakListener extends MyListener implements Listener {

//	public BlockBreakListener(SQL_Connection sql) {
//		super(sql);
//	}

	/**
	 * Wird getriggert, wenn ein Block abgebaut wird
	 * @param e
	 */
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (isSign(e)) {
			Sign s = (Sign) e.getBlock().getState();
			String line = s.getLine(0);
			
			//Wenn Block Schild mit richtigem Tag in der 1. Zeile ist, und User Permission hat, wird das Schild entfernt
			if (line.equalsIgnoreCase("§4[Bauregeln]") || line.equalsIgnoreCase("§4[Abriss]")
					|| line.equalsIgnoreCase("§4[Verschönern]") || line.equalsIgnoreCase("§4[Weiterbauen]")) {
				if (e.getPlayer().hasPermission("signs.support")) {
					
					//Fehlerhaft
				//	sql.setInaktivSign(Integer.parseInt(s.getLine(1).replace("§2#", "")));
					
					mess.toPlayer(e.getPlayer(), "Schild erfolgreich NICHT!!!!! entfernt");
				} else {
					mess.toPlayer(e.getPlayer(), "Keine Berechtigung, das Schild zu entfernen", Messages.IMPORTANT);
					e.setCancelled(true);
				}
			}
		}
	}

	private boolean isSign(BlockEvent e) {
		if (e.getBlock().getType() == Material.WALL_SIGN || e.getBlock().getType() == Material.SIGN)
			return true;
		else
			return false;
	}

}
