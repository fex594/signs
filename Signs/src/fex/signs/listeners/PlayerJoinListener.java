package fex.signs.listeners;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import fex.signs.signs.CommandTransformer;
import fex.signs.util.Messages;
import fex.signs.util.PlayerSign;

public class PlayerJoinListener extends MyListener implements Listener{
	
	Plugin plugin;
	
	public PlayerJoinListener(Plugin plugin) {
		this.plugin = plugin;
	}
	/**
	 * Wird getriggert, sobald ein User joined
	 * @param e
	 */
	@EventHandler() 
	public void onPlayerJoin(PlayerJoinEvent e) {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				List<PlayerSign> list = CommandTransformer.getInstance().getActive(e.getPlayer().getUniqueId().toString());
				int x = list.size();
				if (x == 1) {
					mess.toPlayer(e.getPlayer(), "Du hast " + x + " offenes Schild", Messages.IMPORTANT);
				}
				else if(x > 1){
					mess.toPlayer(e.getPlayer(), "Du hast " + x + " offene Schilder", Messages.IMPORTANT);
				}
				if(x>=1) {
					mess.toPlayer(e.getPlayer(), "Nutze /signs list", Messages.IMPORTANT);
				}
			}
		}, 5);
	}
}
