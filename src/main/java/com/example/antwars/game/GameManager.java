package com.example.antwars.game;
import com.example.antwars.AntWarsPlugin; import com.example.antwars.kit.KitManager; import com.example.antwars.loot.LootManager;
import com.example.antwars.util.LocationUtil; import com.example.antwars.util.WorldBuilder; import com.example.antwars.team.TeamManager;
import org.bukkit.*; import org.bukkit.block.Chest; import org.bukkit.block.Block; import org.bukkit.block.BlockState;
import org.bukkit.entity.Player; import org.bukkit.event.*; import org.bukkit.event.entity.*; import org.bukkit.event.player.*; import org.bukkit.scheduler.BukkitRunnable; import org.bukkit.inventory.ItemStack;
import java.util.*; import java.util.stream.Collectors;
public class GameManager implements Listener {
    private final AntWarsPlugin plugin; private final TeamManager teamManager; private final KitManager kitManager; private final LootManager lootManager;
    private GameModeType mode=GameModeType.SOLO; private GameState state=GameState.WAITING; private final Set<UUID> playing=new HashSet<>(); private int aliveCount=0; private BukkitRunnable phaseTask;
    public GameManager(AntWarsPlugin p, TeamManager t, KitManager k, LootManager lm){ plugin=p; teamManager=t; kitManager=k; lootManager=lm; }
    public void setMode(GameModeType m){ if(state==GameState.WAITING) mode=m; } public boolean isPlaying(Player p){ return playing.contains(p.getUniqueId()); }
    public void join(Player p, GameModeType m){ if(state!=GameState.WAITING){ p.sendMessage(ChatColor.RED+"Game already running."); return; }
        setMode(m); playing.add(p.getUniqueId()); sendToLobby(p); p.sendMessage(ChatColor.GREEN+"Joined AntWars ("+mode+"). Wait for start.");
        int needed=(mode==GameModeType.SOLO?plugin.getConfig().getInt("modes.solo.maxPlayers"):plugin.getConfig().getInt("modes.team.teamCount")*plugin.getConfig().getInt("modes.team.teamSize"));
        if(playing.size()>=Math.max(2,Math.min(needed,12))){ new BukkitRunnable(){ int t=10; @Override public void run(){ if(state!=GameState.WAITING){ cancel(); return; } if(t==0){ startGather(); cancel(); return; } Bukkit.getOnlinePlayers().forEach(pl->pl.sendActionBar(ChatColor.YELLOW+"AntWars starts in "+t+"s")); t--; } }.runTaskTimer(plugin,0L,20L); }
    }
    public void leave(Player p){ playing.remove(p.getUniqueId()); teamManager.remove(p.getUniqueId()); p.sendMessage(ChatColor.YELLOW+"Left AntWars."); }
    public void forceStart(){ if(state==GameState.WAITING && playing.size()>=2) startGather(); }
    private void sendToLobby(Player p){ String raw=plugin.getConfig().getString("locations.lobby",null); if(raw==null){ p.sendMessage(ChatColor.RED+"Lobby not set."); return; } p.teleport(LocationUtil.fromString(raw)); }

    public void generateCellsCommand(int count, org.bukkit.command.CommandSender sender){
        var cfg=plugin.getConfig(); String name=cfg.getString("worlds.resource","antwars_resources"); World res=WorldBuilder.ensureWorld(name);
        int size=cfg.getInt("cells.size",3), wall=cfg.getInt("cells.wallHeight",2), spacing=cfg.getInt("cells.spacing",7), y=cfg.getInt("cells.startY",80);
        String floorStr=cfg.getString("cells.floorMaterial","OAK_PLANKS"), wallStr=cfg.getString("cells.wallMaterial","GLASS"); boolean doorway=cfg.getBoolean("cells.doorway",true);
        int ox=cfg.getInt("cells.origin.x",0), oz=cfg.getInt("cells.origin.z",0);
        if(cfg.getBoolean("autoResetResourceArea", true)) WorldBuilder.clearArea(res, count, size, wall, spacing, y, ox, oz);
        java.util.List<String> spawns=WorldBuilder.buildCells(res,count,size,wall,spacing,y,Material.matchMaterial(floorStr),Material.matchMaterial(wallStr),doorway,ox,oz);
        cfg.set("locations.spawnPoints", spawns); cfg.set("worlds.resource", name); plugin.saveConfig();
        placeRoofsAndChests(res, count, size, wall, spacing, y, ox, oz); // place roof & chests now
        sender.sendMessage(ChatColor.GREEN+"Generated "+spawns.size()+" cells in world '"+name+"'.");
    }

    private void startGather(){
        if(playing.isEmpty()) return; state=GameState.GATHER; teamManager.reset(); assignTeamsIfNeeded();
        var cfg=plugin.getConfig(); boolean auto=cfg.getBoolean("autoGenerateCells",true); String name=cfg.getString("worlds.resource","antwars_resources"); World res;
        res = WorldBuilder.ensureWorld(name);
        int size=cfg.getInt("cells.size",3), wall=cfg.getInt("cells.wallHeight",2), spacing=cfg.getInt("cells.spacing",7), y=cfg.getInt("cells.startY",80);
        String floorStr=cfg.getString("cells.floorMaterial","OAK_PLANKS"), wallStr=cfg.getString("cells.wallMaterial","GLASS"); boolean doorway=cfg.getBoolean("cells.doorway",true);
        int ox=cfg.getInt("cells.origin.x",0), oz=cfg.getInt("cells.origin.z",0);
        if(auto){
            int count=playing.size();
            if(cfg.getBoolean("autoResetResourceArea", true)) WorldBuilder.clearArea(res, count, size, wall, spacing, y, ox, oz);
            java.util.List<String> spawns=WorldBuilder.buildCells(res,count,size,wall,spacing,y,Material.matchMaterial(floorStr),Material.matchMaterial(wallStr),doorway,ox,oz);
            cfg.set("locations.spawnPoints", spawns); cfg.set("worlds.resource", name); plugin.saveConfig();
            placeRoofsAndChests(res, count, size, wall, spacing, y, ox, oz);
        }
        java.util.List<Location> spawns=plugin.getConfig().getStringList("locations.spawnPoints").stream().map(LocationUtil::fromString).collect(Collectors.toList());
        res=Bukkit.getWorld(plugin.getConfig().getString("worlds.resource", name)); if(res==null){ plugin.getLogger().warning("Resource world not found: "+name); res=Bukkit.getWorlds().get(0); }
        int i=0; for(UUID id:new HashSet<>(playing)){ Player p=Bukkit.getPlayer(id); if(p==null) continue; Location s=spawns.isEmpty()?res.getSpawnLocation():spawns.get(i%spawns.size()); p.teleport(s); p.getInventory().clear(); p.setGameMode(GameMode.SURVIVAL); kitManager.giveDefaultKit(p); i++; }
        aliveCount=playing.size(); int gatherSecs=plugin.getConfig().getInt("gatherDurationSeconds",600); boolean pvp=plugin.getConfig().getBoolean("pvpDuringGather",false); setPvp(pvp);
        phaseTask=new BukkitRunnable(){ int t=gatherSecs; @Override public void run(){ if(t%60==0 || t<=10) Bukkit.getOnlinePlayers().forEach(pl->pl.sendActionBar(ChatColor.AQUA+"Gather phase: "+t+"s")); if(t==0){ startDeathmatch(); cancel(); } t--; }}; phaseTask.runTaskTimer(plugin,0L,20L);
        Bukkit.broadcastMessage(ChatColor.GREEN+"AntWars: gather phase started for "+gatherSecs+"s.");
    }

    private void placeRoofsAndChests(World w, int count, int size, int wall, int spacing, int y, int ox, int oz){
        Material roofMat = Material.matchMaterial(plugin.getConfig().getString("cells.roofMaterial","BARRIER"));
        boolean enableChest = plugin.getConfig().getBoolean("loot.enableChest", true);
        int perRow=(int)Math.ceil(Math.sqrt(count));
        java.util.List<Block> roofBlocks = new java.util.ArrayList<>();
        for(int r=0, idx=0; r<perRow && idx<count; r++){
            for(int c=0; c<perRow && idx<count; c++, idx++){
                int baseX=ox + c*(size+spacing); int baseZ=oz + r*(size+spacing);
                // roof (flat size x size) at y + wall + 1
                int roofY = y + wall + 1;
                for(int x=0;x<size;x++) for(int z=0;z<size;z++){ Block b = w.getBlockAt(baseX+x, roofY, baseZ+z); b.setType(roofMat,false); roofBlocks.add(b); }
                // chest inside cell near north wall (z+1)
                if (enableChest){
                    int cx = baseX + size/2; int cy = y + 1; int cz = baseZ + 1;
                    Block chestBlock = w.getBlockAt(cx, cy, cz);
                    chestBlock.setType(Material.CHEST, false);
                    BlockState st = chestBlock.getState();
                    if (st instanceof Chest chest){
                        // fill
                        java.util.List<ItemStack> items = lootManager.rollItems();
                        for (ItemStack it : items) chest.getBlockInventory().addItem(it);
                        st.update(true, false);
                    }
                }
            }
        }
        // schedule roof removal
        int secs = Math.max(0, plugin.getConfig().getInt("cells.roofDespawnSeconds", 5));
        if (secs > 0){
            new BukkitRunnable(){ @Override public void run(){ for(Block b : roofBlocks) if (b.getType() != Material.AIR) b.setType(Material.AIR, false); } }.runTaskLater(plugin, secs * 20L);
        }
    }

    private void startDeathmatch(){ state=GameState.DEATHMATCH; setPvp(true);
        String name=plugin.getConfig().getString("worlds.deathmatch","world"); World dm=Bukkit.getWorld(name); if(dm==null) dm=Bukkit.getWorlds().get(0);
        String raw=plugin.getConfig().getString("locations.deathmatchCenter",null); Location center=raw==null?dm.getSpawnLocation():LocationUtil.fromString(raw);
        int radius=plugin.getConfig().getInt("border.deathmatchRadius",50); WorldBorder border=dm.getWorldBorder(); border.setCenter(center); border.setSize(radius*2.0);
        for(UUID id:new HashSet<>(playing)){ Player p=Bukkit.getPlayer(id); if(p!=null){ p.teleport(center.clone().add((Math.random()-0.5)*radius,0,(Math.random()-0.5)*radius)); p.sendMessage(ChatColor.RED+"Deathmatch! PVP enabled."); } }
        int after=plugin.getConfig().getInt("border.shrinkAfterSeconds",120), to=plugin.getConfig().getInt("border.shrinkToRadius",10)*2, time=plugin.getConfig().getInt("border.shrinkTimeSeconds",90);
        new BukkitRunnable(){ @Override public void run(){ border.setSize(to,time); Bukkit.broadcastMessage(ChatColor.YELLOW+"Border is shrinking!"); }}.runTaskLater(plugin, after*20L);
    }

    private void setPvp(boolean v){ for(World w:Bukkit.getWorlds()) w.setPVP(v); }
    public void shutdown(){ if(phaseTask!=null) phaseTask.cancel(); state=GameState.ENDED; playing.clear(); teamManager.reset(); }
    private void assignTeamsIfNeeded(){ if(mode==GameModeType.SOLO) return; int teamCount=plugin.getConfig().getInt("modes.team.teamCount",6); java.util.List<UUID> list=new java.util.ArrayList<>(playing); java.util.Collections.shuffle(list);
        for(int i=0;i<list.size();i++){ int idx=i%teamCount; teamManager.addToTeam(idx, list.get(i)); } teamManager.setFriendlyFire(plugin.getConfig().getBoolean("friendlyFire",false)); }
    @EventHandler public void onDamage(EntityDamageByEntityEvent e){ if(state==GameState.GATHER && !plugin.getConfig().getBoolean("pvpDuringGather",false)) e.setCancelled(true);
        if(mode==GameModeType.TEAM && e.getEntity() instanceof Player v && e.getDamager() instanceof Player a){ if(!teamManager.isFriendlyFire() && teamManager.sameTeam(v.getUniqueId(), a.getUniqueId())) e.setCancelled(true); } }
    @EventHandler public void onDeath(PlayerDeathEvent e){ Player p=e.getEntity(); if(!isPlaying(p)) return; aliveCount--; playing.remove(p.getUniqueId()); teamManager.remove(p.getUniqueId());
        Bukkit.broadcastMessage(ChatColor.GRAY+p.getName()+" has fallen. Alive: "+aliveCount); if(aliveCount<=1) endGame(); }
    private void endGame(){ state=GameState.ENDED; String winners="Unknown"; if(mode==GameModeType.SOLO){ for(UUID id:playing){ Player p=Bukkit.getPlayer(id); if(p!=null){ winners=p.getName(); break; } } } else winners=teamManager.getWinningTeamName();
        Bukkit.broadcastMessage(ChatColor.GOLD+"AntWars ended! Winner: "+winners); String raw=plugin.getConfig().getString("locations.lobby",null); Location lobby=raw==null?Bukkit.getWorlds().get(0).getSpawnLocation():LocationUtil.fromString(raw);
        for(Player p:Bukkit.getOnlinePlayers()) if(p.isOnline()) p.teleport(lobby); playing.clear(); teamManager.reset(); state=GameState.WAITING; }
    @EventHandler public void onJoin(PlayerJoinEvent e){ e.getPlayer().setGameMode(GameMode.SURVIVAL); }
    @EventHandler public void onQuit(PlayerQuitEvent e){ playing.remove(e.getPlayer().getUniqueId()); teamManager.remove(e.getPlayer().getUniqueId()); }
}