package fex.signs.listeners;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fex.signs.util.Messages;
import fex.signs.util.PlayerSign;

public class PlayerJoinListener extends MyListener implements Listener{
	
//	public PlayerJoinListener(SQL_Connection sql) {
//		super(sql);
//	}
	
	
	/**
	 * Wird getriggert, sobald ein User joined
	 * @param e
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		List<PlayerSign> list = _sql.abgelaufen;
		int x = 0;
		for(PlayerSign ps : list) {
			if(ps.getBesitzerUUID().equals(e.getPlayer().getUniqueId().toString()))
				x++;
		}
	//	int x = sql.getActiveSigns(e.getPlayer().getUniqueId().toString());
		if (x == 1) {
			mess.toPlayer(e.getPlayer(), "Du hast " + x + " offenes Schild", Messages.IMPORTANT);
		}
		else if(x > 1){
			mess.toPlayer(e.getPlayer(), "Du hast " + x + " offene Schilder", Messages.IMPORTANT);
		}
	}
}
