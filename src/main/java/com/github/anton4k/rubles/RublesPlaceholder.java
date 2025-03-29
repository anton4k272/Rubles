package com.github.anton4k.rubles;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.Optional;

public class RublesPlaceholder extends PlaceholderExpansion {

    private final Rubles plugin;

    public RublesPlaceholder(Rubles plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "rubles";
    }

    @Override
    public String getAuthor() {
        return "anton4k";
    }

    @Override
    public String getVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        // Проверяем, что игрок не null
        if (player == null) return "";

        // Обрабатываем запросы Placeholder
        if (identifier.equals("balance")) {
            return Optional.ofNullable(plugin.getDataConfig().getInt("players." + player.getUniqueId() + ".balance", 0))
                    .map(String::valueOf)
                    .orElse("0");
        }

        return null;
    }
}