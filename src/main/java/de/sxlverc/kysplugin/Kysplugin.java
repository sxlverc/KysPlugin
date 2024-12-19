package de.sxlverc.kysplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

import java.util.Objects;

public class Kysplugin extends JavaPlugin {

    @Override
    public void onEnable() {
        KysSettingCommand kysSettingCommand = new KysSettingCommand(this);
        KysCommand kysCommand = new KysCommand(this, kysSettingCommand);

        registerCommand("kys", kysCommand, null);
        registerCommand("kyssetting", kysSettingCommand, kysSettingCommand);

        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerCommand(String name, CommandExecutor executor, TabCompleter completer) {
        if (getCommand(name) != null) {
            Objects.requireNonNull(getCommand(name)).setExecutor(executor);
            if (completer != null) {
                Objects.requireNonNull(getCommand(name)).setTabCompleter(completer);
            }
        } else {
            getLogger().severe("Command '" + name + "' not found in plugin.yml");
        }
    }
}