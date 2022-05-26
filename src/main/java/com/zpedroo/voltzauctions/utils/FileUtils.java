package com.zpedroo.voltzauctions.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileUtils {

    private static FileUtils instance;
    public static FileUtils get() { return instance; }

    private final Plugin plugin;
    private final Map<Files, FileManager> files;

    public FileUtils(Plugin plugin) {
        instance = this;
        this.plugin = plugin;
        this.files = new HashMap<>(Files.values().length);

        for (Files files : Files.values()) {
            getFiles().put(files, new FileManager(files));
        }
    }

    public String getString(Files file, String path) {
        return getString(file, path, "NULL");
    }

    public String getString(Files file, String path, String defaultValue) {
        return getFile(file).get().getString(path, defaultValue);
    }

    public List<String> getStringList(Files file, String path) {
        return getFiles().get(file).get().getStringList(path);
    }

    public boolean getBoolean(Files file, String path) {
        return getFile(file).get().getBoolean(path);
    }

    public int getInt(Files file, String path) {
        return getInt(file, path, 0);
    }

    public int getInt(Files file, String path, int defaultValue) {
        return getFile(file).get().getInt(path, defaultValue);
    }

    public long getLong(Files file, String path) {
        return getLong(file, path, 0);
    }

    public long getLong(Files file, String path, long defaultValue) {
        return getFile(file).get().getLong(path, defaultValue);
    }

    public double getDouble(Files file, String path) {
        return getDouble(file, path, 0);
    }

    public double getDouble(Files file, String path, double defaultValue) {
        return getFile(file).get().getDouble(path, defaultValue);
    }

    public float getFloat(Files file, String path) {
        return getFloat(file, path, 0);
    }

    public float getFloat(Files file, String path, float defaultValue) {
        return (float) getFile(file).get().getDouble(path, defaultValue);
    }

    public Set<String> getSection(Files file, String path) {
        return getFile(file).get().getConfigurationSection(path).getKeys(false);
    }

    public FileManager getFile(Files file) {
        return getFiles().get(file);
    }

    public Map<Files, FileManager> getFiles() {
        return files;
    }

    private void copy(InputStream is, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;

            while ((len=is.read(buf)) > 0) {
                out.write(buf,0,len);
            }

            out.close();
            is.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public enum Files {
        CONFIG("config", "configuration-files", ""),
        MAIN("main", "menus", "menus"),
        ITEM_CONFIRMATION("item_confirmation", "menus", "menus"),
        SELECT_DURATION("select_duration", "menus", "menus"),
        SELECT_CURRENCY("select_currency", "menus", "menus"),
        ACTIVE_AUCTIONS("active_auctions", "menus", "menus"),
        PLAYER_AUCTIONS("player_auctions", "menus", "menus"),
        PLAYER_ITEMS("player_items", "menus", "menus");

        private final String name;
        private final String resource;
        private final String folder;

        Files(String name, String resource, String folder) {
            this.name = name;
            this.resource = resource;
            this.folder = folder;
        }

        public String getName() {
            return name;
        }

        public String getResource() {
            return resource;
        }

        public String getFolder() {
            return folder;
        }
    }

    public class FileManager {

        private final File file;
        private FileConfiguration fileConfig;

        public FileManager(Files file) {
            this.file = new File(plugin.getDataFolder() + (file.getFolder().isEmpty() ? "" : "/" + file.getFolder()), file.getName() + ".yml");

            if (!this.file.exists()) {
                try {
                    this.file.getParentFile().mkdirs();
                    this.file.createNewFile();

                    copy(plugin.getResource((file.getResource().isEmpty() ? "" : file.getResource() + "/") + file.getName() + ".yml"), this.file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.file), StandardCharsets.UTF_8));
                fileConfig = YamlConfiguration.loadConfiguration(reader);
                reader.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public FileConfiguration get() {
            return fileConfig;
        }

        public File getFile() {
            return file;
        }

        public void save() {
            try {
                fileConfig.save(file);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}