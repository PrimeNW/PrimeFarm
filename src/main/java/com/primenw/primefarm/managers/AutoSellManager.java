package com.primenw.primefarm.managers;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Otomatik satis modulu: oyuncu bunu /pf autosell ile actiginda, toplanan
 * materyaller depoya gitmez, aninda satilip parasi hesabina yatar.
 * Varsayilan olarak kapalidir (depolama davranisi degismez).
 */
public class AutoSellManager {

    private final PrimeFarm plugin;
    private final File file;
    private final Set<UUID> enabled = ConcurrentHashMap.newKeySet();

    public AutoSellManager(PrimeFarm plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "autosell.yml");
        load();
    }

    public boolean isEnabled(UUID uuid) {
        return enabled.contains(uuid);
    }

    /** Durumu tersine cevirir, yeni durumu dondurur. */
    public boolean toggle(UUID uuid) {
        boolean nowEnabled;
        if (enabled.add(uuid)) {
            nowEnabled = true;
        } else {
            enabled.remove(uuid);
            nowEnabled = false;
        }
        save();
        return nowEnabled;
    }

    // ---------- Kalicilik ----------

    public void load() {
        enabled.clear();
        if (!file.exists()) return;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (String uuidStr : cfg.getStringList("enabled")) {
            try {
                enabled.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void save() {
        FileConfiguration cfg = new YamlConfiguration();
        java.util.List<String> names = new java.util.ArrayList<>();
        for (UUID uuid : enabled) names.add(uuid.toString());
        cfg.set("enabled", names);

        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("autosell.yml kaydedilemedi: " + e.getMessage());
        }
    }
}
