package com.primenw.primefarm.listeners;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Bu listener PrimeFarm'in kalbidir.
 *
 * Kaktus, seker kamisi vb. bloklar herhangi bir sekilde kirildiginda
 * (elle, pistonla, fizik nedeniyle dusme vb.) sunucu bir ItemSpawnEvent tetikler.
 * Eger bu event bir oyuncunun arsasi icinde ve takip edilen bir materyal icin
 * gerceklesiyorsa, itemi dogrudan o arsanin sahibinin sanal deposuna ekleyip
 * dunyadan kaldiriyoruz. Boylece hem hopper/zincir kurmaya gerek kalmiyor
 * hem de item lag'i olusmuyor.
 *
 * Islem BIR TICK ERTELENIR (event aninda degil): boylece ayni tick icinde
 * tetiklenen PlayerDropItemEvent (oyuncu Q ile elden atarsa) once isaretini
 * koyabiliyor - ThrownItemTracker sayesinde oyuncular birbirine bu
 * materyalleri hediye/takas edebiliyor, biz sadece gercek farm dusmelerine
 * dokunuyoruz. Ayrica oyuncu /pf settings'ten materyali kapattiysa
 * (FarmCollectionService.collect false donerse) item DOKUNULMADAN yerde
 * kalir, silinmez.
 */
public class ItemCollectListener implements Listener {

    private final PrimeFarm plugin;
    private final Set<Material> tracked = new HashSet<>();

    public ItemCollectListener(PrimeFarm plugin) {
        this.plugin = plugin;
        reloadTrackedMaterials();
    }

    public void reloadTrackedMaterials() {
        tracked.clear();
        List<String> names = plugin.getConfig().getStringList("tracked-materials");
        for (String name : names) {
            Material mat = Material.matchMaterial(name);
            if (mat != null) {
                tracked.add(mat);
            } else {
                plugin.getLogger().warning("config.yml -> tracked-materials icinde bilinmeyen materyal: " + name);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item itemEntity = event.getEntity();
        Material material = itemEntity.getItemStack().getType();

        if (!tracked.contains(material)) return;

        UUID entityId = itemEntity.getUniqueId();

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!itemEntity.isValid()) return; // birisi zaten aldi ya da despawn oldu
            if (plugin.getThrownItemTracker().wasPlayerThrown(entityId)) return; // oyuncu elden atmis, dokunma

            ItemStack stack = itemEntity.getItemStack();
            Location loc = itemEntity.getLocation();
            UUID owner = plugin.getPlotHook().getOwnerAt(loc);
            if (owner == null) return;

            boolean handled = plugin.getFarmCollectionService().collect(owner, material, stack.getAmount());
            if (handled) {
                itemEntity.remove();
            }
            // handled false ise (oyuncu settings'ten kapatmis): item'a hic dokunmuyoruz, normal yerde kalir.
        });
    }
}
