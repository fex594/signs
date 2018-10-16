package fex.signs.signs;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fex.signs.util.PlayerSign;

public class CommandTransformer {

	List<PlayerSign> active;
	List<PlayerSign> abgelaufen;
	int maxID;
	static CommandTransformer instance;

	private CommandTransformer() {
		update();
	}
	
	public static CommandTransformer getInstance() {
		if(instance == null) {
			instance = new CommandTransformer();
		}
		return CommandTransformer.instance;
	}
	
	/**
	 * Infos über aktives Schild, Kennung über ID
	 * @param ID ID
	 */
	public void getInfo(int ID) {
		PlayerSign ps = null;
		for(PlayerSign p : active) {
			if(p.getID()==ID) {
				ps = p;
				break;
			}
		}
		if(ps == null) {
			System.out.println("Kein Schild mit der Nummer "+ID+" aktiv");
		}else
		System.out.println(ps.toString());
	}

	
	public void update(){
		try {
		SQLHandler handle = SQLHandler.getInstance();
		this.active = handle.update();							//Update aktive Schilder
//		System.out.println(active.size());
		this.abgelaufen = new ArrayList<PlayerSign>();
		for(PlayerSign ps : active)								//Update abgelaufene Schilder
			if (ps.getAblaufDatum().before(SQLHandler.now())) { 
				this.abgelaufen.add(ps);
			}													
		
		this.maxID = handle.getMaxID();							//Update MaxID
		
		}catch(SQLException e) {
			e.printStackTrace();
		}

	}
	
	public int getMaxID() {
		return this.maxID;
	}
}
