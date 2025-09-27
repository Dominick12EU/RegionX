package it.dominick.regionx.manager;

import com.google.gson.*;
import it.dominick.regionx.RegionX;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import it.dominick.regionx.region.settings.SettingRegistry;
import it.dominick.regionx.region.settings.SettingRegistryAPI;
import it.dominick.regionx.utils.ChatUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionManager {

    @Getter
    private final Map<String, Region> regions = new HashMap<>();
    private final Map<Player, PlayerPositions> playerPositions = new HashMap<>();

    private final File regionsFolder;
    private final Gson gson;

    public RegionManager(RegionX plugin) {
        this.regionsFolder = new File(plugin.getDataFolder(), "regions");
        if (!regionsFolder.exists()) {
            regionsFolder.mkdirs();
        }
        this.gson = new GsonBuilder().setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }

    public void addRegion(String name, Location pos1, Location pos2) {
        Region region = new Region(name, pos1, pos2);
        regions.put(name.toLowerCase(), region);
        saveRegion(name, pos1, pos2);
    }

    public void removeRegion(String name) {
        regions.remove(name.toLowerCase());
        File regionFile = new File(regionsFolder, name + ".json");
        if (regionFile.exists()) {
            regionFile.delete();
        }
    }

    public Region getRegion(Location location) {
        return regions.values().stream()
                .filter(region -> region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                .findFirst()
                .orElse(null);
    }

    public Region getRegion(String name) {
        return regions.get(name.toLowerCase());
    }

    public boolean regionExists(String name) {
        return regions.containsKey(name.toLowerCase());
    }

    public void saveRegion(String name, Location pos1, Location pos2) {
        File regionFile = new File(regionsFolder, name.toLowerCase() + ".json");
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", name);
        jsonObject.add("pos1", locationToJson(pos1));
        jsonObject.add("pos2", locationToJson(pos2));

        try (Writer writer = new FileWriter(regionFile)) {
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadRegions() {
        File[] files = regionsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try (Reader reader = new FileReader(file)) {
                    JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
                    String name = jsonObject.get("name").getAsString();
                    Location pos1 = jsonToLocation(jsonObject.getAsJsonObject("pos1"));
                    Location pos2 = jsonToLocation(jsonObject.getAsJsonObject("pos2"));

                    Region region = new Region(name, pos1, pos2);

                    JsonObject settingsObject = jsonObject.getAsJsonObject("settings");
                    if (settingsObject != null) {
                        for (Map.Entry<String, JsonElement> entry : settingsObject.entrySet()) {
                            String settingName = entry.getKey();
                            SettingRegistryAPI registry = SettingRegistry.getInstance();
                            registry.applySettingToRegion(settingName, region);
                        }
                    }

                    regions.put(name.toLowerCase(), region);
                    System.out.println("Loaded region: " + name);
                } catch (FileNotFoundException e) {
                    ChatUtils.error(e, "File not found: " + file.getName());
                } catch (IOException e) {
                    ChatUtils.error(e, "IOException while reading file: " + file.getName());
                } catch (Exception e) {
                    ChatUtils.error(e, "Unexpected error while loading region from file: " + file.getName());
                }
            }
        } else {
            System.err.println("No region files found in the regions folder.");
        }
    }

    public JsonObject locationToJson(Location location) {
        JsonObject jsonLocation = new JsonObject();
        jsonLocation.addProperty("world", location.getWorld().getName());
        jsonLocation.addProperty("x", location.getX());
        jsonLocation.addProperty("y", location.getY());
        jsonLocation.addProperty("z", location.getZ());
        jsonLocation.addProperty("yaw", location.getYaw());
        jsonLocation.addProperty("pitch", location.getPitch());
        return jsonLocation;
    }

    private Location jsonToLocation(JsonObject json) {
        String worldName = json.get("world").getAsString();
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();
        float yaw = json.has("yaw") ? json.get("yaw").getAsFloat() : 0.0f;
        float pitch = json.has("pitch") ? json.get("pitch").getAsFloat() : 0.0f;
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    public Region getRegionByLocation(Location location) {
        for (Region region : regions.values()) {
            if (region.contains(location)) {
                return region;
            }
        }
        return null;
    }

    public void setPlayerPos1(Player player, Location location) {
        playerPositions.computeIfAbsent(player, p -> new PlayerPositions(null, null)).setPos1(location);
    }

    public void setPlayerPos2(Player player, Location location) {
        playerPositions.computeIfAbsent(player, p -> new PlayerPositions(null, null)).setPos2(location);
    }

    public Location getPlayerPos1(Player player) {
        PlayerPositions positions = playerPositions.get(player);
        return positions != null ? positions.getPos1() : null;
    }

    public Location getPlayerPos2(Player player) {
        PlayerPositions positions = playerPositions.get(player);
        return positions != null ? positions.getPos2() : null;
    }

    public List<Region> getAllRegions() {
        return new ArrayList<>(regions.values());
    }

    public void toggleRegionSetting(Region region, String settingName) {
        File regionFile = new File(regionsFolder, region.getName().toLowerCase() + ".json");
        JsonObject jsonObject;

        if (regionFile.exists()) {
            try (Reader reader = new FileReader(regionFile)) {
                jsonObject = gson.fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            jsonObject = new JsonObject();
        }

        JsonObject settingsObject = jsonObject.has("settings") ? jsonObject.getAsJsonObject("settings") : new JsonObject();

        if (settingsObject.has(settingName.toUpperCase())) {
            settingsObject.remove(settingName.toUpperCase());
        } else {
            settingsObject.addProperty(settingName.toUpperCase(), true);
        }

        jsonObject.add("settings", settingsObject);

        try (Writer writer = new FileWriter(regionFile)) {
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
