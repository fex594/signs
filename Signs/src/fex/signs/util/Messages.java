package fex.signs.util;

import org.bukkit.entity.Player;

public class Messages {
	public static final int NORMAL = 1;
	public static final int IMPORTANT = 2;
	public static final int ERROR = 3;
	private static final String LORE = "[Signs]";
	private static Messages instance;

	private Messages() {

	}
	public static Messages getInstance() {
		if(Messages.instance == null) {
			Messages.instance = new Messages();
		}
		return Messages.instance;
	}
	

	/**
	 * Nachricht an User
	 * @param p Spieler
	 * @param s Nachricht
	 */
	public void toPlayer(Player p, String s) {
		toPlayer(p, s, 1);
	}

	/**
	 * Nachriccht an User mit Dringlichkeit
	 * @param p Spieler
	 * @param s Nachricht
	 * @param type Dringlichkeit (1: Normal/Gold, 2: Wichtig/ Hellrot, 3: Fehler/Dunkelrot
	 */
	public void toPlayer(Player p, String s, int type) {
		char c = ' ';
		if (type == NORMAL) {
			c = '6';		//Grau
		} else if (type == IMPORTANT) {
			c = 'c';		//Wichtig Rot
		} else if (type == ERROR) {
			c = '4';		//Fehler Rot
		}
		p.sendMessage("§2"+LORE + " §" + c + s);
	}

	/**
	 * Nachricht an die Konsole
	 * @param s Nachricht
	 */
	public void toConsole(String s) {
		System.out.println(LORE + " " + s);
	}
}
