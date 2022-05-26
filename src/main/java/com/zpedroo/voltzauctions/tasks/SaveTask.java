package com.zpedroo.voltzauctions.tasks;

import com.zpedroo.voltzauctions.managers.DataManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static com.zpedroo.voltzauctions.utils.config.Settings.SAVE_INTERVAL;

public class SaveTask extends BukkitRunnable {

    public SaveTask(Plugin plugin) {
        this.runTaskTimerAsynchronously(plugin, 20 * SAVE_INTERVAL, 20 * SAVE_INTERVAL);
    }

    @Override
    public void run() {
        DataManager.getInstance().saveAllAuctionsData();
        DataManager.getInstance().saveAllPlayersData();
    }
}