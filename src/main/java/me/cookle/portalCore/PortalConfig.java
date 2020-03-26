package me.cookle.portalCore;

import org.bukkit.*;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PortalConfig {
    private File file;
    private String version;
    private Map<String, String> portals;
    private Map<String, String> locations;

    public PortalConfig(@NotNull PortalCore main) {
        Configuration config = main.getConfig();
        file = new File(main.getDataFolder(), "config.yml");

        String configVersion = config.getString("Version");
        version = main.getDescription().getVersion();

        locations = new HashMap<>();
        portals = new HashMap<>();

        ConfigurationSection locSec = config.getConfigurationSection("Locations");
        ConfigurationSection portSec = config.getConfigurationSection("Portals");

        if(locSec != null) locSec.getKeys(true).forEach(loc -> locations.put(loc, locSec.getString(loc)));
        if(portSec != null) portSec.getKeys(true).forEach(loc -> portals.put(loc, portSec.getString(loc)));

        if(configVersion == null) {
            updateConfig(config);
        }
    }

    public boolean setPortalLink(Location location) {
        // get identifier, if invalid return false
        List<String> identifier = getPortalID(location);
        if(identifier == null) return false;

        // gets the string versions for both the location and the identity of this portal
        String locationString = getStringFromLocation(location);
        String identifierString = identifier.toString();

        // if this portal has been linked before the old portal id will be removed
        String savedLocation = portals.get(identifierString);
        if(savedLocation != null) {
            // returns so an older portal isn't overriding by creating this one
            if(!savedLocation.equals(locationString)) return false;

            // this will override an old identifier for this portal if it's had a different one it the past
            String savedID = locations.get(savedLocation);
            if(savedID != null) portals.put(savedID, locationString);
        }


        // links the portal location and its id, if the portal has changed then a new link was made
        boolean changed = (portals.get(identifierString) == null || locations.get(locationString) == null);
        if(!changed) changed = (!portals.get(identifierString).equals(locationString));
        if(!changed) changed = (!locations.get(locationString).equals(identifierString));

        portals.put(identifierString, locationString);
        locations.put(locationString, identifierString);
        return changed;
    }

    public void deletePortalLink(Location location) {
        String locationString = getStringFromLocation(location);
        String identifierString = locations.get(locationString);

        locations.remove(locationString);
        if(identifierString != null) portals.remove(identifierString);
    }

    public Location getPortalLink(Location origin) {
        // in for loop which allows the player to have the end point higher in the sky for more flexibility
        for (int t = 1; t < 10; t++) {
            // get the identity of the location being tested, but if it's not valid then continue to the next
            List<String> otherID = getPortalID(origin.clone().add(0.0, t, 0.0));
            if (otherID == null) continue;

            // if the id is a valid one but doesn't exist then continue to the next possible id
            String destinationString = portals.get(otherID.toString());
            if (destinationString == null) continue;

            // if destination can't support a portal then remove it & continue to the next
            Location destination = getLocationFromString(destinationString);
            if(checkBeacon(destination)) {
                deletePortalLink(destination);
                continue;
            }

            // if destination no longer has a valid id then remove it & continue to the next
            List<String> destinationID = getPortalID(destination);
            if (destinationID == null) {
                deletePortalLink(destination);
                continue;
            }

            // if the destinations id has been updated then update & continue to the next
            if (!destinationID.equals(otherID)) {
                setPortalLink(destination);
                continue;
            }
            return destination;
        }
        return null;
    }

    public boolean checkBeacon(@NotNull Location location) {
        Block block = location.getBlock();
        if (block.getType() != Material.BEACON) return true;

        Beacon beacon = (Beacon) block.getState();
        return beacon.getTier() == 0;
    }

    private void updateConfig(@NotNull Configuration config) {
        Set<String> keys = config.getKeys(true);
        for(String portalKey : keys) {
            String portalLocation = config.getString(portalKey);
            if(portalLocation == null) continue;

            Location location = getLocationFromString(portalLocation);
            if(location.getBlock().getType() != Material.BEACON) continue;

            List<String> newPortalKey = getPortalID(location);
            if(newPortalKey == null) continue;

            portalKey = newPortalKey.toString();

            portals.put(portalKey, portalLocation);
            locations.put(portalLocation, portalKey);
        }
    }

    @NotNull
    private Location getLocationFromString(@NotNull String string) {
        String[] Cords = string.split(",");
        int x = Integer.parseInt(Cords[0]);
        int y = Integer.parseInt(Cords[1]);
        int z = Integer.parseInt(Cords[2]);
        World world = Bukkit.getWorld(Cords[3]);
        return new Location(world, x, y, z);
    }

    @NotNull
    private String getStringFromLocation(@NotNull Location loc) {
        String cords = "";
        cords = cords + loc.getBlockX() + ",";
        cords = cords + loc.getBlockY() + ",";
        cords = cords + loc.getBlockZ() + ",";
        cords = cords + Objects.requireNonNull(loc.getWorld()).getName();
        return cords;
    }

    @Nullable
    private String getBlockData(Block block) {
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
    private List<String> getPortalID(@NotNull Location location) {
        Location loc = location.clone().subtract(0.0, 1.0, 0.0);

        ArrayList<String> ID = new ArrayList<>();
        loc = loc.subtract(1.0, 0.0, 2.0);
        boolean encountered_block = false;

        for (int i = 0; i < 16; ++i) {
            String tileEntity = getBlockData(loc.getBlock());

            if (tileEntity != null) {
                ID.add(loc.getBlock().getType().name() + ", [" + tileEntity + "]");
            }
            else {
                ID.add(loc.getBlock().getType().name());
                if (loc.getBlock().getType() != Material.AIR) { encountered_block = true; }
            }

            if (i < 3) loc.add(1.0, 0.0, 0.0);
            if (3 <= i & i < 7) loc.add(0.0, 0.0, 1.0);
            if (7 <= i & i < 11) loc.subtract(1.0, 0.0, 0.0);
            if (!(11 <= i & i < 15)) continue;loc.subtract(0.0, 0.0, 1.0);
        }

        if (!encountered_block) { return null; }
        return ID;
    }

    public void save() {
        FileConfiguration configuration = new YamlConfiguration();
        configuration.set("Version", version);
        configuration.set("Portals", portals);
        configuration.set("Locations", locations);

        try {
            if(!file.exists()) { file.createNewFile(); }
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
