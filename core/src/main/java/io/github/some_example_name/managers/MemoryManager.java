package io.github.some_example_name.managers;

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
}
