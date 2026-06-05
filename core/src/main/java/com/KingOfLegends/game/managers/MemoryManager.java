package com.KingOfLegends.game.managers;

import com.KingOfLegends.game.GameSettings;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class MemoryManager {
    private static final Preferences preferences = Gdx.app.getPreferences("User saves");

    public static void saveProfileName(String profileName) {
        preferences.putString("ProfileName", profileName);
        preferences.flush();
    }

    public static String loadProfileName() {
        return preferences.getString("ProfileName", "Unknown");
    }

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
