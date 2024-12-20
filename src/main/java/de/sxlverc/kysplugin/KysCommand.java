package de.sxlverc.kysplugin;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return false;
        }

        PersistentDataContainer data = player.getPersistentDataContainer();

        if (player.getWorld().getEnvironment() == World.Environment.NETHER) {
            player.sendMessage("You can't use this command in the Nether.");
            return true;
        }

        if (player.getWorld().getEnvironment() == World.Environment.THE_END) {
            player.sendMessage("You can't use this command in the End.");
            return true;
        }

        if (args.length > 0 && "countreset".equalsIgnoreCase(args[0])) {
            resetCounts(data, player);
            return true;
        }

        int successfulAttempts = data.getOrDefault(successfulAttemptsKey, PersistentDataType.INTEGER, 0);
        int unsuccessfulAttempts = data.getOrDefault(unsuccessfulAttemptsKey, PersistentDataType.INTEGER, 0);

        String mode = kysSettingCommand.getMode(player);
        if ("russian_roulette".equalsIgnoreCase(mode)) {
            handleRussianRoulette(player, data, unsuccessfulAttempts);
        } else if ("5050".equalsIgnoreCase(mode)) {
            handle5050Mode(player, data, successfulAttempts, unsuccessfulAttempts);
        }

        return true;
    }

    private void resetCounts(PersistentDataContainer data, Player player) {
        data.set(successfulAttemptsKey, PersistentDataType.INTEGER, 0);
        data.set(unsuccessfulAttemptsKey, PersistentDataType.INTEGER, 0);
        player.sendMessage("Successful and unsuccessful attempts have been reset."); //Output message when successful and unsuccessful attempts have been reset
    }

    private void handleRussianRoulette(Player player, PersistentDataContainer data, int unsuccessfulAttempts) {
        boolean success = unsuccessfulAttempts >= 5 || random.nextInt(6) == 0;
        if (success) {
            player.setHealth(0);
            player.sendMessage("Bang!"); //Output message when russian roulette is lost
            data.set(unsuccessfulAttemptsKey, PersistentDataType.INTEGER, 0); // Reset unsuccessful attempts
        } else {
            player.sendMessage("Click! not yet..."); //Output message when russian roulette is won
            data.set(unsuccessfulAttemptsKey, PersistentDataType.INTEGER, unsuccessfulAttempts + 1);
        }
    }

    private void handle5050Mode(Player player, PersistentDataContainer data, int successfulAttempts, int unsuccessfulAttempts) {
        boolean success = random.nextBoolean();
        if (success) {
            player.setHealth(0);
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
            data.set(successfulAttemptsKey, PersistentDataType.INTEGER, successfulAttempts + 1);
        } else {
            applyRandomEffect(player, data, unsuccessfulAttempts);
        }
        player.sendMessage("Successful attempts: " + successfulAttempts);  //Outputs both after each attempt
        player.sendMessage("Unsuccessful attempts: " + unsuccessfulAttempts); //Outputs both after each attempt
    }

    private void applyRandomEffect(Player player, PersistentDataContainer data, int unsuccessfulAttempts) {
        PotionEffectType[] effects = {
                PotionEffectType.POISON,
                PotionEffectType.NAUSEA,
                PotionEffectType.SLOWNESS,
                PotionEffectType.BLINDNESS,
        };
        PotionEffectType randomEffect = effects[random.nextInt(effects.length)];
        if (kysSettingCommand.isEffectEnabled(player, randomEffect)) {
            player.addPotionEffect(new PotionEffect(randomEffect, 72000, 1));
            player.sendMessage("No death today, but you feel sick. You have been affected by " + randomEffect.getKey().getKey().toLowerCase() + ".");
            spawnRandomParticles(player); //^^^Output message when an effect is applied
            new BukkitRunnable() {
                @Override
                public void run() {
                    spawnRandomParticles(player);
                }
            }.runTaskLater(plugin, 10L);
        } else {
            player.sendMessage("No death today, but you feel a strange sensation.");
        }                           //Output message when no effect is applied
        data.set(unsuccessfulAttemptsKey, PersistentDataType.INTEGER, unsuccessfulAttempts + 1);
    }

    private void spawnRandomParticles(Player player) {
        for (int i = 0; i < 10; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2;
            double offsetY = random.nextDouble();
            double offsetZ = (random.nextDouble() - 0.5) * 2;
            player.getWorld().spawnParticle(Particle.RAID_OMEN, player.getLocation().add(offsetX, offsetY, offsetZ), 1);
        }
    }
}