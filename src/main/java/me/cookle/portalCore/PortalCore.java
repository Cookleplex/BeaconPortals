package me.cookle.portalCore;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PortalCore extends JavaPlugin {
    private PortalConfig config;

    public void onEnable() {
        config = new PortalConfig(this);

        this.getServer().getPluginManager().registerEvents(new PortalListener(config), this);

        // auto saves config ever 80 seconds with a start delay of 5 seconds
        new BukkitRunnable()
        {
            public void run()
            {
                config.save();
            }
        }.runTaskTimer(this, 200, 1600);
    }

    public void onDisable() {
        config.save();
    }
}

