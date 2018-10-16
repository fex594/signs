package fex.signs.tests;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fex.signs.signs.CommandTransformer;

public class ConverterTest{
	public static void main(String[] args) {
		CommandTransformer ct= CommandTransformer.getInstance();
		Player p = Bukkit.getPlayer("fex594");
		ct.getInfo(36,p);
		ct.getInfo(5,p);
		System.out.println(ct.getMaxID());
		
	}
}
