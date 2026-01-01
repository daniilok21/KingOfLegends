package io.github.some_example_name;

public class GameSettings {
    // Размеры экрана
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    // Настройки игрока
    public static final int PLAYER_WIDTH = 64;
    public static final int PLAYER_HEIGHT = 64;
    public static final float PLAYER_MOVE_FORCE = 500f;
    public static final float PLAYER_JUMP_FORCE = 25f;
    public static final float PLAYER_MAX_VELOCITY = 10f;

    // Гравитация
    public static final float GRAVITY = -100f;

    // Сетевые настройки
    public static final int PORT = 9090;
    public static final int FRAME_RATE = 60;
    public static final int FRAME_DELAY_MS = 1000 / FRAME_RATE;

    // Уворот
    public static final float DODGE_FORCE = 1500f;
    public static final float DODGE_DURATION = 0.3f;
    public static final float DODGE_COOLDOWN = 1.0f;

    // Прыжок
    public static final float JUMP_COOLDOWN = 0.2f;

    // Физика Box2D
    public static final float STEP_TIME = 1f / 60f;
    public static final int VELOCITY_ITERATIONS = 8;
    public static final int POSITION_ITERATIONS = 3;
    public static final float SCALE = 0.05f;

    // UI
    public static final int BUTTON_WIDTH = 150;
    public static final int BUTTON_HEIGHT = 150;
    public static final float BUTTON_ALPHA = 0.7f;

    // Категории коллизий
    public static final short PLAYER_BIT = 2;
    public static final short PLATFORM_BIT = 4;
}
