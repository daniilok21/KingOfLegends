package io.github.some_example_name;

public class GameSettings {
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    // Игровые константы
    public static final int CUBE_SIZE = 50;
    public static final float MOVE_FORCE = 8f;
    public static final float JUMP_FORCE = 15f;
    public static final float MAX_VELOCITY = 20f;

    // Сетевые настройки
    public static final int PORT = 9090;
    public static final int FRAME_RATE = 60;
    public static final int FRAME_DELAY_MS = 1000 / FRAME_RATE;

    // физика
    public static final float STEP_TIME = 1f / 60f;
    public static final int VELOCITY_ITERATIONS = 8;
    public static final int POSITION_ITERATIONS = 3;
    public static final float SCALE = 0.05f;

    // Платформы
    public static final int PLATFORM_WIDTH = 200;
    public static final int PLATFORM_HEIGHT = 30;

    // UI для Android
    public static final int BUTTON_WIDTH = 150;
    public static final int BUTTON_HEIGHT = 150;
    public static final float BUTTON_ALPHA = 0.7f;
}
