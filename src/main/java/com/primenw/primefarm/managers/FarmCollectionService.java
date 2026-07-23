package com.primenw.primefarm.managers;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * Bir materyalin bir oyuncunun deposuna/satisina yonlendirilmesindeki tum ortak
 * mantik burada toplanir: oyuncu ayari (ac/kapa), otomatik satis tercihi,
 * sayfa kapasitesi ve tasma durumunda otomatik satis. Hem ItemCollectListener
 * (fiziksel dusen itemler) hem AutoBreakListener (hic fiziksel olusmayan
 * kabak/seker kamisi) bu tek metodu kullanir.
 */
public class FarmCollectionService {

    private final PrimeFarm plugin;

    public FarmCollectionService(PrimeFarm plugin) {
        this.plugin = plugin;
    }

    /** Materyali gercekten depoya/satisa yonlendirdiyse true, oyuncu ayardan kapattiysa false doner. */
    public boolean collect(UUID owner, Material material, int amount) {
        if (amount <= 0) return false;
        if (!plugin.getPlayerSettingsManager().isEnabled(owner, material)) return false;

        double price = plugin.getConfig().getDouble("prices." + material.name(), 0.0);
        OfflinePlayer offlineOwner = plugin.getServer().getOfflinePlayer(owner);

        if (plugin.getAutoSellManager().isEnabled(owner) && hasAutoSellPermission(owner)) {
            plugin.getEconomyManager().deposit(offlineOwner, price * amount);
            return true;
        }

        int maxSlots = plugin.getPageManager().unlockedPageCount(owner) * StorageManager.SLOTS_PER_PAGE;

        // Oyuncunun acik hicbir sayfasi yoksa (maxSlots == 0) depo kapasitesi yok
        // demektir; bu durumda "tasma" kavrami gecersizdir. Eskiden bu durumda
        // addWithCapacity'nin tum miktari overflow olarak donmesi, autosell
        // izni/ayari hic kontrol edilmeden materyalin dogrudan satilmasina yol
        // aciyordu (bkz. #21 raporu). Sayfasi olmayan oyuncu icin materyale
        // hic dokunmuyoruz; ItemCollectListener/AutoBreakListener bunu
        // "oyuncu kapatmis" gibi degerlendirip normal davranista birakir.
        if (maxSlots <= 0) {
            return false;
        }

        int overflow = plugin.getStorageManager().addWithCapacity(owner, material, amount, maxSlots);

        if (overflow > 0) {
            plugin.getEconomyManager().deposit(offlineOwner, price * overflow);
        }
        return true;
    }

    /**
     * Oyuncu cevrimdisiyse guvenli varsayimla izni yok sayilir (VIP suresi
     * bitmis birinin cevrimdisiyken otomatik satistan yararlanmaya devam
     * etmemesi icin). Cevrimiciyse gercek izni kontrol edilir.
     */
    private boolean hasAutoSellPermission(UUID owner) {
        org.bukkit.entity.Player p = plugin.getServer().getPlayer(owner);
        return p != null && (p.hasPermission("primefarm.autosell") || p.hasPermission("primefarm.admin"));
    }
}
