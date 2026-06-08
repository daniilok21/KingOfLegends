package com.KingOfLegends.game.managers;

import com.KingOfLegends.game.GameSettings;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class MemoryManager {
    private static final Preferences preferences = Gdx.app.getPreferences("User saves");
    private static final String[] SKILL_KEYS = {
        "Skill_Health",
        "Skill_Knockback",
        "Skill_Luck",
        "Skill_Protect",
        "Skill_Critical",
        "Skill_MoreExp"
    };

    public static void saveSkillLevel(int skillId, int level) {
        if (skillId >= 0 && skillId < SKILL_KEYS.length) {
            preferences.putInteger(SKILL_KEYS[skillId], level);
            preferences.flush();
        }
    }

    public static int getSkillLevel(int skillId) {
        if (skillId >= 0 && skillId < SKILL_KEYS.length) {
            return preferences.getInteger(SKILL_KEYS[skillId], 0);
        }
        return 1;
    }

    public static void saveAllSkills(int[] levels) {
        if (levels == null || levels.length != SKILL_KEYS.length) return;

        for (int i = 0; i < SKILL_KEYS.length; i++) {
            preferences.putInteger(SKILL_KEYS[i], levels[i]);
        }
        preferences.flush();
    }

    public static int[] loadAllSkills() {
        int[] levels = new int[SKILL_KEYS.length];
        for (int i = 0; i < SKILL_KEYS.length; i++) {
            levels[i] = getSkillLevel(i);
        }
        return levels;
    }
    public static void saveProfileName(String profileName) {
        preferences.putString("ProfileName", profileName);
        preferences.flush();
    }
    public static void saveExp(int exp) {
        preferences.putInteger("PlayerExp", exp);
        preferences.flush();
    }

    public static int loadExp() {
        return preferences.getInteger("PlayerExp", 0);
    }

    public static void addExp(int value) {
        int current = loadExp();
        saveExp(current + value);
        preferences.flush();
    }
    public static String loadProfileName() {
        return preferences.getString("ProfileName", "Unknown");
    }
    public static int getUpgradePoint() { return preferences.getInteger("PlayerUpgradePoint", 0); }
    public static void add1UpgradePoint() {
        preferences.putInteger("PlayerUpgradePoint", getUpgradePoint() + 1);
        preferences.flush();
    }
    public static void saveUpgradePoint(int lvl) {
        preferences.putInteger("PlayerUpgradePoint", lvl);
        preferences.flush();
    }
    public static void saveLvl(int lvl) {
        preferences.putInteger("PlayerLVL", lvl);
        preferences.flush();
    }
    public static void add1Lvl() {
        preferences.putInteger("PlayerLVL", getLvl() + 1);
        preferences.flush();
    }

    public static int getLvl() {
        return preferences.getInteger("PlayerLVL", 0);
    }
    public static void setLastIP(String ip) {
        preferences.putString("LastIP", ip);
        preferences.flush();
    }
    public static String getLastIP() { return preferences.getString("LastIP", ""); }

    public static void saveSettings(float musicVolume, float soundVolume, boolean vibration) {
        preferences.putFloat("MusicVolume", musicVolume);
        preferences.putFloat("SoundVolume", soundVolume);
        preferences.putBoolean("Vibration", vibration);
        preferences.flush();
    }

    public static void loadSettings() {
        GameSettings.MUSIC_VOLUME = preferences.getFloat("MusicVolume", 0.2f);
        GameSettings.SOUND_VOLUME = preferences.getFloat("SoundVolume", 0.5f);
        GameSettings.VIBRATION_ENABLED = preferences.getBoolean("Vibration", true);
    }
}
