package com.example.antwars.team;
import org.bukkit.ChatColor; import java.util.*;
public class TeamManager {
    private final Map<Integer, Set<UUID>> teams=new HashMap<>(); private final Map<UUID,Integer> byPlayer=new HashMap<>(); private boolean friendlyFire=false;
    public void reset(){ teams.clear(); byPlayer.clear(); }
    public void addToTeam(int index,UUID p){ teams.computeIfAbsent(index,k->new HashSet<>()).add(p); byPlayer.put(p,index); }
    public void remove(UUID p){ Integer idx=byPlayer.remove(p); if(idx!=null){ Set<UUID> set=teams.get(idx); if(set!=null) set.remove(p);} }
    public boolean sameTeam(UUID a,UUID b){ Integer ia=byPlayer.get(a), ib=byPlayer.get(b); return ia!=null && ia.equals(ib); }
    public String getWinningTeamName(){ Optional<Map.Entry<Integer,Set<UUID>>> w=teams.entrySet().stream().filter(e->!e.getValue().isEmpty()).findFirst(); return w.map(e->ChatColor.AQUA+"Team "+(e.getKey()+1)).orElse("No team"); }
    public void setFriendlyFire(boolean v){ this.friendlyFire=v; } public boolean isFriendlyFire(){ return friendlyFire; }
}