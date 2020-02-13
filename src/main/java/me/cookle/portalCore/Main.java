package me.cookle.portalCore;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main
extends JavaPlugin {
    private Plugin plugin;

    public void onEnable() {
        plugin = this;
        this.getServer().getPluginManager().registerEvents(new PortalListener(this), plugin);
    }

    public void onDisable() {
        plugin.saveConfig();
    }

    @NotNull
    static Location getLocationFromString(String string) {
        String[] Cords = string.split(",");
        int x = Integer.parseInt(Cords[0]);
        int y = Integer.parseInt(Cords[1]);
        int z = Integer.parseInt(Cords[2]);
        World world = Bukkit.getWorld(Cords[3]);
        return new Location(world, x, y, z);
    }

    @NotNull
    static String getStringFromLocation(Location loc) {
        String cords = "";
        cords = cords + loc.getBlockX() + ",";
        cords = cords + loc.getBlockY() + ",";
        cords = cords + loc.getBlockZ() + ",";
        cords = cords + Objects.requireNonNull(loc.getWorld()).getName();
        return cords;
    }

    @Nullable
    private static String getBlockData(Block block) {
        StringBuilder blockData = new StringBuilder();

        if (block instanceof Container){
            Container container = (Container)block.getState();
            blockData.append("Inventory = ");
            for (ItemStack i : container.getInventory()) {
                if (i != null) {
                    blockData.append(i.getType().name()).append(":").append(i.getAmount()).append(",");
                    continue;
                }
                blockData.append("AIR:0,");
            }
        } else if (block instanceof Sign){
            Sign sign = (Sign)block.getState();
            blockData.append("Lines = ");
            blockData.append(String.join(",", sign.getLines()));
        }
        if (blockData.length() != 0) { return blockData.toString(); }
        return null;
    }

    @Nullable
    static List<String> getPortalID(Location location) {
        Location loc = location.clone().subtract(0.0, 1.0, 0.0);

        ArrayList<String> ID = new ArrayList<>();
        loc = loc.subtract(1.0, 0.0, 2.0);
        boolean encountered_block = false;

        for (int i = 0; i < 16; ++i) {
            String tileEntity = Main.getBlockData(loc.getBlock());

            if (tileEntity != null) {
                ID.add(loc.getBlock().getType().name() + ", [" + tileEntity + "]");
            }
            else {
                ID.add(loc.getBlock().getType().name());
                if (loc.getBlock().getType() != Material.AIR) { encountered_block = true; }
            }

            if (i < 3) { loc.add(1.0, 0.0, 0.0); }
            if (3 <= i & i < 7) { loc.add(0.0, 0.0, 1.0); }
            if (7 <= i & i < 11) { loc.subtract(1.0, 0.0, 0.0); }
            if (!(11 <= i & i < 15)) continue;loc.subtract(0.0, 0.0, 1.0); }
        if (encountered_block) { return ID; }
        return null;
    }
}

