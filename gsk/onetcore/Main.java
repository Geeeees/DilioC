package gsk.onetcore;

import com.google.common.collect.Sets;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.milkbowl.vault.permission.Permission;
import gsk.onetcore.commands.Alerts;
import gsk.onetcore.commands.CPS;
import gsk.onetcore.commands.Clearchat;
import gsk.onetcore.commands.Crowbar;
import gsk.onetcore.commands.Dragon;
import gsk.onetcore.commands.End;
import gsk.onetcore.commands.Freeze;
import gsk.onetcore.commands.Lock;
import gsk.onetcore.commands.Lol;
import gsk.onetcore.commands.PVP;
import gsk.onetcore.commands.Rename;
import gsk.onetcore.commands.Say;
import gsk.onetcore.commands.SetEndSpawn;
import gsk.onetcore.commands.Setspawn;
import gsk.onetcore.commands.Spawn;
import gsk.onetcore.commands.Staff;
import gsk.onetcore.commands.Staffchat;
import gsk.onetcore.commands.ToggleGlobalchat;
import gsk.onetcore.commands.Togglechat;
import gsk.onetcore.commands.Who;
import gsk.onetcore.listeners.ConfigManager;
import gsk.onetcore.listeners.EnderpearlListener;
import gsk.onetcore.listeners.PlayerListener;
import gsk.onetcore.listeners.ProxyCheck;
import gsk.onetcore.listeners.SpawnTagListener;
import gsk.onetcore.managers.DeathMessageHandler;
import gsk.onetcore.managers.server.ServerHandler;
import gsk.onetcore.managers.signs.SignManager;
import gsk.onetcore.pvptimer.PvPTimer;
import gsk.onetcore.scoreboard.ScoreboardHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public class Main
extends JavaPlugin {
    public HashMap<UUID, Double> clickRate = new HashMap();
    public HashMap<UUID, Integer> clickCount = new HashMap();
    public static ArrayList<UUID> alerts = new ArrayList();
    FileConfiguration config;
    public int maxProt = 2;
    public int maxFireProt = 4;
    public int maxProjProt = 4;
    public int maxThorns = 0;
    public int maxFire = 0;
    public int maxSharp = 2;
    public int maxKB = 0;
    public int maxBowPower = 2;
    public int maxBowFire = 1;
    public int maxBowInfinity = 1;
    public int maxBowKB = 0;
    public static Set<Object> DISALLOWED_POTIONS;
    public Permission permission;
    private static HashMap<PotionEffectType, Integer> levels;
    private ServerHandler serverHandler;
    private ScoreboardHandler scoreboardHandler;
    private PvPTimer pvpTimer;
    private ConfigManager cManager;
    private YamlConfiguration yaml;
    private File file;
    private static Main instance;
    private SignManager signManager;
    public static final Random RANDOM;

    static {
        levels = new HashMap();
        RANDOM = new Random();
    }

    public boolean setupPermissions() {
        RegisteredServiceProvider rsp = this.getServer().getServicesManager().getRegistration((Class)Permission.class);
        this.permission = (Permission)rsp.getProvider();
        if (this.permission != null) {
            return true;
        }
        return false;
    }

    public static Main getInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        DISALLOWED_POTIONS = Sets.newHashSet((Object[])new Integer[]{8193, 8225, 8257, 16385, 16417, 16449, 8200, 8232, 8264, 16392, 16424, 16456, 8233, 16393, 16425, 16457, 8204, 8236, 8268, 16396, 16428, 16460, 8238, 8270, 16430, 16462, 16398, 8238, 8228, 8260, 16420, 16452, 8234, 8266, 16426, 16458});
        this.setupCommands();
        this.setupListeners();
        this.scoreboardHandler = new ScoreboardHandler();
        if (!new File(this.getDataFolder(), "config.yml").exists()) {
            this.saveDefaultConfig();
        }
        this.loadEnchants();
        DeathMessageHandler.init();
        this.setupPermissions();
        this.setupRegister();
        this.setupConfigManager();
        this.cManager = new ConfigManager(this.yaml, this.file);
        this.saveYaml();
        ItemStack is = new ItemStack(Material.SPECKLED_MELON);
        ShapelessRecipe melon = new ShapelessRecipe(is).addIngredient(Material.MELON).addIngredient(Material.GOLD_INGOT);
        Bukkit.addRecipe((Recipe)melon);
        this.signManager = new SignManager();
        this.serverHandler = new ServerHandler();
        new gsk.onetcore.listeners.ProxyCheck();
        this.getRate();
        PotionEffectType[] arrpotionEffectType = PotionEffectType.values();
        int n = arrpotionEffectType.length;
        int n2 = 0;
        while (n2 < n) {
            PotionEffectType pEffectType = arrpotionEffectType[n2];
            if (pEffectType != null) {
                this.getConfig().addDefault(pEffectType.getName(), (Object)-1);
                int k = this.getConfig().getInt(pEffectType.getName());
                if (k != -1) {
                    levels.put(pEffectType, k);
                }
            }
            ++n2;
        }
    }

    public void onDisable() {
    }

    public HashMap<PotionEffectType, Integer> getLevels() {
        return levels;
    }

    public void getRate() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)this, new Runnable(){
            LinkedList<Double> ll;
            double sum;
            double total;

            @Override
            public void run() {
                Player[] arrplayer = Bukkit.getOnlinePlayers();
                int n = arrplayer.length;
                int n2 = 0;
                while (n2 < n) {
                    Player p = arrplayer[n2];
                    if (Main.this.clickCount.containsKey(p.getUniqueId())) {
                        double count = Main.this.clickCount.get(p.getUniqueId()).intValue();
                        if (count > (double)Main.this.getConfigManager().getMaxCPS().intValue()) {
                            Player[] arrplayer2 = Bukkit.getOnlinePlayers();
                            int n3 = arrplayer2.length;
                            int n4 = 0;
                            while (n4 < n3) {
                                Player on = arrplayer2[n4];
                                if (on.hasPermission("staff.mod") && !Main.alerts.contains(on.getUniqueId())) {
                                    on.sendMessage("\u00a7c" + p.getName() + "\u00a7e has reached a CPS of \u00a76\u00a7l" + count);
                                }
                                ++n4;
                            }
                        }
                        if (count > 0.0) {
                            this.ll.add(count);
                            Iterator<Double> localIterator = this.ll.iterator();
                            while (localIterator.hasNext()) {
                                double x = localIterator.next();
                                x = count;
                                this.total = this.sum + x;
                            }
                        }
                        this.total = 0.0;
                        try {
                            this.ll.remove();
                        }
                        catch (Exception localIterator) {
                            // empty catch block
                        }
                        Main.this.clickRate.put(p.getUniqueId(), count);
                        Main.this.clickCount.put(p.getUniqueId(), 0);
                    }
                    ++n2;
                }
            }
        }, 0, 20);
    }

    public void loadEnchants() {
        this.config = this.getConfig();
        this.maxProt = this.config.getInt("maxProt");
        this.maxFireProt = this.config.getInt("maxFireProt");
        this.maxProjProt = this.config.getInt("maxProjProt");
        this.maxThorns = this.config.getInt("maxThorns");
        this.maxSharp = this.config.getInt("maxSharp");
        this.maxFire = this.config.getInt("maxFire");
        this.maxKB = this.config.getInt("maxKB");
        this.maxBowPower = this.config.getInt("maxBowPower");
        this.maxBowFire = this.config.getInt("maxBowFire");
        this.maxBowInfinity = this.config.getInt("maxBowInfinity");
        this.maxBowKB = this.config.getInt("maxBowKB");
    }

    public void setupRegister() {
        Who who = new Who();
        this.getCommand(who.getCommand()).setExecutor((CommandExecutor)who);
        Spawn spawn = new Spawn();
        this.getCommand(spawn.getCommand()).setExecutor((CommandExecutor)spawn);
        Rename rename = new Rename();
        this.getCommand(rename.getCommand()).setExecutor((CommandExecutor)rename);
        Lock lock = new Lock();
        this.getCommand(lock.getCommand()).setExecutor((CommandExecutor)lock);
        Crowbar crowbar = new Crowbar();
        this.getCommand(crowbar.getCommand()).setExecutor((CommandExecutor)crowbar);
        Dragon dragon = new Dragon();
        this.getCommand(dragon.getCommand()).setExecutor((CommandExecutor)dragon);
        End end = new End();
        this.getCommand(end.getCommand()).setExecutor((CommandExecutor)end);
        SetEndSpawn endSpawn = new SetEndSpawn();
        this.getCommand(endSpawn.getCommand()).setExecutor((CommandExecutor)endSpawn);
        PVP pvp = new PVP();
        this.getCommand(pvp.getCommand()).setExecutor((CommandExecutor)pvp);
        Alerts alerts = new Alerts();
        this.getCommand(alerts.getCommand()).setExecutor((CommandExecutor)alerts);
        CPS cps = new CPS();
        this.getCommand(cps.getCommand()).setExecutor((CommandExecutor)cps);
        Setspawn setspawn = new Setspawn();
        this.getCommand(setspawn.getCommand()).setExecutor((CommandExecutor)setspawn);
        Staff staff = new Staff();
        this.getCommand(staff.getCommand()).setExecutor((CommandExecutor)staff);
        Freeze freeze = new Freeze();
        this.getCommand(freeze.getCommand()).setExecutor((CommandExecutor)freeze);
    }

    public void setupConfigManager() {
        try {
            this.file = new File(this.getDataFolder() + File.separator + "map.yml");
            this.yaml = YamlConfiguration.loadConfiguration((File)this.file);
            if (!this.file.exists()) {
                List configList = this.yaml.getStringList("MOTD.Text");
                configList.add("&aWelcome to &cMCHCF&a, %player%");
                configList.add("&aMap: &c#1");
                this.yaml.set("MOTD.Text", (Object)configList);
                this.yaml.set("WorldBorder", (Object)5000);
                this.yaml.set("MOTD.System", (Object)"&cThis is the default system motd.");
                this.yaml.set("MOTD.Slots", (Object)120);
                this.yaml.set("Spawn.World", (Object)"world");
                this.yaml.set("Spawn.X", (Object)0.5);
                this.yaml.set("Spawn.Y", (Object)78.0);
                this.yaml.set("Spawn.Z", (Object)0.5);
                this.yaml.set("End.World", (Object)"world");
                this.yaml.set("End.X", (Object)0.5);
                this.yaml.set("End.Y", (Object)78.0);
                this.yaml.set("End.Z", (Object)0.05);
                this.yaml.set("EndS.World", (Object)"world_the_end");
                this.yaml.set("EndS.X", (Object)-214.5);
                this.yaml.set("EndS.Y", (Object)162.5);
                this.yaml.set("EndS.Z", (Object)-193.5);
                this.yaml.set("Clicks", (Object)10);
                this.yaml.set("TSIP", (Object)"fgt");
                this.yaml.set("ScoreboardEnabled", (Object)true);
                this.yaml.set("scoreboardTitle", (Object)"fgt");
                this.yaml.save(this.file);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getMaxProt() {
        return this.maxProt;
    }

    public int getMaxFireProt() {
        return this.maxFireProt;
    }

    public int getMaxProjProt() {
        return this.maxProjProt;
    }

    public int getMaxThorns() {
        return this.maxThorns;
    }

    public int getMaxSharp() {
        return this.maxSharp;
    }

    public int getMaxFire() {
        return this.maxFire;
    }

    public int getMaxKB() {
        return this.maxKB;
    }

    public int getMaxBowPower() {
        return this.maxBowPower;
    }

    public int getMaxBowFire() {
        return this.maxBowFire;
    }

    public int getMaxBowInfinity() {
        return this.maxBowInfinity;
    }

    public int getMaxBowKB() {
        return this.maxBowKB;
    }

    public void fixInventory(Player player) {
        ItemStack[] inv = player.getInventory().getContents();
        int i = 0;
        while (i < inv.length) {
            if (inv[i] != null) {
                inv[i] = this.fixItem(inv[i]);
            }
            ++i;
        }
        player.getInventory().setContents(inv);
    }

    public void fixArmour(Player player) {
        if (player.getInventory().getHelmet() != null) {
            player.getInventory().setHelmet(this.fixItem(player.getInventory().getHelmet()));
        }
        if (player.getInventory().getChestplate() != null) {
            player.getInventory().setChestplate(this.fixItem(player.getInventory().getChestplate()));
        }
        if (player.getInventory().getLeggings() != null) {
            player.getInventory().setLeggings(this.fixItem(player.getInventory().getLeggings()));
        }
        if (player.getInventory().getBoots() != null) {
            player.getInventory().setBoots(this.fixItem(player.getInventory().getBoots()));
        }
    }

    public ItemStack fixItem(ItemStack item) {
        HashMap<Enchantment, Integer> enchants = this.fixEnchant(item.getEnchantments());
        for (Enchantment ench : item.getEnchantments().keySet()) {
            item.removeEnchantment(ench);
        }
        item.addEnchantments(enchants);
        return item;
    }

    public HashMap<Enchantment, Integer> fixEnchant(Map<Enchantment, Integer> enchantments) {
        HashMap<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
        for (Enchantment key : enchantments.keySet()) {
            enchants.put(key, enchantments.get((Object)key));
        }
        if (enchants.containsKey((Object)Enchantment.PROTECTION_ENVIRONMENTAL) && (Integer)enchants.get((Object)Enchantment.PROTECTION_ENVIRONMENTAL) > this.getMaxProt()) {
            enchants.remove((Object)Enchantment.PROTECTION_ENVIRONMENTAL);
            if (this.getMaxProt() > 0) {
                enchants.put(Enchantment.PROTECTION_ENVIRONMENTAL, this.getMaxProt());
            }
        }
        if (enchants.containsKey((Object)Enchantment.PROTECTION_FIRE) && (Integer)enchants.get((Object)Enchantment.PROTECTION_FIRE) > this.getMaxFireProt()) {
            enchants.remove((Object)Enchantment.PROTECTION_FIRE);
            if (this.getMaxFireProt() > 0) {
                enchants.put(Enchantment.PROTECTION_FIRE, this.getMaxFireProt());
            }
        }
        if (enchants.containsKey((Object)Enchantment.PROTECTION_PROJECTILE) && (Integer)enchants.get((Object)Enchantment.PROTECTION_PROJECTILE) > this.getMaxProjProt()) {
            enchants.remove((Object)Enchantment.PROTECTION_PROJECTILE);
            if (this.getMaxProjProt() > 0) {
                enchants.put(Enchantment.PROTECTION_PROJECTILE, this.getMaxProjProt());
            }
        }
        if (enchants.containsKey((Object)Enchantment.THORNS) && (Integer)enchants.get((Object)Enchantment.THORNS) > this.getMaxThorns()) {
            enchants.remove((Object)Enchantment.THORNS);
            if (this.getMaxThorns() > 0) {
                enchants.put(Enchantment.THORNS, this.getMaxThorns());
            }
        }
        if (enchants.containsKey((Object)Enchantment.DAMAGE_ALL) && (Integer)enchants.get((Object)Enchantment.DAMAGE_ALL) > this.getMaxSharp()) {
            enchants.remove((Object)Enchantment.DAMAGE_ALL);
            if (this.getMaxSharp() > 0) {
                enchants.put(Enchantment.DAMAGE_ALL, this.getMaxSharp());
            }
        }
        if (enchants.containsKey((Object)Enchantment.FIRE_ASPECT) && (Integer)enchants.get((Object)Enchantment.FIRE_ASPECT) > this.getMaxFire()) {
            enchants.remove((Object)Enchantment.FIRE_ASPECT);
            if (this.getMaxFire() > 0) {
                enchants.put(Enchantment.FIRE_ASPECT, this.getMaxFire());
            }
        }
        if (enchants.containsKey((Object)Enchantment.KNOCKBACK) && (Integer)enchants.get((Object)Enchantment.KNOCKBACK) > this.getMaxKB()) {
            enchants.remove((Object)Enchantment.KNOCKBACK);
            if (this.getMaxKB() > 0) {
                enchants.put(Enchantment.KNOCKBACK, this.getMaxKB());
            }
        }
        if (enchants.containsKey((Object)Enchantment.ARROW_DAMAGE) && (Integer)enchants.get((Object)Enchantment.ARROW_DAMAGE) > this.getMaxBowPower()) {
            enchants.remove((Object)Enchantment.ARROW_DAMAGE);
            if (this.getMaxBowPower() > 0) {
                enchants.put(Enchantment.ARROW_DAMAGE, this.getMaxBowPower());
            }
        }
        if (enchants.containsKey((Object)Enchantment.ARROW_FIRE) && (Integer)enchants.get((Object)Enchantment.ARROW_FIRE) > this.getMaxBowFire()) {
            enchants.remove((Object)Enchantment.ARROW_FIRE);
            if (this.getMaxBowFire() > 0) {
                enchants.put(Enchantment.ARROW_FIRE, this.getMaxBowFire());
            }
        }
        if (enchants.containsKey((Object)Enchantment.ARROW_INFINITE) && (Integer)enchants.get((Object)Enchantment.ARROW_INFINITE) > this.getMaxBowInfinity()) {
            enchants.remove((Object)Enchantment.ARROW_INFINITE);
            if (this.getMaxBowInfinity() > 0) {
                enchants.put(Enchantment.ARROW_INFINITE, this.getMaxBowInfinity());
            }
        }
        if (enchants.containsKey((Object)Enchantment.ARROW_KNOCKBACK) && enchants.get((Object)Enchantment.ARROW_KNOCKBACK) > this.getMaxBowKB()) {
            enchants.remove((Object)Enchantment.ARROW_KNOCKBACK);
            if (this.getMaxBowKB() > 0) {
                enchants.put(Enchantment.ARROW_KNOCKBACK, this.getMaxBowKB());
            }
        }
        return enchants;
    }

    public void setupCommands() {
        new ohho.plasmacore.commands.Staffchat();
        new ohho.plasmacore.commands.Togglechat();
        new ohho.plasmacore.commands.Clearchat();
        new ohho.plasmacore.commands.ToggleGlobalchat();
        new ohho.plasmacore.commands.Say();
        new ohho.plasmacore.commands.Lol();
    }

    public void setupListeners() {
        Bukkit.getPluginManager().registerEvents((Listener)new PlayerListener(this), (Plugin)this);
        new ohho.plasmacore.commands.Lock();
        Bukkit.getPluginManager().registerEvents((Listener)new EnderpearlListener(), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new SpawnTagListener(), (Plugin)this);
    }

    public ConfigManager getConfigManager() {
        return this.cManager;
    }

    public SignManager getSignManager() {
        return this.signManager;
    }

    public void setSignManager(SignManager signManager) {
        this.signManager = signManager;
    }

    public ServerHandler getServerHandler() {
        return this.serverHandler;
    }

    public static ApplicableRegionSet getRegionAt(Location loc) {
        return WGBukkit.getRegionManager((World)loc.getWorld()).getApplicableRegions(loc);
    }

    public static boolean correctRegion(Location loc) {
        ApplicableRegionSet set = WGBukkit.getRegionManager((World)loc.getWorld()).getApplicableRegions(loc);
        if (set.allows(DefaultFlag.PVP) && !set.allows(DefaultFlag.INVINCIBILITY)) {
            return true;
        }
        return false;
    }

    private void saveYaml() {
        try {
            this.yaml.save(this.file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ScoreboardHandler getScoreboardHandler() {
        return this.scoreboardHandler;
    }

    public PvPTimer getPvpTimer() {
        return this.pvpTimer;
    }

}
