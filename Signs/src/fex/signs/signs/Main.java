package fex.signs.signs;

import java.util.ArrayList;
import java.util.List;

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
import fex.signs.util.PlayerSign;
import fex.signs.util.Util;

public class Main extends JavaPlugin {
	private Messages mess; // Nachrichtensystem an User und Konsole
	private final String version = "1.0";

	@Override
	public void onEnable() {

		CommandTransformer.getInstance();
		// Auto-Update + First Init
		mess = Messages.getInstance();
		this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			public void run() {
				CommandTransformer.getInstance().update();
			}
		}, 20 * 60 * 30L, 20 * 60 * 60L);// 20*60*60L = 60 Minuten

		getServer().getPluginManager().registerEvents(new SignChangeListener(), this);
		getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
		getServer().getPluginManager().registerEvents(new SignClickListener(), this);

		// Zeitliches anzeigen der abgelaufenen Schilder
		this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			public void run() {
				getAbgelaufene();
			}
		}, 20 * 20L, 20 * 600L);// 20*600L = 10 Minuten

		// Lokale Schilder Auto-Update
		mess.toConsole("Plugin gestartet");

	}

	public void getAbgelaufene() {

		int x = CommandTransformer.getInstance().getAbgelaufen(null).size();
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
		getServer().getScheduler().cancelTasks(this);
		SQLHandler.getInstance().closeConnection();
		mess.toConsole("Plugin gestoppt");
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			// try {
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
						list(args, p);
					} else if (args[0].equalsIgnoreCase("listActive")) {
						listActive(args, p);
					} else if (args[0].equalsIgnoreCase("info")) {
						if (p.hasPermission("signs.user")) {
							if (args.length == 1) {
								mess.toPlayer(p, "Gebe eine ID an", Messages.IMPORTANT);
							} else {
								try {
									int id = Integer.parseInt(args[1]);
									CommandTransformer.getInstance().getInfo(id, p);
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
								if (CommandTransformer.getInstance().commentSign(Integer.parseInt(args[1]), s)) {
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
									boolean state1 = CommandTransformer.getInstance().isActive(id);
									boolean state2 = (id <= CommandTransformer.getInstance().getMaxID());
									if (state1 && state2) {
										CommandTransformer.getInstance().setInaktiv(id);
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
									String location = CommandTransformer.getInstance()
											.getLocation(Integer.parseInt(args[1]));
									if (location == null) {
										mess.toPlayer(p, "Schild existiert nicht mehr", Messages.IMPORTANT);
									} else {
										location = location.replace("(", "").replace(")", "");
										String world = location.substring(0, location.indexOf(":")).replace(":", "");
										double erster = Integer.parseInt(
												location.substring(location.indexOf(":"), location.indexOf("/"))
														.replace(":", "").replace("/", ""))
												+ 0.5;
										double zweiter = Integer.parseInt(
												location.substring(location.indexOf("/"), location.lastIndexOf("/"))
														.replace("/", ""));
										double dritter = Integer.parseInt(
												location.substring(location.lastIndexOf("/")).replace("/", "")) + 0.5;
										Location l = new Location(Bukkit.getServer().getWorld(world), erster, zweiter,
												dritter);
										p.teleport(l);
									}
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
									mess.toPlayer(p, CommandTransformer.getInstance().setDate(id, days, false));
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
									mess.toPlayer(p, CommandTransformer.getInstance().setDate(id, days, true));
								} catch (NumberFormatException e) {
									mess.toPlayer(p, "Keine Zahl", Messages.IMPORTANT);
								}
							}
						} else {
							noPermission(p);
						}
					} else if (args[0].equalsIgnoreCase("signTypes")) {
						if (p.hasPermission("signs.support")) {
							mess.toPlayer(p,
									"Verfügbare Schilder: §2[Bauregeln], [Verschönern], [Abriss], [Weiterbauen]");
						} else {
							noPermission(p);
						}
					} else if (args[0].equalsIgnoreCase("removeall")) {
						if (p.hasPermission("signs.admin")) {
							mess.toPlayer(p, "Nur über die Konsole möglich!", Messages.IMPORTANT);
						} else {
							noPermission(p);
						}
					}

					else {

						mess.toPlayer(p, "Unbekannter Sub-Befehl", Messages.IMPORTANT);
					}
				} else {
					hilfe(p);
				}

				return true;
			} else {
				return false;
			}
		} else if (args[0].equalsIgnoreCase("removeall")) {
			if (args.length <= 1) {
				mess.toConsole("Gebe einen Namen ein!");
			} else {
				String uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString();
				try {
					List<String> liste = CommandTransformer.getInstance().deleteAllSigns(uuid);
					for (int count = 0; count < liste.size(); count++) {
						String location = liste.get(count);
						try {
							location = location.replace("(", "").replace(")", "");
							String world = location.substring(0, location.indexOf(":")).replace(":", "");
							double erster = Integer
									.parseInt(location.substring(location.indexOf(":"), location.indexOf("/"))
											.replace(":", "").replace("/", ""));
							double zweiter = Integer.parseInt(location
									.substring(location.indexOf("/"), location.lastIndexOf("/")).replace("/", ""));
							double dritter = Integer
									.parseInt(location.substring(location.lastIndexOf("/")).replace("/", ""));
							Location l = new Location(Bukkit.getServer().getWorld(world), erster, zweiter, dritter);
							if (Util.isSign(l)) {
								l.getBlock().setType(Material.AIR);
							}
						} catch (NumberFormatException e) {
							mess.toConsole("Keine Zahl");
						} catch (StringIndexOutOfBoundsException ex) {
							mess.toConsole("Kein Schild gefunden");
						}
					}
					mess.toConsole("Alle Schilder von §2" + args[1] + "§6 wurden §2erfolgreich §6entfernt!");
				} catch (Exception e) {
					mess.toConsole("Fehler beim Ausführen des Befehls");
					e.printStackTrace();
				}
			}

		}

		else {
			mess.toConsole("Dieser Befehl ist nur als Spieler ausführbar");
		}
		return true;
	}

	private void noPermission(Player p) {
		mess.toPlayer(p, "Keine Permission", Messages.IMPORTANT);
	}

	@SuppressWarnings("deprecation")
	private void listActive(String[] args, Player p) {
		if (p.hasPermission("signs.smod")) {

			if (args.length == 1) {
				mess.toPlayer(p, "Aktive Schilder: ");
				List<PlayerSign> list = CommandTransformer.getInstance().getActive(null);
				if (list.isEmpty()) {
					mess.toPlayer(p, "Keine Schilder offen", Messages.NORMAL);
				} else {
					ArrayList<String> mastered = new ArrayList<>();
					for (PlayerSign ps : list) {
						mastered.add(ps.toString());
					}
					mess.toPlayerStaged(p, mastered, 1, "/signs listactive -");
				}
			} else if (args.length == 2) {
				if (args[1].startsWith("-")) {
					mess.toPlayer(p, "Aktive Schilder");
					List<PlayerSign> list = CommandTransformer.getInstance().getActive(null);
					if (list.isEmpty()) {
						mess.toPlayer(p, "Keine Schilder gefunden", Messages.NORMAL);
					}
					ArrayList<String> mastered = new ArrayList<>();
					for (PlayerSign ps : list) {
						mastered.add(ps.toString());
					}
					mess.toPlayerStaged(p, mastered, Integer.parseInt(args[1].replace("-", "")), "/signs listActive -");
				} else {
					mess.toPlayer(p, "Aktive Schilder von " + args[1]);
					List<PlayerSign> list = CommandTransformer.getInstance()
							.getActive(Bukkit.getServer().getOfflinePlayer(args[1]).getUniqueId().toString());
					if (list.isEmpty()) {
						mess.toPlayer(p, "Der Spieler " + args[1] + " hat keine offenen Schilder");
					} else {
						ArrayList<String> mastered = new ArrayList<>();
						for (PlayerSign ps : list) {
							mastered.add(ps.toString());
						}
						String send = "/signs listActive " + args[1] + " -";
						mess.toPlayerStaged(p, mastered, 1, send);
					}
				}
			} else if (args.length == 3) {
				if (args[2].startsWith("-")) {
					mess.toPlayer(p, "Aktive Schilder von " + args[1]);
					List<PlayerSign> list = CommandTransformer.getInstance()
							.getActive(Bukkit.getServer().getOfflinePlayer(args[1]).getUniqueId().toString());
					if (list.isEmpty()) {
						mess.toPlayer(p, "Der Spieler " + args[1] + " hat keine offenen Schilder");
					} else {
						ArrayList<String> mastered = new ArrayList<>();
						for (PlayerSign ps : list) {
							mastered.add(ps.toString());
						}
						String send = "/signs listActive " + args[1] + " -";
						mess.toPlayerStaged(p, mastered, Integer.parseInt(args[2].replace("-", "")), send);
					}
				}
			} else {
				mess.toPlayer(p, "Falscher Syntak", Messages.IMPORTANT);
			}

		} else {
			noPermission(p);
		}
	}

	@SuppressWarnings("deprecation")
	private void list(String[] args, Player p) {
		if (p.hasPermission("signs.smod")) {

			if (args.length == 1) {
				mess.toPlayer(p, "Abgelaufene Schilder: ");
				List<PlayerSign> list = CommandTransformer.getInstance().getAbgelaufen(null);
				if (list.isEmpty()) {
					mess.toPlayer(p, "Keine Schilder abgelaufen", Messages.NORMAL);
				} else {
					ArrayList<String> mastered = new ArrayList<>();
					for (PlayerSign ps : list) {
						mastered.add(ps.toString());
					}
					mess.toPlayerStaged(p, mastered, 1, "/signs list -");
				}
			} else if (args.length == 2) {
				if (args[1].startsWith("-")) {
					mess.toPlayer(p, "Abgelaufene Schilder");
					List<PlayerSign> list = CommandTransformer.getInstance().getAbgelaufen(null);
					if (list.isEmpty()) {
						mess.toPlayer(p, "Keine Schilder gefunden", Messages.NORMAL);
					}
					ArrayList<String> mastered = new ArrayList<>();
					for (PlayerSign ps : list) {
						mastered.add(ps.toString());
					}
					mess.toPlayerStaged(p, mastered, Integer.parseInt(args[1].replace("-", "")), "/signs list -");
				} else if (args[1].equalsIgnoreCase("all")) {

					mess.toPlayer(p, "Inaktive Schilder");
					List<PlayerSign> list = CommandTransformer.getInstance().getInactive(null);
					if (list.isEmpty()) {
						mess.toPlayer(p, "Keine Schilder inaktiv");
					} else {
						ArrayList<String> mastered = new ArrayList<>();
						for (PlayerSign ps : list) {
							mastered.add(ps.toString());
						}
						String send = "/signs list all -";
						mess.toPlayerStaged(p, mastered, 1, send);
					}
				} else {
					mess.toPlayer(p, "Abgelaufene Schilder von " + args[1]);
					List<PlayerSign> list = CommandTransformer.getInstance()
							.getAbgelaufen(Bukkit.getServer().getOfflinePlayer(args[1]).getUniqueId().toString());
					if (list.isEmpty()) {
						mess.toPlayer(p, "Der Spieler " + args[1] + " hat keine abgelaufene Schilder");
					} else {
						ArrayList<String> mastered = new ArrayList<>();
						for (PlayerSign ps : list) {
							mastered.add(ps.toString());
						}
						String send = "/signs list " + args[1] + " -";
						mess.toPlayerStaged(p, mastered, 1, send);
					}
				}
			} else if (args.length == 3) {
				if (args[1].equalsIgnoreCase("all")) {
					if (args[2].startsWith("-")) {
						mess.toPlayer(p, "Inaktive Schilder");
						List<PlayerSign> list = CommandTransformer.getInstance().getInactive(null);
						if (list.isEmpty()) {
							mess.toPlayer(p, "Keine Schilder inaktiv");
						} else {
							ArrayList<String> mastered = new ArrayList<>();
							for (PlayerSign ps : list) {
								mastered.add(ps.toString());
							}
							String send = "/signs list all -";
							mess.toPlayerStaged(p, mastered, Integer.parseInt(args[2].replace("-", "")), send);
						}
					} else {
						mess.toPlayer(p, "Inaktive Schilder von " + args[2] + ": ");
						List<PlayerSign> list = CommandTransformer.getInstance()
								.getInactive(Bukkit.getServer().getOfflinePlayer(args[2]).getUniqueId().toString());
						if (list.isEmpty()) {
							mess.toPlayer(p, "Keine Schilder inaktiv");
						} else {
							ArrayList<String> mastered = new ArrayList<>();
							for (PlayerSign ps : list) {
								mastered.add(ps.toString());
							}
							String send = "/signs list all " + args[2] + " -";
							mess.toPlayerStaged(p, mastered, 1, send);
						}
					}
				} else if (args[2].startsWith("-")) {
					mess.toPlayer(p, "Abgelaufene Schilder von " + args[1]);
					List<PlayerSign> list = CommandTransformer.getInstance()
							.getAbgelaufen(Bukkit.getServer().getOfflinePlayer(args[1]).getUniqueId().toString());
					if (list.isEmpty()) {
						mess.toPlayer(p, "Der Spieler " + args[1] + " hat keine abgelaufene Schilder");
					} else {
						ArrayList<String> mastered = new ArrayList<>();
						for (PlayerSign ps : list) {
							mastered.add(ps.toString());
						}
						String send = "/signs list " + args[1] + " -";
						mess.toPlayerStaged(p, mastered, Integer.parseInt(args[2].replace("-", "")), send);
					}
				}
			} else if (args.length == 4) {
				mess.toPlayer(p, "Inaktive Schilder von " + args[2] + ":");
				List<PlayerSign> list = CommandTransformer.getInstance()
						.getInactive(Bukkit.getServer().getOfflinePlayer(args[2]).getUniqueId().toString());
				if (list.isEmpty()) {
					mess.toPlayer(p, "Der Spieler " + args[1] + " hat keine offenen Schilder");
				} else {
					ArrayList<String> mastered = new ArrayList<>();
					for (PlayerSign ps : list) {
						mastered.add(ps.toString());
					}
					String send = "/signs list " + args[1] + " -";
					mess.toPlayerStaged(p, mastered, Integer.parseInt(args[3].replace("-", "")), send);
				}

			} else {
				mess.toPlayer(p, "Falscher Syntak", Messages.IMPORTANT);
			}

		} else {
			noPermission(p);
		}
	}

	private void hilfe(Player p) {
		if (p.hasPermission("signs.user")) {
			mess.toPlayer(p, "§2--- §6Befehlsliste §2---");
			mess.toPlayer(p, "/signs help §7- Zeigt die Befehlsliste an");
			mess.toPlayer(p, "/signs info §8[id] §7- Gibt Infos über ein Schild");
			if (p.hasPermission("signs.support")) {
				mess.toPlayer(p, "§2--- §6Support-Befehle §2---");
				mess.toPlayer(p, "/signs getSign §7- Gibt ein Schild");
				mess.toPlayer(p, "/signs listActive §8{Name} {-Seite} §7- Zeigt alle aktiven Schilder an");
				mess.toPlayer(p, "/signs comment §8[id] [Text] §7- Kommentiert ein Schild");
				mess.toPlayer(p, "/signs tp §8[id]§7 - Tpt zu dem Schild");
				mess.toPlayer(p, "/signs signTypes§7 - Listet verfügbare Schilder auf");
				if (p.hasPermission("signs.smod")) {
					mess.toPlayer(p, "§2--- §6SMod-Befehle §2---");
					mess.toPlayer(p,
							"/signs expand §8[id] [Tage] §7- Verlängert ein Schild um x Tage (maximale Zeit: 30 Tage)");
					mess.toPlayer(p, "/signs list §8{all} {Name} {-Seite} §7- Zeigt alle abgelaufenen Schilder an");
					mess.toPlayer(p, "/signs remove §8[id]§7 - Setzt ein Schild inaktiv (Falls nicht vorhanden)");
					if (p.hasPermission("signs.admin")) {
						mess.toPlayer(p, "§2--- §6Admin-Befehle §2---");
						mess.toPlayer(p,
								"/signs forceexpand §8[id] [zeit]§7 - Verlängert ein Schild um x Tage (Keine Begrenzung)");
						mess.toPlayer(p, "§2--- §6Konsolen-Befehle §2---");
						mess.toPlayer(p,
								"/signs removeall §8[Spieler]§7 - Setzt alle aktiven Schilder eines Spielers inaktiv und entfernt die Schilder");
					}
				}
			}
		}
	}

	private void infos(Player p) {
		if (p.hasPermission("signs.user")) {
			mess.toPlayer(p, "Informationen");
			mess.toPlayer(p, "Version: §2" + version);
			mess.toPlayer(p, "Author: §2fex594");
		} else {
			noPermission(p);
		}
	}
}
