package fex.signs.listeners;

import fex.signs.util.Messages;

/**
 * Abstrakte Klasse, die allen Listenern eine SQL-Verbindung und eine Instanz der Message-Klasse bereitstellt
 * @author Fex594
 *
 */
public abstract class MyListener {

	Messages mess;

	public MyListener() {
		mess = Messages.getInstance();
	}

}
