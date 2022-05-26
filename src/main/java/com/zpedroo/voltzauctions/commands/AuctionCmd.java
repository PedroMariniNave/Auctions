package com.zpedroo.voltzauctions.commands;

import com.zpedroo.voltzauctions.utils.menu.Menus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuctionCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        Menus.getInstance().openMainMenu(player);
        return false;
    }
}