package com.github.anton4k.rubles;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;

public class Rubles extends JavaPlugin {

    private File dataFile;
    private FileConfiguration dataConfig;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        // Создаем/загружаем config.yml
        saveDefaultConfig();
        // Создаем/загружаем data.yml и messages.yml
        setupDataFile();
        setupMessagesFile();
        // Регистрация PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RublesPlaceholder(this).register();
        }
        getLogger().info("Rubles plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Rubles plugin disabled!");
    }

    private void setupDataFile() {
        dataFile = new File(getDataFolder(), "data.yml");
        // Если файл не существует, создаем его с дефолтными значениями
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs(); // Создаем папку, если её нет
            saveResource("data.yml", false); // Копируем дефолтный data.yml из ресурсов
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void setupMessagesFile() {
        messagesFile = new File(getDataFolder(), "messages.yml");
        // Если файл не существует, создаем его с дефолтными значениями
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs(); // Создаем папку, если её нет
            saveResource("messages.yml", false); // Копируем дефолтный messages.yml из ресурсов
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public FileConfiguration getDataConfig() {
        return dataConfig;
    }

    public void saveDataConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String key) {
        return messagesConfig.getString(key, key);
    }

    public void reloadConfigs() {
        reloadConfig();
        setupDataFile();
        setupMessagesFile();
        getLogger().info("Rubles plugin configuration reloaded!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("rubles")) {
            if (args.length == 0) {
                // Команда /rubles
                if (!sender.hasPermission("rubles.use")) {
                    sender.sendMessage(getMessage("no-permission"));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(getMessage("player-only"));
                    return true;
                }
                Player player = (Player) sender;
                int defaultBalance = getConfig().getInt("default-balance", 0);
                int balance = dataConfig.getInt("players." + player.getUniqueId() + ".balance", defaultBalance);
                player.sendMessage(getMessage("balance").replace("{balance}", String.valueOf(balance)));
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                // Команда /rubles reload
                if (!sender.hasPermission("rubles.reload")) {
                    sender.sendMessage(getMessage("no-permission"));
                    return true;
                }
                reloadConfigs();
                sender.sendMessage(getMessage("config-reloaded"));
                return true;
            } else if (args.length == 3 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
                // Команды /rubles add и /rubles remove
                if (args[0].equalsIgnoreCase("add") && !sender.hasPermission("rubles.add")) {
                    sender.sendMessage(getMessage("no-permission"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("remove") && !sender.hasPermission("rubles.remove")) {
                    sender.sendMessage(getMessage("no-permission"));
                    return true;
                }
                Player target = getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(getMessage("player-not-found"));
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                    if (amount <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    sender.sendMessage(getMessage("invalid-amount"));
                    return true;
                }
                String targetPath = "players." + target.getUniqueId() + ".balance";
                int currentBalance = dataConfig.getInt(targetPath, 0);
                if (args[0].equalsIgnoreCase("add")) {
                    dataConfig.set(targetPath, currentBalance + amount);
                    sender.sendMessage(getMessage("added").replace("{amount}", String.valueOf(amount)).replace("{player}", target.getName()));
                } else {
                    int newBalance = Math.max(0, currentBalance - amount);
                    dataConfig.set(targetPath, newBalance);
                    sender.sendMessage(getMessage("removed").replace("{amount}", String.valueOf(amount)).replace("{player}", target.getName()));
                }
                saveDataConfig();
                return true;
            }
        }
        return false;
    }
}