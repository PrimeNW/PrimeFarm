package com.primenw.primefarm;

import com.primenw.primefarm.commands.FarmCommand;
import com.primenw.primefarm.gui.StorageGUI;
import com.primenw.primefarm.listeners.GUIListener;
import com.primenw.primefarm.listeners.ItemCollectListener;
import com.primenw.primefarm.managers.EconomyManager;
import com.primenw.primefarm.managers.LanguageManager;
import com.primenw.primefarm.managers.NoopPlotHook;
import com.primenw.primefarm.managers.PageManager;
import com.primenw.primefarm.managers.PlotHook;
import com.primenw.primefarm.managers.StorageManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PrimeFarm extends JavaPlugin {

    private StorageManager storageManager;
    private EconomyManager economyManager;
    private PageManager pageManager;
    private LanguageManager languageManager;
    private StorageGUI storageGUI;
    private com.primenw.primefarm.gui.SettingsGUI settingsGUI;
    private com.primenw.primefarm.managers.PlayerSettingsManager playerSettingsManager;
    private PlotHook plotHook;
    private ItemCollectListener itemCollectListener;
    private com.primenw.primefarm.managers.FarmCollectionService farmCollectionService;
    private com.primenw.primefarm.managers.AutoSellManager autoSellManager;
    private com.primenw.primefarm.managers.ThrownItemTracker thrownItemTracker;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        this.languageManager = new LanguageManager(this);
        this.storageManager = new StorageManager(this);
        this.economyManager = new EconomyManager(this);
        this.pageManager = new PageManager(this);
        this.storageGUI = new StorageGUI(this);
        this.playerSettingsManager = new com.primenw.primefarm.managers.PlayerSettingsManager(this);
        this.settingsGUI = new com.primenw.primefarm.gui.SettingsGUI(this);
        this.autoSellManager = new com.primenw.primefarm.managers.AutoSellManager(this);
        this.farmCollectionService = new com.primenw.primefarm.managers.FarmCollectionService(this);
        this.thrownItemTracker = new com.primenw.primefarm.managers.ThrownItemTracker(this);
        getServer().getPluginManager().registerEvents(thrownItemTracker, this);

        if (getServer().getPluginManager().getPlugin("PlotSquared") != null) {
            this.plotHook = new com.primenw.primefarm.managers.PlotSquaredHook(this);
        } else {
            this.plotHook = new NoopPlotHook(this);
        }

        this.itemCollectListener = new ItemCollectListener(this);
        getServer().getPluginManager().registerEvents(itemCollectListener, this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new com.primenw.primefarm.listeners.AutoBreakListener(this), this);

        getCommand("primefarm").setExecutor(new FarmCommand(this));

        getLogger().info("PrimeFarm aktif edildi. Vault ekonomisi: "
                + (economyManager.isVaultEnabled() ? "AKTIF" : "dahili (fallback)"));
    }

    @Override
    public void onDisable() {
        if (storageManager != null) storageManager.saveAll();
        if (economyManager != null) economyManager.saveFallback();
        if (pageManager != null) pageManager.save();
        if (playerSettingsManager != null) playerSettingsManager.save();
        if (autoSellManager != null) autoSellManager.save();
        if (languageManager != null) languageManager.savePlayerPreferences();

        getLogger().info("PrimeFarm devre disi birakildi, veriler kaydedildi.");
    }

    /** /pf reload komutu icin: config + dil + sayfa atamalarini tazeler. */
    public void reloadEverything() {
        reloadConfig();
        languageManager.reload();
        itemCollectListener.reloadTrackedMaterials();
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PageManager getPageManager() {
        return pageManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public StorageGUI getStorageGUI() {
        return storageGUI;
    }

    public com.primenw.primefarm.gui.SettingsGUI getSettingsGUI() {
        return settingsGUI;
    }

    public com.primenw.primefarm.managers.PlayerSettingsManager getPlayerSettingsManager() {
        return playerSettingsManager;
    }

    public PlotHook getPlotHook() {
        return plotHook;
    }

    public com.primenw.primefarm.managers.FarmCollectionService getFarmCollectionService() {
        return farmCollectionService;
    }

    public com.primenw.primefarm.managers.AutoSellManager getAutoSellManager() {
        return autoSellManager;
    }

    public com.primenw.primefarm.managers.ThrownItemTracker getThrownItemTracker() {
        return thrownItemTracker;
    }
}
