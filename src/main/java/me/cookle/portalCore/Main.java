/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Server
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockState
 *  org.bukkit.block.Chest
 *  org.bukkit.block.Dispenser
 *  org.bukkit.block.Dropper
 *  org.bukkit.block.Furnace
 *  org.bukkit.block.Hopper
 *  org.bukkit.block.Sign
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.event.Listener
 *  org.bukkit.inventory.FurnaceInventory
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 *  org.bukkit.plugin.java.JavaPlugin
 */
package me.cookle.portalCore;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main
extends JavaPlugin {
    private static Plugin plugin;

    public void onEnable() {
        plugin = this;
        this.getServer().getPluginManager().registerEvents(new PortalListener(), plugin);
    }

    public void onDisable() {
        plugin.saveConfig();
    }

    static Location getLocationFromString(String string) {
        String[] Cords = string.split(",");
        int x = Integer.parseInt(Cords[0]);
        int y = Integer.parseInt(Cords[1]);
        int z = Integer.parseInt(Cords[2]);
        World world = Bukkit.getWorld((String)Cords[3]);
        return new Location(world, (double)x, (double)y, (double)z);
    }

    static String getStringFromLocation(Location loc) {
        String cords = "";
        cords = cords + loc.getBlockX() + ",";
        cords = cords + loc.getBlockY() + ",";
        cords = cords + loc.getBlockZ() + ",";
        cords = cords + Objects.requireNonNull(loc.getWorld()).getName();
        return cords;
    }

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
        if (blockData.length() != 0) {
            return blockData.toString();
        }
        return null;

//        switch (block.getType()){
//            case CHEST:
//            case TRAPPED_CHEST:
//            case FURNACE:
//            case HOPPER:
//            case DROPPER:
//            case DISPENSER:
//            case SHULKER_BOX:
//                Container container = (Container)block.getState();
//                blockData.append("Inventory = ");
//                for (ItemStack i : container.getInventory()) {
//                    if (i != null) {
//                        blockData.append(i.getType().name()).append(":").append(i.getAmount()).append(",");
//                        continue;
//                    }
//                    blockData.append("AIR:0,");
//                }
//                break;
//
//            case OAK_SIGN:
//            case BIRCH_SIGN:
//            case SPRUCE_SIGN:
//            case JUNGLE_SIGN:
//            case DARK_OAK_SIGN:
//            case ACACIA_SIGN:
//                Sign sign = (Sign)block.getState();
//                blockData.append("Lines = ");
//                blockData.append(String.join(",", sign.getLines()));
//                break;
//            default:
//                return null; // early return, no
//        }
//        if (blockData.length() != 0) {
//            return blockData.toString();
//        }
//        return null;
    }

    static List<String> getPortalID(Location loc) {
        ArrayList<String> ID = new ArrayList<String>();
        loc = loc.subtract(1.0, 0.0, 2.0);
        for (int i = 0; i < 16; ++i) {
            String tileEntity = Main.getBlockData(loc.getBlock());
            if (tileEntity != null) {
                ID.add(loc.getBlock().getType().name() + ", [" + tileEntity + "]");
            } else {
                ID.add(loc.getBlock().getType().name());
            }
            if (i < 3) {
                loc.add(1.0, 0.0, 0.0);
            }
            if (3 <= i & i < 7) {
                loc.add(0.0, 0.0, 1.0);
            }
            if (7 <= i & i < 11) {
                loc.subtract(1.0, 0.0, 0.0);
            }
            if (!(11 <= i & i < 15)) continue;
            loc.subtract(0.0, 0.0, 1.0);
        }
        return ID;
    }

    static FileConfiguration getCustomConfig() {
        return plugin.getConfig();
    }
}

