package com.zpedroo.voltzauctions.tasks;

import com.zpedroo.voltzauctions.managers.AuctionManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static com.zpedroo.voltzauctions.utils.config.Settings.CHECK_INTERVAL;

public class CheckTask extends BukkitRunnable {

    public CheckTask(Plugin plugin) {
        this.runTaskTimerAsynchronously(plugin, 20 * CHECK_INTERVAL, 20 * CHECK_INTERVAL);
    }

    @Override
    public void run() {
        AuctionManager.getInstance().checkAllAuctionsExpiration();
    }
}