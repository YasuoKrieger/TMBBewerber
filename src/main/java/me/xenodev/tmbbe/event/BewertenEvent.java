package me.xenodev.tmbbe.event;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.permission.IPermissionManagement;
import de.dytanic.cloudnet.driver.permission.IPermissionUser;
import me.xenodev.tmbbe.cmd.BewertenCMD;
import me.xenodev.tmbbe.files.Abgaben;
import me.xenodev.tmbbe.files.Bewertungen;
import me.xenodev.tmbbe.main.Main;
import me.xenodev.tmbbe.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;

import java.io.File;
import java.io.IOException;

public class BewertenEvent implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        File file = new File("plugins//TMBClan//Bewertungen", p.getName() + ".yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        if (BewertenCMD.bewerten.contains(p)) {
            if (e.getItem().getItemMeta().getDisplayName().equals("§6Plot Bewerten")) {
                if (PlayerEvent.currentplayer.containsKey(p)) {
                    e.setCancelled(true);
                    Inventory inv = Bukkit.createInventory(p, 9 * 3, "§7>> §6Bewerten §7<<");

                    for (int i = 0; i < 27; i++) {
                        inv.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).build());
                    }

                    inv.setItem(10, new ItemBuilder(Material.LIME_DYE).setName("§aAnnehmen").setLore("", "§7Nehme den ausgewählten Spieler an").build());
                    OfflinePlayer t = Bukkit.getOfflinePlayer(PlayerEvent.currentplayer.get(p));
                    inv.setItem(12, new ItemBuilder(Material.PLAYER_HEAD).setOwner(PlayerEvent.currentplayer.get(p)).setName("§6" + PlayerEvent.currentplayer.get(p)).setLore("§7Hat sich beworben am", "", "§7Datum: §a" + cfg.get("date").toString(), "§7Zeit: §a" + cfg.getString("time").toString()).build());
                    inv.setItem(14, new ItemBuilder(Material.RED_DYE).setName("§cAblehnen").setLore("", "§7Lehne den ausgewählten Spieler ab").build());
                    inv.setItem(16, new ItemBuilder(Material.BARRIER).setName("§7Breche den Vorgang ab").build());
                    p.openInventory(inv);
                } else {
                    e.setCancelled(true);
                    p.sendMessage(Main.error + "§7Du hast noch keinen Spieler ausgewählt");
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        OfflinePlayer t = Bukkit.getOfflinePlayer(PlayerEvent.currentplayer.get(p));
        File file = new File("plugins//TMBClan//Bewertungen", t.getName() + ".yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        if (e.getView().getTitle().equals("§7>> §6Bewerten §7<<")) {
            e.setCancelled(true);
            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aAnnehmen")) {
                p.closeInventory();
                Inventory inv = Bukkit.createInventory(null, 9 * 1, "§7>> §6Annehmen? §7<<");

                inv.setItem(1, new ItemBuilder(Material.LIME_DYE).setName("§aBestätigen").build());
                inv.setItem(7, new ItemBuilder(Material.RED_DYE).setName("§cAbbrechen").build());

                inv.setItem(12, new ItemBuilder(Material.PLAYER_HEAD).setOwner(PlayerEvent.currentplayer.get(p)).setName("§6" + PlayerEvent.currentplayer.get(p)).setLore("§7Hat sich beworben am", "", "§7Datum: §a" + cfg.get("date").toString(), "§7Zeit: §a" + cfg.getString("time").toString()).build());
                p.openInventory(inv);
            } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cAblehnen")) {
                p.closeInventory();
                Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                openAblehnen(p, inv);
            } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§7Breche den Vorgang ab")) {
                p.closeInventory();
                PlayerEvent.currentplayer.remove(p);
            }
        } else if (e.getView().getTitle().equals("§7>> §6Annehmen? §7<<")) {
            e.setCancelled(true);
            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aBestätigen")) {
                if (!cfg.get("angenommen").equals("Angenommen")) {
                    cfg.set("angenommen", "Angenommen");
                    try {
                        cfg.save(file);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    IPermissionManagement manager = CloudNetDriver.getInstance().getPermissionManagement();
                    IPermissionUser player = manager.getUser(t.getUniqueId());
                    if (player.inGroup("Neuling")) {
                        player.addGroup("Azubi");
                    } else if (player.inGroup("Azubi")) {
                        player.removeGroup("Azubi");
                        player.addGroup("Testbuilder");
                    }
                    Abgaben.players.remove(PlayerEvent.currentplayer.get(p));
                    PlayerEvent.currentplayer.remove(p);
                    Abgaben.addList(Abgaben.players);
                    Abgaben.addArray();
                    p.closeInventory();
                    p.sendMessage(Main.prefix + "§7Der Spieler wurde angenommen");

                } else {
                    Abgaben.players.remove(PlayerEvent.currentplayer.get(p));
                    PlayerEvent.currentplayer.remove(p);
                    Abgaben.addList(Abgaben.players);
                    Abgaben.addArray();
                    p.closeInventory();
                    p.sendMessage(Main.error + "§cDer Spieler wurde bereits angenommen");

                }
            } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cAbbrechen")) {

                p.closeInventory();
                PlayerEvent.currentplayer.remove(p);
            }
        } else if (e.getView().getTitle().equals("§7>> §6Grund §7<<")) {
            e.setCancelled(true);
            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cAbbrechen")) {
                p.closeInventory();
                cfg.set("terra.forming", false);
                cfg.set("terra.aufbau", false);
                cfg.set("terra.coloring", false);
                cfg.set("terra.nichtvorhanden", false);
                cfg.set("plot.vegetation", false);
                cfg.set("plot.zuleer", false);
                cfg.set("plot.zusammenhangslos", false);
                cfg.set("plot.zuvoll", false);
                cfg.set("mapping.baeume", false);
                cfg.set("struktur.detailsetzung", false);
                cfg.set("struktur.dach", false);
                cfg.set("struktur.details", false);
                cfg.set("struktur.aufbau", false);
                PlayerEvent.currentplayer.remove(p);
            } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aBestätigen")) {
                p.closeInventory();
                Inventory inv = Bukkit.createInventory(null, 9 * 1, "§7>> §6Ablehnen? §7<<");

                inv.setItem(1, new ItemBuilder(Material.LIME_DYE).setName("§aBestätigen").build());
                inv.setItem(7, new ItemBuilder(Material.RED_DYE).setName("§cAbbrechen").build());

                inv.setItem(4, new ItemBuilder(Material.PLAYER_HEAD).setOwner(PlayerEvent.currentplayer.get(p)).setName("§6" + PlayerEvent.currentplayer.get(p)).build());

                p.openInventory(inv);
            } else {
                if (e.getCurrentItem().getItemMeta().getEnchantLevel(Enchantment.CHANNELING) == 1) {
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Terra - Forming")) {
                        cfg.set("terra.forming", false);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Terra - Aufbau")) {
                        cfg.set("terra.aufbau", false);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Terra - Coloring")) {
                        cfg.set("terra.coloring", false);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Terra - Nicht Vorhanden")) {
                        cfg.set("terra.aufbau", false);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Plot - Vegetation")) {
                        cfg.set("plot.vegetation", false);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Plot - zu leer")) {
                        cfg.set("plot.zuleer", false);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Plot - Zusammenhangslos")) {
                        cfg.set("plot.zusammenhangslos", false);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Plot - zu voll")) {
                        cfg.set("plot.zuvoll", false);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Mapping - Bäume")) {
                        cfg.set("mapping.baeume", false);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Struktur - Detailsetzung")) {
                        cfg.set("struktur.detailsetztung", false);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Struktur - Dach")) {
                        cfg.set("struktur.dach", false);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Struktur - Aufbau")) {
                        cfg.set("struktur.aufbau", false);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Struktur - Details")) {
                        cfg.set("struktur.details", false);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else p.sendMessage(Main.error + "§cDiesen Grund gibt es nicht");
                } else if (!e.getCurrentItem().getItemMeta().hasEnchants()) {
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Terra - Forming")) {
                        cfg.set("terra.forming", true);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Terra - Aufbau")) {
                        cfg.set("terra.aufbau", true);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Terra - Coloring")) {
                        cfg.set("terra.coloring", true);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Terra - Nicht Vorhanden")) {
                        cfg.set("terra.aufbau", true);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Plot - Vegetation")) {
                        cfg.set("plot.vegetation", true);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Plot - zu leer")) {
                        cfg.set("plot.zuleer", true);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Plot - Zusammenhangslos")) {
                        cfg.set("plot.zusammenhangslos", true);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Plot - zu voll")) {
                        cfg.set("plot.zuvoll", true);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Mapping - Bäume")) {
                        cfg.set("mapping.baeume", true);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Struktur - Detailsetzung")) {
                        cfg.set("struktur.detailsetzung", true);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Struktur - Dach")) {
                        cfg.set("struktur.dach", true);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Struktur - Aufbau")) {
                        cfg.set("struktur.aufbau", true);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Struktur - Details")) {
                        cfg.set("struktur.details", true);
                        p.closeInventory();
                        Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                        openAblehnen(p, inv);
                        try {
                            cfg.save(file);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    } else p.sendMessage(Main.error + "§cDiesen Grund gibt es nicht");
                }
            }
        } else if (e.getView().getTitle().equals("§7>> §6Ablehnen? §7<<")) {
            e.setCancelled(true);
            if(e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§aBestätigen")){
                p.closeInventory();
                cfg.set("angenommen", "Abgelehnt");
                Abgaben.players.remove(PlayerEvent.currentplayer.get(p));
                PlayerEvent.currentplayer.remove(p);
                Abgaben.addList(Abgaben.players);
                Abgaben.addArray();
            }else if(e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§cAbbrechen")){
                p.closeInventory();
                Inventory inv = Bukkit.createInventory(null, 9 * 6, "§7>> §6Grund §7<<");
                openAblehnen(p, inv);
            }
        }
    }

    private void openAblehnen(Player p, Inventory inv) {

        File file = new File("plugins//TMBClan//Bewertungen", p.getName() + ".yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);


        for (int i = 0; i < 54; i++) {
            inv.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).build());
        }
        if (cfg.get("terra.forming").equals(true)) {
            inv.setItem(0, new ItemBuilder(Material.STONE).setName("§6Terra - Forming").setLore("", "§7Versuche dein Terraforming zu Verbessern").addEnchantment(Enchantment.CHANNELING, 1).addFlag(ItemFlag.HIDE_ENCHANTS).build());
        } else
            inv.setItem(0, new ItemBuilder(Material.STONE).setName("§6Terra - Forming").setLore("", "§7Versuche dein Terraforming zu Verbessern").build());

        if (cfg.get("terra.aufbau").equals(true)) {
            inv.setItem(9, new ItemBuilder(Material.NETHER_BRICK).setName("§6Terra - Aufbau").setLore("", "§7Verbessere den Aufbau deines Terras").addEnchantment(Enchantment.CHANNELING, 1).addFlag(ItemFlag.HIDE_ENCHANTS).build());
        } else
            inv.setItem(9, new ItemBuilder(Material.NETHER_BRICK).setName("§6Terra - Aufbau").setLore("", "§7Verbessere den Aufbau deines Terras").build());

        if (cfg.get("terra.nichtvorhanden").equals(true)) {
            inv.setItem(10, new ItemBuilder(Material.OAK_FENCE).setName("§6Terra - Nicht Vorhanden").setLore("", "§7Du musst dein Terra formen").addEnchantment(Enchantment.CHANNELING, 1).addFlag(ItemFlag.HIDE_ENCHANTS).build());
        } else
            inv.setItem(10, new ItemBuilder(Material.OAK_FENCE).setName("§6Terra - Nicht Vorhanden").setLore("", "§7Du musst dein Terra formen").build());

        if (cfg.get("terra.coloring").equals(true)) {
            inv.setItem(18, new ItemBuilder(Material.WHITE_TERRACOTTA).setName("§6Terra - Coloring").setLore("", "§7Verbessere dein Terracoloring durch passende Blockwahl").addEnchantment(Enchantment.CHANNELING, 1).addFlag(ItemFlag.HIDE_ENCHANTS).build());
        } else
            inv.setItem(18, new ItemBuilder(Material.WHITE_TERRACOTTA).setName("§6Terra - Coloring").setLore("", "§7Verbessere dein Terracoloring durch passende Blockwahl").build());

        if (cfg.get("struktur.detailsetzung").equals(true)) {
            inv.setItem(8, new ItemBuilder(Material.COBBLESTONE_WALL).setName("§6Struktur - Detailsetzung").setLore("", "§7Versuche die Details deiner Struktur besser zu setzen").addEnchantment(Enchantment.CHANNELING, 1).addFlag(ItemFlag.HIDE_ENCHANTS).build());
        } else
            inv.setItem(8, new ItemBuilder(Material.COBBLESTONE_WALL).setName("§6Struktur - Detailsetzung").setLore("", "§7Versuche die Details deiner Struktur besser zu setzen").build());

        if (cfg.get("struktur.detailsetzung").equals(true)) {
            inv.setItem(16, new ItemBuilder(Material.OAK_FENCE).setName("§6Struktur - Details").setLore("", "§7Verbessere die Details deiner Strukturen").addEnchantment(Enchantment.CHANNELING, 1).addFlag(ItemFlag.HIDE_ENCHANTS).build());

        } else
            inv.setItem(16, new ItemBuilder(Material.OAK_FENCE).setName("§6Struktur - Details").setLore("", "§7Verbessere die Details deiner Strukturen").build());

        if (cfg.get("struktur.dach").equals(true)) {
            inv.setItem(17, new ItemBuilder(Material.BRICK_STAIRS).setName("§6Struktur - Dach").setLore("", "§7Der Aufbau deines Dachs ist verbesserungswürdig").addEnchantment(Enchantment.CHANNELING, 1).addFlag(ItemFlag.HIDE_ENCHANTS).build());
        } else
            inv.setItem(17, new ItemBuilder(Material.BRICK_STAIRS).setName("§6Struktur - Dach").setLore("", "§7Der Aufbau deines Dachs ist verbesserungswürdig").build());

        if (cfg.get("struktur.aufbau").equals(true)) {
            inv.setItem(26, new ItemBuilder(Material.BRICKS).setName("§6Struktur - Aufbau").setLore("", "§7Verbessere den Aufbau deiner Struktur").addEnchantment(Enchantment.CHANNELING, 1).addFlag(ItemFlag.HIDE_ENCHANTS).build());
        } else
            inv.setItem(26, new ItemBuilder(Material.BRICKS).setName("§6Struktur - Aufbau").setLore("", "§7Verbessere den Aufbau deiner Struktur").build());

        if (cfg.get("plot.vegetation").equals(true)) {
            inv.setItem(4, new ItemBuilder(Material.VINE).setName("§6Plot - Vegetation").setLore("", "§7Die Vegetation auf deinem Plot fehlt oder ist schlecht plaziert").addEnchantment(Enchantment.CHANNELING, 1).addFlag(ItemFlag.HIDE_ENCHANTS).build());

        } else
            inv.setItem(4, new ItemBuilder(Material.VINE).setName("§6Plot - Vegetation").setLore("", "§7Die Vegetation auf deinem Plot fehlt oder ist schlecht plaziert").build());

        if (cfg.get("plot.zuleer").equals(true)) {
            inv.setItem(12, new ItemBuilder(Material.WHITE_STAINED_GLASS).setName("§6Plot - zu leer").setLore("", "§7Dein Plot fühlt sich zu leer an").addEnchantment(Enchantment.CHANNELING, 1).addFlag(ItemFlag.HIDE_ENCHANTS).build());

        } else
            inv.setItem(12, new ItemBuilder(Material.WHITE_STAINED_GLASS).setName("§6Plot - zu leer").setLore("", "§7Dein Plot fühlt sich zu leer an").build());

        if (cfg.get("plot.zusammenhangslos").equals(true)) {
            inv.setItem(13, new ItemBuilder(Material.MOSSY_COBBLESTONE).setName("§6Plot - Zusammenhangslos").setLore("", "§7Der Aufbau deines Plots hat keinen zusammenhang").addEnchantment(Enchantment.CHANNELING, 1).addFlag(ItemFlag.HIDE_ENCHANTS).build());

        } else
            inv.setItem(13, new ItemBuilder(Material.MOSSY_COBBLESTONE).setName("§6Plot - Zusammenhangslos").setLore("", "§7Der Aufbau deines Plots hat keinen zusammenhang").build());

        if (cfg.get("plot.zuvoll").equals(true)) {
            inv.setItem(14, new ItemBuilder(Material.COAL).setName("§6Plot - zu voll").setLore("", "§7Dein Plot fühlt sich zu voll an").addEnchantment(Enchantment.CHANNELING, 1).addFlag(ItemFlag.HIDE_ENCHANTS).build());
        } else
            inv.setItem(14, new ItemBuilder(Material.COAL).setName("§6Plot - zu voll").setLore("", "§7Dein Plot fühlt sich zu voll an").build());

        if (cfg.get("mapping.baeume").equals(true)) {
            inv.setItem(22, new ItemBuilder(Material.OAK_LEAVES).setName("§6Mapping - Bäume").setLore("", "§7Die Qualität deiner Bäume entspricht nicht unseren Standards").addEnchantment(Enchantment.CHANNELING, 1).addFlag(ItemFlag.HIDE_ENCHANTS).build());
        } else
            inv.setItem(22, new ItemBuilder(Material.OAK_LEAVES).setName("§6Mapping - Bäume").setLore("", "§7Die Qualität deiner Bäume entspricht nicht unseren Standards").build());


        inv.setItem(38, new ItemBuilder(Material.LIME_DYE).setName("§aBestätigen").build());
        OfflinePlayer t = Bukkit.getOfflinePlayer(PlayerEvent.currentplayer.get(p));
        inv.setItem(40, new ItemBuilder(Material.PLAYER_HEAD).setOwner(PlayerEvent.currentplayer.get(p)).setName("§6" + PlayerEvent.currentplayer.get(p)).setLore("§7Hat sich beworben am", "", "§7Datum: §a" + cfg.get("date").toString(), "§7Zeit: §a" + cfg.getString("time").toString()).build());
        inv.setItem(42, new ItemBuilder(Material.RED_DYE).setName("§cAbbrechen").build());


        p.openInventory(inv);
    }

}
