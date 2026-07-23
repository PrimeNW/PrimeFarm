package com.primenw.primefarm.managers;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Oyuncular birbirine item verebilsin diye: bir oyuncu envanterinden Q ile
 * item attiginda, o item entity'sini isaretleriz. ItemCollectListener bu
 * isareti gorunce dokunmaz, item normal sekilde yerde kalip baskasi
 * tarafindan alinabilir. Boylece kaktus/seker kamisi/kabak gibi takip
 * edilen materyalleri oyuncular birbirine hediye/takas edebilir.
 */
public class ThrownItemTracker implements Listener {

    private final PrimeFarm plugin;
    private final Set<UUID> thrown = ConcurrentHashMap.newKeySet();

    public ThrownItemTracker(PrimeFarm plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        UUID entityId = event.getItemDrop().getUniqueId();
        thrown.add(entityId);
        // Guvenlik agi: ItemCollectListener bu isareti hic okumazsa bile
        // (orn. takip edilmeyen materyal) bir sure sonra otomatik temizlensin.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> thrown.remove(entityId), 20L);
    }

    /** Isaretliyse true dondurup isareti kaldirir (bir kere kontrol edilsin yeter). */
    public boolean wasPlayerThrown(UUID entityId) {
        return thrown.remove(entityId);
    }
}
