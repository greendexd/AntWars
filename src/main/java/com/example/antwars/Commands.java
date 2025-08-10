package com.example.antwars;
import com.example.antwars.game.GameManager;
import com.example.antwars.game.GameModeType;
import com.example.antwars.util.LocationUtil;
import com.example.antwars.team.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*; import org.bukkit.entity.Player;
public class Commands implements CommandExecutor {
    private final AntWarsPlugin plugin; private final GameManager game; private final TeamManager teams;
    public Commands(AntWarsPlugin p, GameManager g, TeamManager t){ plugin=p; game=g; teams=t; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length==0){ sender.sendMessage(ChatColor.YELLOW+"AntWars: join, leave, start, mode, setlobby, setdm, addspawn, setresourceworld, setdmworld, setgather, setteams, gencells"); return true; }
        String sub=args[0].toLowerCase();
        switch(sub){
            case "join"-> { if(!(sender instanceof Player p)){ sender.sendMessage("Players only."); return true; }
                String mode=args.length>=2?args[1]:"solo"; game.join(p, mode.equalsIgnoreCase("team")?GameModeType.TEAM:GameModeType.SOLO); return true; }
            case "leave"-> { if(!(sender instanceof Player p)){ sender.sendMessage("Players only."); return true; } game.leave(p); return true; }
            case "start"-> { game.forceStart(); sender.sendMessage(ChatColor.GREEN+"Force start triggered."); return true; }
            case "mode"-> { if(args.length<2){ sender.sendMessage("Usage: /antwars mode <solo|team>"); return true; }
                GameModeType type=args[1].equalsIgnoreCase("team")?GameModeType.TEAM:GameModeType.SOLO; game.setMode(type);
                sender.sendMessage(ChatColor.GREEN+"Mode set to "+type.name()); return true; }
            case "setlobby"-> { if(!(sender instanceof Player p)){ sender.sendMessage("Players only."); return true; }
                Location loc=p.getLocation(); plugin.getConfig().set("locations.lobby", LocationUtil.toString(loc)); plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN+"Lobby set."); return true; }
            case "setdm"-> { if(!(sender instanceof Player p)){ sender.sendMessage("Players only."); return true; }
                Location loc=p.getLocation(); plugin.getConfig().set("locations.deathmatchCenter", LocationUtil.toString(loc)); plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN+"Deathmatch center set."); return true; }
            case "addspawn"-> { if(!(sender instanceof Player p)){ sender.sendMessage("Players only."); return true; }
                Location loc=p.getLocation(); var list=plugin.getConfig().getStringList("locations.spawnPoints"); list.add(LocationUtil.toString(loc));
                plugin.getConfig().set("locations.spawnPoints", list); plugin.saveConfig(); sender.sendMessage(ChatColor.GREEN+"Spawn point added. Total: "+list.size()); return true; }
            case "setresourceworld"-> { if(args.length<2){ sender.sendMessage("Usage: /antwars setresourceworld <world>"); return true; }
                plugin.getConfig().set("worlds.resource", args[1]); plugin.saveConfig(); sender.sendMessage(ChatColor.GREEN+"Resource world set to "+args[1]); return true; }
            case "setdmworld"-> { if(args.length<2){ sender.sendMessage("Usage: /antwars setdmworld <world>"); return true; }
                plugin.getConfig().set("worlds.deathmatch", args[1]); plugin.saveConfig(); sender.sendMessage(ChatColor.GREEN+"Deathmatch world set to "+args[1]); return true; }
            case "setgather"-> { if(args.length<2){ sender.sendMessage("Usage: /antwars setgather <seconds>"); return true; }
                int secs=Integer.parseInt(args[1]); plugin.getConfig().set("gatherDurationSeconds", secs); plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN+"Gather duration set to "+secs+"s"); return true; }
            case "setteams"-> { if(args.length<3){ sender.sendMessage("Usage: /antwars setteams <count> <size>"); return true; }
                int count=Integer.parseInt(args[1]); int size=Integer.parseInt(args[2]);
                plugin.getConfig().set("modes.team.teamCount", count); plugin.getConfig().set("modes.team.teamSize", size); plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN+"Teams set: "+count+"x"+size); return true; }
            case "gencells"-> { int count=12; if(args.length>=2) try{ count=Integer.parseInt(args[1]); }catch(Exception ignored){} game.generateCellsCommand(count, sender); return true; }
            default -> sender.sendMessage(ChatColor.RED+"Unknown subcommand.");
        } return true;
    }
}