package com.zpedroo.voltzauctions.utils.config;

import com.zpedroo.voltzauctions.utils.FileUtils;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Messages {

    public static final String OVER_MAX_BID = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.over-max-bid"));

    public static final String NEW_AUCTION = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.new-auction"));

    public static final String NEW_BID = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.new-bid"));

    public static final String NEED_ITEM_IN_HAND = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.need-item-in-hand"));

    public static final String INVALID_AUCTION = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.invalid-auction"));

    public static final String INVALID_AMOUNT = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.invalid-amount"));

    public static final String HAS_BID = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.has-bid"));

    public static final String VALUE_BELOW = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.value-below"));

    public static final String INSUFFICIENT_CURRENCY = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.insufficient-currency"));

    public static final String ITEM_NOT_FOUND = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.item-not-found"));

    public static final String AUCTION_FINISHED_WITHOUT_BID = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.auction-finished-without-bid"));

    public static final String AUCTION_FINISHED_HAS_BID = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.auction-finished-has-bid"));

    public static final List<String> CHOOSE_MINIMUM_BID = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.choose-minimum-bid"));

    public static final List<String> CHOOSE_BID = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.choose-bid"));

    private static List<String> getColored(List<String> strList) {
        List<String> coloredList = new ArrayList<>(strList.size());
        for (String str : strList) {
            coloredList.add(getColored(str));
        }

        return coloredList;
    }

    private static String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}