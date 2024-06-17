package com.csc.anticriminal;

import com.csc.anticriminal.event.GetPlayerChat;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiCriminal extends JavaPlugin {

    private static AntiCriminal instance;
    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        getLogger().info("Plugin Enabled");
        getServer().getPluginManager().registerEvents(new GetPlayerChat(), this);
    }

    public static AntiCriminal getInstance(){
        return instance;
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Plugin Disabled");

        // unban all player; debug
        /*
        BanList banList = Bukkit.getBanList(BanList.Type.IP);

        for (Object entryObject : banList.getBanEntries()) {
            if (entryObject instanceof BanEntry) {
                BanEntry entry = (BanEntry) entryObject;
                banList.pardon(entry.getTarget());
            }
        }
        */

    }
}

