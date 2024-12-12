package de.sxlverc.kysplugin;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class KysCommand implements CommandExecutor {

    private final Random random = new Random();
    private final JavaPlugin plugin;
    private final NamespacedKey successfulAttemptsKey;
    private final NamespacedKey unsuccessfulAttemptsKey;
    private final KysSettingCommand kysSettingCommand;

    public KysCommand(JavaPlugin plugin, KysSettingCommand kysSettingCommand) {
        this.plugin = plugin;
        this.kysSettingCommand = kysSettingCommand;
        this.successfulAttemptsKey = new NamespacedKey(plugin, "successful_attempts");
        this.unsuccessfulAttemptsKey = new NamespacedKey(plugin, "unsuccessful_attempts");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PersistentDataContainer data = player.getPersistentDataContainer();

            if (args.length > 0 && "countreset".equalsIgnoreCase(args[0])) {
                data.set(successfulAttemptsKey, PersistentDataType.INTEGER, 0);
                data.set(unsuccessfulAttemptsKey, PersistentDataType.INTEGER, 0);
                player.sendMessage("Successful and unsuccessful attempts have been reset.");
                return true;
            }

            int successfulAttempts = data.getOrDefault(successfulAttemptsKey, PersistentDataType.INTEGER, 0);
            int unsuccessfulAttempts = data.getOrDefault(unsuccessfulAttemptsKey, PersistentDataType.INTEGER, 0);

            String mode = kysSettingCommand.getMode(player);
            boolean success = false;
            if ("russian_roulette".equalsIgnoreCase(mode)) {
                if (unsuccessfulAttempts >= 5) {
                    success = true; // Force success on the 6th attempt
                } else {
                    success = random.nextInt(6) == 0; // 1/6 chance
                    if (!success) {
                        unsuccessfulAttempts++;
                    }
                }
                if (success) {
                    player.setHealth(0);
                    player.sendMessage("Revolver reloaded.");
                } else {
                    player.sendMessage("Not yet.");
                }
                data.set(unsuccessfulAttemptsKey, PersistentDataType.INTEGER, unsuccessfulAttempts);
            } else if ("5050".equalsIgnoreCase(mode)) {
                success = random.nextBoolean(); // 50/50 chance
                if (success) {
                    player.setHealth(0);
                    player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
                    successfulAttempts++;
                    data.set(successfulAttemptsKey, PersistentDataType.INTEGER, successfulAttempts);
                } else {
                    PotionEffectType[] effects = {
                            PotionEffectType.POISON,
                            PotionEffectType.NAUSEA, // Nausea
                            PotionEffectType.SLOWNESS, // Slowness
                            PotionEffectType.BLINDNESS, // Darkness
                    };
                    PotionEffectType randomEffect = effects[random.nextInt(effects.length)];
                    if (kysSettingCommand.isEffectEnabled(player, randomEffect)) {
                        int duration = 72000; // Default duration
                        int amplifier = 1; // Default amplifier
                        player.addPotionEffect(new PotionEffect(randomEffect, duration, amplifier));
                        player.sendMessage("No death today, but you feel sick. You have been affected by " + randomEffect.getName().toLowerCase() + ".");
                        unsuccessfulAttempts++;
                        data.set(unsuccessfulAttemptsKey, PersistentDataType.INTEGER, unsuccessfulAttempts);
                        spawnRandomParticles(player);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                spawnRandomParticles(player);
                            }
                        }.runTaskLater(plugin, 10L); // 10 ticks = 0.5 seconds
                    } else {
                        // Default action if no effect is enabled
                        player.sendMessage("No death today, but you feel a strange sensation.");
                        unsuccessfulAttempts++;
                        data.set(unsuccessfulAttemptsKey, PersistentDataType.INTEGER, unsuccessfulAttempts);
                    }
                }
                player.sendMessage("Successful attempts: " + successfulAttempts);
                player.sendMessage("Unsuccessful attempts: " + unsuccessfulAttempts);
            }
            return true;
        } else {
            sender.sendMessage("This command can only be run by a player.");
            return false;
        }
    }

    private void spawnRandomParticles(Player player) {
        for (int i = 0; i < 10; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2;
            double offsetY = random.nextDouble();
            double offsetZ = (random.nextDouble() - 0.5) * 2;
            Vector offset = new Vector(offsetX, offsetY, offsetZ);
            player.getWorld().spawnParticle(Particle.RAID_OMEN, player.getLocation().add(offset), 1);
        }
    }
}