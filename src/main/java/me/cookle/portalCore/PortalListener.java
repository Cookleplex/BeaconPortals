package me.cookle.portalCore;

import java.util.Collection;
import java.util.stream.Collectors;

import org.bukkit.*;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public class PortalListener implements Listener {
    private PortalConfig config;

    PortalListener(PortalConfig portalConfig) {
        config = portalConfig;
    }

    @EventHandler
    public void onPlayerToggleSneakEvent(@NotNull PlayerToggleSneakEvent event) {

        Player player = event.getPlayer();
        if (player.isSneaking()) return;

        Location origin = player.getLocation().subtract(0, 1, 0);
        if(config.checkBeacon(origin)) return;

        World originWorld = player.getWorld();
        if(config.setPortalLink(origin)) originWorld.spawnParticle(Particle.LAVA, player.getLocation(), 5);

        Location destination = config.getPortalLink(origin);
        if(destination == null) return;

        Collection<Mob> leashedEntities = originWorld.getNearbyEntities(
                BoundingBox.of(
                        origin, 5,5,5
                ))
                .parallelStream().filter(entity -> entity instanceof Mob)
                .map(entity -> (Mob)entity)
                .filter(mob -> mob.isLeashed() && mob.getLeashHolder().equals(player))
                .collect(Collectors.toSet());

        originWorld.playSound(origin, Sound.ENTITY_ENDERMAN_TELEPORT, 16, 16);
        originWorld.spawnParticle(Particle.FLASH, origin, 10, 0.2, 2, 0.2);

        World destinationWorld = destination.getWorld();
        if(destinationWorld == null) return;

        origin.setWorld(destinationWorld);
        Location blockOffset = origin.clone().subtract(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());

        Location locationOffset = destination.subtract(origin).add(blockOffset);

        Location playerDestination = player.getLocation();
        playerDestination.setWorld(destinationWorld);
        playerDestination.add(locationOffset);
        player.teleport(playerDestination);
        destinationWorld.playSound(playerDestination, Sound.ENTITY_ENDERMAN_TELEPORT, 16, 16);
        destinationWorld.spawnParticle(Particle.FLASH, playerDestination, 20, 0.2, 1, 0.2);

        leashedEntities.forEach(mob -> {
            Location mobLocation = mob.getLocation();
            Location mobDestination = mobLocation.add(locationOffset);
            mob.teleport(mobDestination);
            destinationWorld.spawnParticle(Particle.FLASH, mobDestination, 20, 0.2, 1, 0.2);
            mob.setLeashHolder(player);
        });

        player.setSneaking(false);
    }
}

