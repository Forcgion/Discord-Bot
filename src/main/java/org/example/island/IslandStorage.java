package org.example.island;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;

public class IslandStorage {

    private static final String DATA_FILE = "island_data.json";
    private static final String BOTS_FILE = "island_bots.json";
    private static final String AFK_FILE  = "island_afk.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ─── Generic helpers ─────────────────────────────────────────────────────

    private static JsonObject loadFile(String filename) {
        try {
            File f = new File(filename);
            if (!f.exists()) return new JsonObject();
            String content = new String(Files.readAllBytes(f.toPath()));
            JsonElement el = JsonParser.parseString(content);
            return el.isJsonObject() ? el.getAsJsonObject() : new JsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonObject();
        }
    }

    private static void saveFile(String filename, JsonObject data) {
        try {
            Files.writeString(Path.of(filename), GSON.toJson(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── Island data (coins, level, xp) ──────────────────────────────────────

    public static JsonObject getIslandData(String userId) {
        JsonObject all = loadFile(DATA_FILE);
        if (all.has(userId)) return all.getAsJsonObject(userId);
        return null;
    }

    // Returns true only if they actually BOUGHT an island
    public static boolean hasIsland(String userId) {
        JsonObject all = loadFile(DATA_FILE);
        if (!all.has(userId)) return false;
        JsonObject data = all.getAsJsonObject(userId);
        return data.has("islandBought") && data.get("islandBought").getAsBoolean();
    }

    // Returns true if they have any storage entry (from /work or /newdaily)
    public static boolean hasStorageEntry(String userId) {
        return loadFile(DATA_FILE).has(userId);
    }

    // Creates a storage entry without marking island as bought
    public static void createIsland(String userId) {
        JsonObject all = loadFile(DATA_FILE);
        if (all.has(userId)) return; // don't overwrite existing entry
        JsonObject island = new JsonObject();
        island.addProperty("coins", 0);
        island.addProperty("level", 1);
        island.addProperty("xp", 0);
        island.addProperty("dailyCoins", 0);
        island.addProperty("lastDailyClaim", 0);
        island.addProperty("islandBought", false);
        all.add(userId, island);
        saveFile(DATA_FILE, all);
    }

    // Marks island as actually bought
    public static void markIslandBought(String userId) {
        JsonObject all = loadFile(DATA_FILE);
        JsonObject data = all.getAsJsonObject(userId);
        data.addProperty("islandBought", true);
        saveFile(DATA_FILE, all);
    }

    public static long getCoins(String userId) {
        JsonObject data = getIslandData(userId);
        return data != null ? data.get("coins").getAsLong() : 0;
    }

    public static void addCoins(String userId, long amount) {
        JsonObject all = loadFile(DATA_FILE);
        JsonObject data = all.getAsJsonObject(userId);
        long current = data.get("coins").getAsLong();
        data.addProperty("coins", current + amount);
        saveFile(DATA_FILE, all);
    }

    public static boolean removeCoins(String userId, long amount) {
        JsonObject all = loadFile(DATA_FILE);
        JsonObject data = all.getAsJsonObject(userId);
        long current = data.get("coins").getAsLong();
        if (current < amount) return false;
        data.addProperty("coins", current - amount);
        saveFile(DATA_FILE, all);
        return true;
    }

    public static int getLevel(String userId) {
        JsonObject data = getIslandData(userId);
        return data != null ? data.get("level").getAsInt() : 0;
    }

    public static long getXp(String userId) {
        JsonObject data = getIslandData(userId);
        return data != null ? data.get("xp").getAsLong() : 0;
    }

    public static void addXp(String userId, long amount) {
        JsonObject all = loadFile(DATA_FILE);
        JsonObject data = all.getAsJsonObject(userId);
        long currentXp = data.get("xp").getAsLong();
        int currentLevel = data.get("level").getAsInt();
        long newXp = currentXp + amount;

        while (true) {
            long needed = xpRequired(currentLevel);
            if (newXp >= needed && currentLevel < 9999) {
                newXp -= needed;
                currentLevel++;
            } else {
                break;
            }
        }

        data.addProperty("xp", newXp);
        data.addProperty("level", currentLevel);
        saveFile(DATA_FILE, all);
    }

    public static long xpRequired(int level) {
        return 100L + (long) Math.pow(level, 1.5) * 10;
    }

    // ─── Bots ────────────────────────────────────────────────────────────────

    public static JsonArray getBots(String userId) {
        JsonObject all = loadFile(BOTS_FILE);
        if (all.has(userId)) return all.getAsJsonArray(userId);
        return new JsonArray();
    }

    public static boolean hasBot(String userId, String botType) {
        JsonArray bots = getBots(userId);
        for (JsonElement el : bots) {
            if (el.getAsString().equalsIgnoreCase(botType)) return true;
        }
        return false;
    }

    public static void addBot(String userId, String botType) {
        JsonObject all = loadFile(BOTS_FILE);
        JsonArray bots = all.has(userId) ? all.getAsJsonArray(userId) : new JsonArray();
        bots.add(botType.toLowerCase());
        all.add(userId, bots);
        saveFile(BOTS_FILE, all);
    }

    // ─── AFK days ────────────────────────────────────────────────────────────

    public static int getAfkDays(String userId) {
        JsonObject all = loadFile(AFK_FILE);
        if (!all.has(userId)) return 0;
        JsonObject data = all.getAsJsonObject(userId);
        return data.has("days") ? data.get("days").getAsInt() : 0;
    }

    public static void addAfkDays(String userId, int days) {
        JsonObject all = loadFile(AFK_FILE);
        JsonObject data = all.has(userId) ? all.getAsJsonObject(userId) : new JsonObject();
        int current = data.has("days") ? data.get("days").getAsInt() : 0;
        data.addProperty("days", current + days);
        all.add(userId, data);
        saveFile(AFK_FILE, all);
    }

    public static boolean useAfkDay(String userId) {
        JsonObject all = loadFile(AFK_FILE);
        if (!all.has(userId)) return false;
        JsonObject data = all.getAsJsonObject(userId);
        int days = data.has("days") ? data.get("days").getAsInt() : 0;
        if (days <= 0) return false;
        data.addProperty("days", days - 1);
        saveFile(AFK_FILE, all);
        return true;
    }

    // ─── Island Daily ─────────────────────────────────────────────────────────

    public static long getDailyCoins(String userId) {
        JsonObject data = getIslandData(userId);
        return data != null && data.has("dailyCoins") ? data.get("dailyCoins").getAsLong() : 0;
    }

    public static void addDailyCoins(String userId, long amount) {
        JsonObject all = loadFile(DATA_FILE);
        JsonObject data = all.getAsJsonObject(userId);
        long current = data.has("dailyCoins") ? data.get("dailyCoins").getAsLong() : 0;
        data.addProperty("dailyCoins", current + amount);
        saveFile(DATA_FILE, all);
    }

    public static long getLastDailyClaim(String userId) {
        JsonObject data = getIslandData(userId);
        return data != null && data.has("lastDailyClaim") ? data.get("lastDailyClaim").getAsLong() : 0;
    }

    public static boolean canClaimIslandDaily(String userId) {
        return System.currentTimeMillis() - getLastDailyClaim(userId) >= 24 * 60 * 60 * 1000L;
    }

    public static void setLastDailyClaim(String userId) {
        JsonObject all = loadFile(DATA_FILE);
        JsonObject data = all.getAsJsonObject(userId);
        data.addProperty("lastDailyClaim", System.currentTimeMillis());
        saveFile(DATA_FILE, all);
    }
}