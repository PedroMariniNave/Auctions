package com.zpedroo.voltzauctions.utils.api;

import org.bukkit.Bukkit;

public class ReflectionUtils {

    private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + version + "." + name);
    }
}