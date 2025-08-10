package com.example.antwars.kit;
import com.example.antwars.AntWarsPlugin; import org.bukkit.*; import org.bukkit.enchantments.Enchantment; import org.bukkit.entity.Player; import org.bukkit.inventory.ItemStack;
import java.io.InputStreamReader; import java.util.List;
public class KitManager {
    private final AntWarsPlugin plugin; private final org.bukkit.configuration.file.FileConfiguration kits;
    public KitManager(AntWarsPlugin plugin){ this.plugin=plugin; this.kits=org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("kits.yml"))); }
    public void giveDefaultKit(Player p){ String name=kits.getString("defaultKit","miner"); give(p,name); }
    public void give(Player p,String kitName){ var sec=kits.getConfigurationSection("kits."+kitName); if(sec==null){ p.sendMessage(ChatColor.RED+"Kit not found: "+kitName); return; }
        p.getInventory().clear(); setArmor(p,sec.getString("helmet"),sec.getString("chestplate"),sec.getString("leggings"),sec.getString("boots"));
        for(String line: sec.getStringList("items")){ ItemStack item=parseItem(line); if(item!=null) p.getInventory().addItem(item); } }
    private void setArmor(Player p,String h,String c,String l,String b){ if(h!=null)p.getInventory().setHelmet(new ItemStack(Material.matchMaterial(h)));
        if(c!=null)p.getInventory().setChestplate(new ItemStack(Material.matchMaterial(c))); if(l!=null)p.getInventory().setLeggings(new ItemStack(Material.matchMaterial(l)));
        if(b!=null)p.getInventory().setBoots(new ItemStack(Material.matchMaterial(b))); }
    private ItemStack parseItem(String s){ try{ String[] parts=s.split(":"); Material mat=Material.matchMaterial(parts[0]); int amt=parts.length>=2?Integer.parseInt(parts[1]):1;
        ItemStack stack=new ItemStack(mat,amt); if(parts.length>=3) for(int i=2;i<parts.length;i++){ String[] e=parts[i].split(","); if(e.length==2){ Enchantment ench=Enchantment.getByName(e[0].toUpperCase()); int lvl=Integer.parseInt(e[1]); if(ench!=null) stack.addUnsafeEnchantment(ench,lvl);} }
        return stack; } catch(Exception ex){ plugin.getLogger().warning("Failed to parse kit item: "+s+" ("+ex.getMessage()+")"); return null; } }
}