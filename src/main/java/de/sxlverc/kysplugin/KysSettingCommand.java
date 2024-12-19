package de.sxlverc.kysplugin;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class KysSettingCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final List<String> applicableEffects = Arrays.asList("poison", "nausea", "slowness", "darkness");
    private final List<String> modes = Arrays.asList("5050", "russian_roulette");

    public KysSettingCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {

            if (args.length < 1) {
                player.sendMessage("Usage: /kyssetting <mode|effect|countreset> <value> [on|off]");
                return false;
            }

            String settingType = args[0].toLowerCase();
            switch (settingType) {
                case "mode" -> {
                    return handleModeSetting(player, args);
                }
                case "effect" -> {
                    return handleEffectSetting(player, args);
                }
                case "countreset" -> {
                    return handleCountReset(player);
                }
                default -> {
                    player.sendMessage("Invalid setting type. Use 'mode', 'effect', or 'countreset'.");
                    return false;
                }
            }
        } else {
            sender.sendMessage("This command can only be run by a player.");
            return false;
        }
    }

    private boolean handleModeSetting(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /kyssetting mode <5050|russian_roulette>");
            return false;
        }

        String mode = args[1].toLowerCase();
        if (!modes.contains(mode)) {
            player.sendMessage("Invalid mode. Use '5050' or 'russian_roulette'.");
            return false;
        }

        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(new NamespacedKey(plugin, "kysmode"), PersistentDataType.STRING, mode);
        player.sendMessage("Mode set to " + mode);
        return true;
    }

    private boolean handleEffectSetting(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("Usage: /kyssetting effect <effectName> <on|off>");
            return false;
        }

        String effectName = args[1].toLowerCase();
        boolean enable = "on".equalsIgnoreCase(args[2]);

        NamespacedKey key = NamespacedKey.minecraft(effectName.toLowerCase());
        PotionEffectType effect = PotionEffectType.getByKey(key);
        if (effect == null) {
            player.sendMessage("Invalid effect name.");
            return false;
        }

        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(new NamespacedKey(plugin, "kyssetting_" + effect.getKey().getKey()), PersistentDataType.BYTE, (byte) (enable ? 1 : 0));
        player.sendMessage("Effect " + effectName + " set to " + (enable ? "on" : "off"));
        return true;
    }

    private boolean handleCountReset(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.remove(new NamespacedKey(plugin, "kyscount"));
        player.sendMessage("Count reset.");
        return true;
    }

    public String getMode(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        return data.get(new NamespacedKey(plugin, "kysmode"), PersistentDataType.STRING);
    }

    public boolean isEffectEnabled(Player player, PotionEffectType effect) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        Byte enabled = data.get(new NamespacedKey(plugin, "kyssetting_" + effect.getKey().getKey()), PersistentDataType.BYTE);
        return enabled != null && enabled == 1;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("mode", "effect", "countreset");
        } else if (args.length == 2 && "mode".equalsIgnoreCase(args[0])) {
            return modes;
        } else if (args.length == 2 && "effect".equalsIgnoreCase(args[0])) {
            return applicableEffects;
        } else if (args.length == 3 && "effect".equalsIgnoreCase(args[0])) {
            return Arrays.asList("on", "off");
        }
        return null;
    }
}