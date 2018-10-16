package fex.signs.tests;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import fex.signs.signs.CommandTransformer;

class SQLConverterTest {
	Player p = Bukkit.getPlayer("fex594");
	
	@Test
	void testVorhanden() {
		CommandTransformer ct = CommandTransformer.getInstance();
		ct.getInfo(36,p);
		
	}
	
	@Test
	void testNichtVorhanden() {
		CommandTransformer ct = CommandTransformer.getInstance();
		ct.getInfo(5,p);
	}
	
	@Test
	void maxID() {
		CommandTransformer ct = CommandTransformer.getInstance();
		System.out.println(ct.getMaxID());
	}

}
