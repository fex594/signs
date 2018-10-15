package fex.signs.signs;

import java.util.ArrayList;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import fex.signs.listeners.*;
import fex.signs.util.Messages;

public class Main extends JavaPlugin {
//	private SQL_Connection connection;				//SQL Verbindung
	private boolean sqlConnected;					//Ist eine Verbundung hergestellt?
	private Messages mess;							//Nachrichtensystem an User und Konsole
	private final String version = "1.0";

	@Override
	public void onEnable() {

		//Verbindung zu MySQL aufbauen, Daten aus Config auslesen
//		connection = new SQL_Connection(new PropertieLoader().getConnectionInfo());
		sqlConnected = true;
		mess = Messages.getInstance();

		if (sqlConnected) {
			//Registrieren der Listener
			getServer().getPluginManager().registerEvents(new SignChangeListener(), this);
			getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
			getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
			getServer().getPluginManager().registerEvents(new SignClickListener(), this);
			this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
				public void run() {
					getAbgelaufene();
				}
			}, 20 * 20L, 20 * 600L);// 20*600L = 10 Minuten
		}
		mess.toConsole("Plugin gestartet");

	}

	public void getAbgelaufene() {

		int x = SQLHandler.getInstance().abgelaufen.size();
		if (x > 0) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission("signs.smod")) {
					if (x == 1) {
						mess.toPlayer(p, "Es ist 1 Schild abgelaufen");
					} else {
						mess.toPlayer(p, "Es sind " + x + " Schilder abgelaufen");
					}
				}
			}
		}
	}

	@Override
	public void onDisable() {
		if (sqlConnected) {
			getServer().getScheduler().cancelTasks(this);
			SQLHandler.getInstance().closeConnection();
		}
		mess.toConsole("Plugin gestoppt");
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if(sender instanceof Player ) {
		Player p = (Player) sender;
		try {
		if (cmd.getName().equalsIgnoreCase("signs")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("getSign")) {
					if (p.hasPermission("signs.support")) {
						ItemStack xSchild = new ItemStack(Material.SIGN);
						ItemMeta meta = xSchild.getItemMeta();
						meta.setDisplayName("§6Adminschild");
						xSchild.setItemMeta(meta);
						p.getInventory().addItem(xSchild);
						mess.toPlayer(p, "Ein magisches Schild ist erschienen", Messages.NORMAL);
					} else {
						noPermission(p);
					}
				} else if (args[0].equalsIgnoreCase("help")) {
					if (p.hasPermission("signs.user")) {
						hilfe(p);
					} else {
						noPermission(p);
					}
				} else if (args[0].equalsIgnoreCase("list")) {
					if (p.hasPermission("signs.smod")) {
						if (args.length == 1) {
							ArrayList<String> list = connection.getAbgelaufen();
							mess.toPlayer(p, "Abgelaufene Schilder:");
							if (list.isEmpty()) {
								mess.toPlayer(p, "Keine Schilder offen", Messages.NORMAL);
							} else {
								for (int z = 0; z < list.size(); z++) {
									p.sendMessage("§6" + list.get(z));
								}
							}
						} else {
							ArrayList<String> list = connection.getAbgelaufen(
									Bukkit.getServer().getOfflinePlayer(args[1]).getUniqueId().toString());
							mess.toPlayer(p, "Abgelaufene Schilder von " + args[1]);
							if (list.isEmpty()) {
								mess.toPlayer(p, "Keine abgelaufenen Schilder");
							} else {
								for (int z = 0; z < list.size(); z++) {
									mess.toPlayer(p, list.get(z));
								}
							}
						}
					} else {
						noPermission(p);
					}
				} else if (args[0].equalsIgnoreCase("listActive")) {
					if (p.hasPermission("signs.support")) {
						if (args.length == 1) {
							mess.toPlayer(p, "Aktive Schilder: ");
							ArrayList<String> list = connection.getActive();
							if (list.isEmpty()) {
								mess.toPlayer(p, "Keine Schilder offen", Messages.NORMAL);
							} else {
								for (int z = 0; z < list.size(); z++) {
									p.sendMessage("§6" + list.get(z));
								}
							}
						} else {
							mess.toPlayer(p, "Aktive Schilder von " + args[1]);
							ArrayList<String> list = connection
									.getActive(Bukkit.getServer().getOfflinePlayer(args[1]).getUniqueId().toString());
							if (list.isEmpty()) {
								mess.toPlayer(p, "Der Spieler " + args[1] + " hat keine offenen Schilder");
							} else {
								for (int z = 0; z < list.size(); z++) {
									mess.toPlayer(p, list.get(z));
								}
							}
						}
					} else {
						noPermission(p);
					}
				} else if (args[0].equalsIgnoreCase("info")) {
					if (p.hasPermission("signs.user")) {
						if (args.length == 1) {
							mess.toPlayer(p, "Gebe eine ID an", Messages.IMPORTANT);
						} else {
							try {
								int id = Integer.parseInt(args[1]);
								mess.toPlayer(p, connection.getInfos(id, p), Messages.NORMAL);
							} catch (NumberFormatException e) {
								mess.toPlayer(p, args[1] + " ist keine Zahl!", Messages.IMPORTANT);
							}
						}
					} else {
						noPermission(p);
					}
				} else if (args[0].equalsIgnoreCase("comment")) {
					if (p.hasPermission("signs.support")) {
						if (args.length == 1) {
							mess.toPlayer(p, "Gebe einen Text ein", Messages.IMPORTANT);
						} else {
							String s = "";
							for (int z = 2; z < args.length - 1; z++) {
								s = s + args[z] + " ";
							}
							s = s + args[args.length - 1];
							if (connection.commentSign(Integer.parseInt(args[1]), s)) {
								mess.toPlayer(p, "Schild erfolgreich aktualisiert", Messages.NORMAL);
							} else {
								mess.toPlayer(p, "Fehler beim Aktualisieren des Schildes", Messages.IMPORTANT);
							}
						}
					} else {
						noPermission(p);
					}
				} else if (args[0].equalsIgnoreCase("remove")) {
					if (p.hasPermission("signs.smod")) {
						if (args.length == 1) {
							mess.toPlayer(p, "Missing ID");
						} else {
							try {
								int id = Integer.parseInt(args[1]);
								boolean state1 = connection.isActive(id);
								boolean state2 = connection.isInArea(id);
								if (state1 && state2) {
									connection.setInaktivSign(Integer.valueOf(id));
									mess.toPlayer(p, "Schild deaktiviert");
								} else if (!state2) {
									mess.toPlayer(p, "Das Schild existiert nicht", Messages.IMPORTANT);
								} else if (!state1) {
									mess.toPlayer(p, "Das Schild ist nicht aktiv", Messages.IMPORTANT);
								}
							} catch (NumberFormatException e) {
								mess.toPlayer(p, "Keine Zahl", Messages.IMPORTANT);
							}
						}
					} else {
						noPermission(p);
					}
				} else if (args[0].equalsIgnoreCase("tp")) {
					if (p.hasPermission("signs.support")) {
						if (args.length == 1) {
							mess.toPlayer(p, "Missing ID");
						} else {
							try {
								String location = connection.getLocation(Integer.parseInt(args[1]));
								location = location.replace("(", "").replace(")", "");
								String world = location.substring(0, location.indexOf(":")).replace(":", "");
								double erster = Integer
										.parseInt(location.substring(location.indexOf(":"), location.indexOf("/"))
												.replace(":", "").replace("/", ""))+0.5;
								double zweiter = Integer.parseInt(location
										.substring(location.indexOf("/"), location.lastIndexOf("/")).replace("/", ""));
								double dritter = Integer
										.parseInt(location.substring(location.lastIndexOf("/")).replace("/", ""))+0.5;
								Location l = new Location(Bukkit.getServer().getWorld(world), erster, zweiter, dritter);
								p.teleport(l);
							} catch (NumberFormatException e) {
								mess.toPlayer(p, "Keine Zahl", Messages.IMPORTANT);
							} catch (StringIndexOutOfBoundsException ex) {
								mess.toPlayer(p, "Kein Schild gefunden", Messages.IMPORTANT);
							}

						}
					} else {
						noPermission(p);
					}
				} else if (args[0].equalsIgnoreCase("faq")) {
					if (p.hasPermission("signs.user")) {
						infos(p);
					} else {
						noPermission(p);
					}
				} else if (args[0].equalsIgnoreCase("expand")) {
					if (p.hasPermission("signs.smod")) {
						if (args.length <= 2) {
							mess.toPlayer(p, "Missing Arguments", Messages.IMPORTANT);
						} else {
							try {
								int id = Integer.parseInt(args[1]);
								int days = Integer.parseInt(args[2]);
								java.sql.Date d = new java.sql.Date(connection.getDate(id).getTime());
								Calendar c = Calendar.getInstance();
								c.setTime(d);
								c.add(Calendar.DAY_OF_MONTH, days);
								d = new java.sql.Date(c.getTimeInMillis());
								boolean finished = connection.setDate(id, d, false);
								if (finished) {
									mess.toPlayer(p, "Schild erfolgreich um " + days + " Tage verl§ngert");
								} else {
									mess.toPlayer(p, "Maximale Verl§ngerungszeit erreicht!", Messages.IMPORTANT);
								}
							} catch (NumberFormatException e) {
								mess.toPlayer(p, "Keine Zahl", Messages.IMPORTANT);
							}
						}
					} else {
						noPermission(p);
					}
				} else if (args[0].equalsIgnoreCase("forceexpand")) {
					if (p.hasPermission("signs.admin")) {
						if (args.length <= 2) {
							mess.toPlayer(p, "Missing Arguments", Messages.IMPORTANT);
						} else {
							try {
								int id = Integer.parseInt(args[1]);
								int days = Integer.parseInt(args[2]);
								java.sql.Date d = new java.sql.Date(connection.getDate(id).getTime());
								Calendar c = Calendar.getInstance();
								c.setTime(d);
								c.add(Calendar.DAY_OF_MONTH, days);
								d = new java.sql.Date(c.getTimeInMillis());
								boolean finished = connection.setDate(id, d, true);
								if (finished) {
									mess.toPlayer(p, "Schild erfolgreich um " + days + " Tage verl§ngert");
								} else {
									mess.toPlayer(p, "Maximale Verl§ngerungszeit erreicht!", Messages.IMPORTANT);
								}
							} catch (NumberFormatException e) {
								mess.toPlayer(p, "Keine Zahl", Messages.IMPORTANT);
							}
						}
					} else {
						noPermission(p);
					}
				} else if (args[0].equalsIgnoreCase("removeall")) {
					if (p.hasPermission("signs.admin")) {
						if (args.length <= 1) {
							mess.toPlayer(p, "Gebe einen Namen ein!", Messages.IMPORTANT);
						} else {
							String uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString();
							try {
								ArrayList<String> liste = connection.deleteAllActiveSigns(uuid);
								for (int count = 0; count < liste.size(); count++) {
									String location = liste.get(count);
									try {
										location = location.replace("(", "").replace(")", "");
										String world = location.substring(0, location.indexOf(":")).replace(":", "");
										double erster = Integer.parseInt(
												location.substring(location.indexOf(":"), location.indexOf("/"))
														.replace(":", "").replace("/", ""));
										double zweiter = Integer.parseInt(
												location.substring(location.indexOf("/"), location.lastIndexOf("/"))
														.replace("/", ""));
										double dritter = Integer.parseInt(
												location.substring(location.lastIndexOf("/")).replace("/", ""));
										Location l = new Location(Bukkit.getServer().getWorld(world), erster, zweiter,
												dritter);
										if (isSign(l)) {
											l.getBlock().setType(Material.AIR);
										}
									} catch (NumberFormatException e) {
										mess.toPlayer(p, "Keine Zahl", Messages.IMPORTANT);
									} catch (StringIndexOutOfBoundsException ex) {
										mess.toPlayer(p, "Kein Schild gefunden", Messages.IMPORTANT);
									}
								}
								mess.toPlayer(p,
										"Alle Schilder von §2" + args[1] + "§6 wurden §2erfolgreich §6entfernt!");
							} catch (Exception e) {
								mess.toPlayer(p, "Fehler beim Ausf§hren des Befehls", Messages.ERROR);
								e.printStackTrace();
							}
						}
					} else {
						noPermission(p);
					}
				} else if (args[0].equalsIgnoreCase("signTypes")) {
					if (p.hasPermission("signs.support")) {
							mess.toPlayer(p, "Verfügbare Schilder: §2[Bauregeln], [Verschönern], [Abriss], [Verlängern]");
					} else {
						noPermission(p);
					}
				} else {

					mess.toPlayer(p, "Unbekannter Sub-Befehl", Messages.IMPORTANT);
				}
			} else {
				hilfe(p);
			}

			return true;
		} else {
			return false;
		}}catch(Exception e) {
			mess.toPlayer(p, "Ein Fehler ist aufgetreten!", Messages.ERROR);
		}}
		else {
			mess.toConsole("Dieser Befehl ist nur als Spieler ausführbar");
		}
		return true;
	}

	private void noPermission(Player p) {
		mess.toPlayer(p, "Keine Permission", Messages.IMPORTANT);
	}

	private void hilfe(Player p) {
		if (p.hasPermission("schilder.user")) {
			mess.toPlayer(p, "§2--- §6Befehlsliste §2---");
			mess.toPlayer(p, "/signs help §7- Zeigt die Befehlsliste an");
			mess.toPlayer(p, "/signs info §a[id] §7- Gibt Infos §ber ein Schild");
			if (p.hasPermission("schilder.support")) {
				mess.toPlayer(p, "§2--- §6Support-Befehle §2---");
				mess.toPlayer(p, "/signs getSign §7- Gibt ein Schild");
				mess.toPlayer(p, "/signs listActive §a{Name}§7- Zeigt alle aktiven Schilder an");
				mess.toPlayer(p, "/signs comment §a[id] [Text] §7- Kommentiert ein Schild");
				mess.toPlayer(p, "/signs tp §a[id]§7 - Tpt zu dem Schild");
				mess.toPlayer(p, "/signs signTypes§7 - Listet verfügbare Schilder auf");
				if (p.hasPermission("schilder.smod")) {
					mess.toPlayer(p, "§2--- §6SMod-Befehle §2---");
					mess.toPlayer(p,
							"/signs expand §a[id] [Tage] §7- Verlängert ein Schild um x Tage (maximale Zeit: 30 Tage)");
					mess.toPlayer(p, "/signs list §a{Name}§7- Zeigt alle abgelaufenen Schilder an");
					mess.toPlayer(p, "/signs remove §a[id]§7 - Setzt ein Schild inaktiv (Falls nicht vorhanden)");
					if (p.hasPermission("schilder.admin")) {
						mess.toPlayer(p, "§2--- §6Admin-Befehle §2---");
						mess.toPlayer(p,
								"/signs forceexpand §a[id] [zeit]§7 - Verlängert ein Schild um x Tage (Keine Begrenzung)");
						mess.toPlayer(p,
								"/signs removeall §a[Spieler]§7 - Setzt alle aktiven Schilder eines Spielers inaktiv und entfernt die Schilder");
					}
				}
			}
		}
	}

	private void infos(Player p) {
		if (p.hasPermission("schilder.user")) {
			mess.toPlayer(p, "Informationen");
			mess.toPlayer(p, "Version: §2" + version);
			mess.toPlayer(p, "Author: §2fex594");
			String status = "";
			if (sqlConnected) {
				status = "§2Verbunden";
			} else {
				status = "§cGetrennt";
			}
			mess.toPlayer(p, "Datenbank: " + status);
		} else {
			noPermission(p);
		}
	}

	private boolean isSign(Location e) {
		if (e.getBlock().getType() == Material.WALL_SIGN
				|| e.getBlock().getType() == Material.SIGN)
			return true;
		else
			return false;
	}

}
