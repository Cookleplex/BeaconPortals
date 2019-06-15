/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.block.Beacon
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockState
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerToggleSneakEvent
 */
package me.cookle.portalCore;

import java.util.List;
import me.cookle.portalCore.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PortalListener
implements Listener {
    @EventHandler
    public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event) {
        Beacon beacon;
        Player p = event.getPlayer();
        Block block = p.getLocation().subtract(0.0, 1.0, 0.0).getBlock();
        if (block.getType() == Material.BEACON && !p.isSneaking() && (beacon = (Beacon)block.getState()).getTier() > 0) {
            List<String> meID = Main.getPortalID(block.getLocation().clone().subtract(0.0, 1.0, 0.0));
            if (!Main.getCustomConfig().contains(meID.toString()) && !meID.toString().equalsIgnoreCase("[AIR, AIR, AIR, AIR, AIR, AIR, AIR, AIR, AIR, AIR, AIR, AIR, AIR, AIR, AIR, AIR]")) {
                Main.getCustomConfig().set(meID.toString(), (Object)Main.getStringFromLocation(block.getLocation()));
            }
            for (int t = 0; t < 10; ++t) {
                List<String> toID = Main.getPortalID(block.getLocation().clone().add(0.0, (double)t, 0.0));
                String strLoc = Main.getCustomConfig().getString(toID.toString());
                if (strLoc == null) continue;
                Location portalTo = Main.getLocationFromString(strLoc);
                Block b = portalTo.getBlock();
                if (b.getType() == Material.BEACON && Main.getPortalID(portalTo.clone().subtract(0.0, 1.0, 0.0)).equals(toID)) {
                    beacon = (Beacon)b.getState();
                    if (beacon.getTier() > 0) {
                        Location pLoc = p.getLocation().clone();
                        portalTo.setPitch(pLoc.getPitch());
                        portalTo.setYaw(pLoc.getYaw());
                        p.teleport(portalTo.add(pLoc.getX() - (double)pLoc.getBlockX(), 1.0, pLoc.getZ() - (double)pLoc.getBlockZ()));
                        break;
                    }
                    Main.getCustomConfig().set(toID.toString(), null);
                    continue;
                }
                Main.getCustomConfig().set(toID.toString(), null);
            }
        }
    }
}

