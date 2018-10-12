package fex.signs.listeners;

import fex.signs.signs.SQLHandler;
import fex.signs.signs.SQL_Connection;
import fex.signs.util.Messages;

/**
 * Abstrakte Klasse, die allen Listenern eine SQL-Verbindung und eine Instanz der Message-Klasse bereitstellt
 * @author S30
 *
 */
public abstract class MyListener {

	@Deprecated
	SQL_Connection sql;
	
	Messages mess;
	SQLHandler _sql;

	@Deprecated
	public MyListener(SQL_Connection sql) {
		this.sql = sql;
		mess = Messages.getInstance();
	}
	
	public MyListener() {
		mess = Messages.getInstance();
		this._sql = SQLHandler.getInstance();
	}

}
