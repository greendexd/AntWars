package com.example.antwars;
import com.example.antwars.game.GameManager;
import com.example.antwars.kit.KitManager;
import com.example.antwars.team.TeamManager;
import com.example.antwars.loot.LootManager;
import org.bukkit.plugin.java.JavaPlugin;
public class AntWarsPlugin extends JavaPlugin {
    private static AntWarsPlugin instance;
    private GameManager gameManager; private KitManager kitManager; private TeamManager teamManager; private LootManager lootManager;
    @Override public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("kits.yml", false);
        saveResource("loot.yml", false);
        this.kitManager = new KitManager(this);
        this.teamManager = new TeamManager();
        this.lootManager = new LootManager(this);
        this.gameManager = new GameManager(this, teamManager, kitManager, lootManager);
        getCommand("antwars").setExecutor(new Commands(this, gameManager, teamManager));
        getServer().getPluginManager().registerEvents(gameManager, this);
        getLogger().info("AntWars enabled v1.2.0 (cells+roof+loot).");
    }
    @Override public void onDisable() { if (gameManager != null) gameManager.shutdown(); getLogger().info("AntWars disabled."); }
    public static AntWarsPlugin get() { return instance; }
}