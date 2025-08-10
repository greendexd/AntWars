package com.example.antwars.util;
import org.bukkit.*; import org.bukkit.block.Block;
import java.util.*; 
public class WorldBuilder {
    public static World ensureWorld(String name){ World w=Bukkit.getWorld(name); if(w!=null) return w; WorldCreator wc=new WorldCreator(name); wc.environment(World.Environment.NORMAL); wc.type(WorldType.NORMAL); return wc.createWorld(); }
    public static void clearArea(World w, int count, int size, int wallHeight, int spacing, int startY, int originX, int originZ){
        int perRow=(int)Math.ceil(Math.sqrt(count));
        for(int r=0;r<perRow;r++){ for(int c=0;c<perRow;c++){
            int baseX=originX + c*(size+spacing); int baseZ=originZ + r*(size+spacing);
            for(int x=-1;x<=size;x++){ for(int z=-1;z<=size;z++){
                for(int y=0;y<=wallHeight+5;y++){ w.getBlockAt(baseX+x,startY+y,baseZ+z).setType(Material.AIR,false); }
            }}
        }}
    }
    public static java.util.List<String> buildCells(World w,int count,int size,int wallHeight,int spacing,int startY,Material floorMat,Material wallMat,boolean doorway,int originX,int originZ){
        java.util.List<String> spawns=new ArrayList<>(); int perRow=(int)Math.ceil(Math.sqrt(count)); int idx=0;
        for(int r=0;r<perRow && idx<count;r++){ for(int c=0;c<perRow && idx<count;c++){
            int baseX=originX + c*(size+spacing); int baseZ=originZ + r*(size+spacing);
            for(int x=0;x<size;x++){ for(int z=0;z<size;z++){
                Block b=w.getBlockAt(baseX+x,startY,baseZ+z); b.setType(floorMat,false);
                for(int y=1;y<=wallHeight+3;y++) w.getBlockAt(baseX+x,startY+y,baseZ+z).setType(Material.AIR,false);
            }}
            for(int h=1;h<=wallHeight;h++){ for(int x=0;x<size;x++){
                w.getBlockAt(baseX+x,startY+h,baseZ).setType(wallMat,false);
                w.getBlockAt(baseX+x,startY+h,baseZ+size-1).setType(wallMat,false);
            } for(int z=0;z<size;z++){
                w.getBlockAt(baseX,startY+h,baseZ+z).setType(wallMat,false);
                w.getBlockAt(baseX+size-1,startY+h,baseZ+z).setType(wallMat,false);
            }}
            if(doorway && size>=3){ int doorX=baseX+size/2; int doorZ=baseZ+size-1; for(int h=1;h<=Math.min(2,wallHeight);h++) w.getBlockAt(doorX,startY+h,doorZ).setType(Material.AIR,false); }
            double sx=baseX+(size/2.0), sz=baseZ+(size/2.0), sy=startY+1.0; 
            spawns.add(com.example.antwars.util.LocationUtil.toString(new Location(w,sx,sy,sz,0f,0f))); 
            idx++;
        }}
        return spawns;
    }
}