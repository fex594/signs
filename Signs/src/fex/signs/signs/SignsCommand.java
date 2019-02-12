package fex.signs.signs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class SignsCommand implements TabCompleter{

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
		List<String> tab = new ArrayList<>();
		if(args.length==1) {
			tab.add("help");
			tab.add("faq");
			tab.add("info");
			if(sender instanceof Player && ((Player) sender).hasPermission("signs.support")) {
			tab.add("getSign");
			tab.add("signtypes");
			tab.add("active");
			tab.add("listactive");
			tab.add("comment");
			tab.add("tp");
			}
			if(sender instanceof Player && ((Player) sender).hasPermission("signs.smod")) {
			tab.add("list");
			tab.add("remove");
			tab.add("expand");
			}
			List<String> temp = new ArrayList<String>();
			for(String in : tab) {
				if(in.startsWith(args[0])) {
					temp.add(in);
					System.out.println(in+" found");
				}
			}
			tab = temp;
			
		}else if(args.length == 2) {
			switch(args[0]) {
			case "list": String all = "all";
						if(all.startsWith(args[args.length-1]))tab.add("all");
			default: @SuppressWarnings("unchecked") List<Player> online = (List<Player>) Bukkit.getOnlinePlayers();
				for(Player p : online) {if(p.getName().toLowerCase().startsWith(args[args.length-1].toLowerCase()))tab.add(p.getName());}
			}
		}else {
			@SuppressWarnings("unchecked")
			List<Player> online = (List<Player>) Bukkit.getOnlinePlayers();
			for(Player p : online) {if(p.getName().toLowerCase().startsWith(args[args.length-1].toLowerCase()))tab.add(p.getName());}
		}
		
//		{
		//Debug
//		String t = "";
//		for(int i = 0; i < args.length; i++) {
//			t += "args["+i+"]: "+args[i]+", ";
//		}
//			System.out.println("Args-Laenge: "+args.length+", s: "+s+", Args: "+t);
//		}
		
		return tab;
	}

//	@Override
//	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
//		// TODO Auto-generated method stub
//		return false;
//	}

}
