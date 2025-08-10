package com.example.antwars.loot;
import com.example.antwars.AntWarsPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import java.io.InputStreamReader;
import java.util.*;
public class LootManager {
    private final AntWarsPlugin plugin;
    private final YamlConfiguration loot;
    public LootManager(AntWarsPlugin plugin){ this.plugin = plugin; this.loot = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("loot.yml"))); }
    public int rollsPerChest(){ return plugin.getConfig().getInt("loot.rollsPerChest", 5); }
    public List<ItemStack> rollItems(){
        String poolName = plugin.getConfig().getString("loot.pool", "default");
        ConfigurationSection pool = loot.getConfigurationSection("pools."+poolName);
        if (pool == null) return Collections.emptyList();
        List<Map<String,Object>> entries = new ArrayList<>();
        for (String key : pool.getKeys(false)){
            entries.add(pool.getConfigurationSection(key).getValues(false));
        }
        // Build weighted list
        List<String> items = new ArrayList<>();
        for (Map<String,Object> e : entries){
            String spec = (String)e.get("item");
            int weight = ((Number)e.getOrDefault("weight", 1)).intValue();
            for (int i=0;i<weight;i++) items.add(spec);
        }
        Random rnd = new Random();
        List<ItemStack> out = new ArrayList<>();
        int rolls = rollsPerChest();
        for (int i=0;i<rolls && !items.isEmpty(); i++){
            String spec = items.get(rnd.nextInt(items.size()));
            ItemStack it = parse(spec);
            if (it != null) out.add(it);
        }
        return out;
    }
    private ItemStack parse(String spec){
        try{
            String[] p = spec.split(":");
            Material m = Material.matchMaterial(p[0]);
            if (m == null) return null;
            int amt = 1;
            if (p.length >= 2){
                String q = p[1];
                if (q.contains("-")){
                    String[] ab = q.split("-");
                    int a = Integer.parseInt(ab[0]), b = Integer.parseInt(ab[1]);
                    if (b < a){ int t=a; a=b; b=t; }
                    amt = a + new Random().nextInt(b - a + 1);
                } else amt = Integer.parseInt(q);
            }
            return new ItemStack(m, Math.max(1, Math.min(64, amt)));
        }catch(Exception ex){
            plugin.getLogger().warning("Bad loot item: "+spec+" ("+ex.getMessage()+")"); return null;
        }
    }
}