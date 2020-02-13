package me.cookle.portalCore;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public class PortalListener implements Listener {
    private Plugin plugin;

    PortalListener(Main main) {
        plugin = main;
    }

    @EventHandler
    public void onPlayerToggleSneakEvent(@NotNull PlayerToggleSneakEvent event) {

        Player player = event.getPlayer();
        if (player.isSneaking()) return;

        Block block = player.getLocation().subtract(0.0, 1.0, 0.0).getBlock();
        if (block.getType() != Material.BEACON) return;

        Beacon beacon = (Beacon) block.getState();
        if (beacon.getTier() == 0) return;

        Location originLocation = block.getLocation();
        List<String> thisID = Main.getPortalID(originLocation);
        if (thisID == null) return;

        World world = player.getWorld();

        if (!plugin.getConfig().contains(thisID.toString())) {
            plugin.getConfig().set(thisID.toString(), Main.getStringFromLocation(originLocation));
            world.playSound(originLocation, Sound.ITEM_FLINTANDSTEEL_USE, 16, 1);
        }

        for (int t = 1; t < 10; t++) {
            List<String> otherID = Main.getPortalID(originLocation.clone().add(0.0, t, 0.0));
            if (otherID == null) continue;

            String strLoc = plugin.getConfig().getString(otherID.toString());
            if (strLoc == null) continue;

            Location destinationLocation = Main.getLocationFromString(strLoc);
            Block destinationBlock = destinationLocation.getBlock();

            if (destinationBlock.getType() != Material.BEACON) {
                plugin.getConfig().set(otherID.toString(), null);
                continue;
            }

            Beacon destinationBeacon = (Beacon)destinationBlock.getState();
            if (destinationBeacon.getTier() == 0) continue;

            List<String> destinationID = Main.getPortalID(destinationLocation);
            if (destinationID == null) {
                plugin.getConfig().set(otherID.toString(), null);
                continue;
            }

            if (!destinationID.equals(otherID)) {
                plugin.getConfig().set(otherID.toString(), null);
                plugin.getConfig().set(destinationID.toString(), Main.getStringFromLocation(destinationLocation));
                continue;
            }

            Location locationOffset = destinationLocation.subtract(originLocation);

            Collection<Mob> leashedEntities = world.getNearbyEntities(
                    BoundingBox.of(
                            originLocation, 3,3,3
                    ))
                    .parallelStream().filter(entity -> entity instanceof Mob)
                    .map(entity -> (Mob)entity)
                    .filter(mob-> mob.isLeashed() && mob.getLeashHolder().equals(player))
                    .collect(Collectors.toSet());

            world.playSound(originLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 16, 16);

            Location playerDestination = player.getLocation();
            playerDestination.add(locationOffset);
            player.teleport(playerDestination);
            world.playSound(playerDestination, Sound.ENTITY_ENDERMAN_TELEPORT, 16, 16);

            leashedEntities.forEach(mob -> {
                Location mobLocation = mob.getLocation();
                Location mobDestination = mobLocation.add(locationOffset);
                mob.teleport(mobDestination);
                mob.setLeashHolder(player);
            });
            break;
        }
    }
}

