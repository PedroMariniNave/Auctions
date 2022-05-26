package com.zpedroo.voltzauctions;

import com.zpedroo.voltzauctions.commands.AuctionCmd;
import com.zpedroo.voltzauctions.hooks.PlaceholderAPIHook;
import com.zpedroo.voltzauctions.listeners.PlayerChatListener;
import com.zpedroo.voltzauctions.listeners.PlayerGeneralListeners;
import com.zpedroo.voltzauctions.managers.AuctionManager;
import com.zpedroo.voltzauctions.managers.DataManager;
import com.zpedroo.voltzauctions.mysql.DBConnection;
import com.zpedroo.voltzauctions.tasks.CheckTask;
import com.zpedroo.voltzauctions.tasks.SaveTask;
import com.zpedroo.voltzauctions.utils.FileUtils;
import com.zpedroo.voltzauctions.utils.config.NumberFormatter;
import com.zpedroo.voltzauctions.utils.menu.Menus;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

import static com.zpedroo.voltzauctions.utils.config.Settings.COMMAND;
import static com.zpedroo.voltzauctions.utils.config.Settings.ALIASES;

public class VoltzAuctions extends JavaPlugin {

    private static VoltzAuctions instance;
    public static VoltzAuctions get() { return instance; }

    public void onEnable() {
        instance = this;
        new FileUtils(this);

        if (!isMySQLEnabled(getConfig())) {
            getLogger().log(Level.SEVERE, "MySQL are disabled! You need to enable it.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new DBConnection(getConfig());
        new NumberFormatter(getConfig());
        new AuctionManager();
        new Menus();
        new CheckTask(this);
        new SaveTask(this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this);
        }

        registerListeners();
        registerCommand(COMMAND, ALIASES, new AuctionCmd());
    }

    public void onDisable() {
        if (!isMySQLEnabled(getConfig())) return;

        try {
            DataManager.getInstance().saveAllAuctionsData();
            DataManager.getInstance().saveAllPlayersData();
            DBConnection.getInstance().closeConnection();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "An error occurred while trying to save data!");
            ex.printStackTrace();
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerGeneralListeners(), this);
    }

    private void registerCommand(String command, List<String> aliases, CommandExecutor executor) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            PluginCommand pluginCmd = constructor.newInstance(command, this);
            pluginCmd.setAliases(aliases);
            pluginCmd.setExecutor(executor);

            Field field = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(Bukkit.getPluginManager());
            commandMap.register(getName().toLowerCase(), pluginCmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isMySQLEnabled(FileConfiguration file) {
        if (!file.contains("MySQL.enabled")) return false;

        return file.getBoolean("MySQL.enabled");
    }
}
