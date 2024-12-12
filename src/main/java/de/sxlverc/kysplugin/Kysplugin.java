package de.sxlverc.kysplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class Kysplugin extends JavaPlugin {

    @Override
    public void onEnable() {
        KysSettingCommand kysSettingCommand = new KysSettingCommand(this);
        KysCommand kysCommand = new KysCommand(this, kysSettingCommand);

        this.getCommand("kys").setExecutor(kysCommand);
        this.getCommand("kyssetting").setExecutor(kysSettingCommand);
        this.getCommand("kyssetting").setTabCompleter(kysSettingCommand);

        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}