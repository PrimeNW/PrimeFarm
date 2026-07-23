package com.primenw.primefarm.listeners;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import java.util.UUID;

/**
 * Otomatik Kirma Modulu: kabak ve seker kamisi icin piston/mekanizma kurmaya
 * gerek kalmadan, oluşma/buyume anini direkt yakalayip urunu depoya/satisa
 * yonlendirir. Bu materyaller fiziksel olarak hicbir zaman gercekten
 * buyumez/olusmaz - her deneme aninda "hasat edilmis" sayilir.
 *
 * Kabak (Pumpkin) ve seker kamisi ayni BlockGrowEvent uzerinden,
 * getNewState().getType() ile ayirt edilir.
 */
public class AutoBreakListener implements Listener {

    private final PrimeFarm plugin;

    public AutoBreakListener(PrimeFarm plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSugarCaneGrow(BlockGrowEvent event) {
        if (!plugin.getConfig().getBoolean("auto-break.sugar-cane", true)) return;
        if (event.getNewState().getType() != Material.SUGAR_CANE_BLOCK) return;

        Location loc = event.getBlock().getLocation();
        UUID owner = plugin.getPlotHook().getOwnerAt(loc);
        if (owner == null) return;

        boolean handled = plugin.getFarmCollectionService().collect(owner, Material.SUGAR_CANE, 1);
        if (handled) {
            // Fiziksel olarak asla 1 bloktan uzun olmasin, her buyume denemesi hasat sayilsin.
            event.setCancelled(true);
        }
        // handled false ise (oyuncu settings'ten kapatmis): normal sekilde buyumesine izin verilir.
    }

    @EventHandler(ignoreCancelled = true)
    public void onPumpkinGrow(BlockGrowEvent event) {
        if (!plugin.getConfig().getBoolean("auto-break.pumpkin", true)) return;
        if (event.getNewState().getType() != Material.PUMPKIN) return;

        Location loc = event.getBlock().getLocation();
        UUID owner = plugin.getPlotHook().getOwnerAt(loc);
        if (owner == null) return;

        boolean handled = plugin.getFarmCollectionService().collect(owner, Material.PUMPKIN, 1);
        if (handled) {
            // Kabak fiziksel olarak hic olusmasin, dogrudan urun sayilsin.
            event.setCancelled(true);
        }
        // handled false ise (oyuncu settings'ten kapatmis): normal sekilde olusmasina izin verilir.
    }
}
