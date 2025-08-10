package com.example.antwars.util;
import org.bukkit.Bukkit; import org.bukkit.Location; import org.bukkit.World;
public class LocationUtil {
    public static String toString(Location loc) { return loc.getWorld().getName()+","+loc.getX()+","+loc.getY()+","+loc.getZ()+","+loc.getYaw()+","+loc.getPitch(); }
    public static Location fromString(String s) {
        String[] p=s.split(","); World w=Bukkit.getWorld(p[0]);
        double x=Double.parseDouble(p[1]), y=Double.parseDouble(p[2]), z=Double.parseDouble(p[3]);
        float yaw=p.length>=5?Float.parseFloat(p[4]):0f, pitch=p.length>=6?Float.parseFloat(p[5]):0f;
        return new Location(w,x,y,z,yaw,pitch);
    }
}