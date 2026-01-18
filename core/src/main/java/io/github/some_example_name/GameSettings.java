package io.github.some_example_name;

public class GameSettings {
    // Размеры экрана
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    // Настройки игрока
    public static final int PLAYER_WIDTH = 64;
    public static final int PLAYER_HEIGHT = 64;
    public static final float PLAYER_MOVE_FORCE = 500f;
    public static final float PLAYER_JUMP_FORCE = 35f;
    public static final float PLAYER_MAX_VELOCITY = 10f;
    public static final float PLAYER_MAX_KNOCKBACK_VELOCITY = 60f;
    public static final float HIT_STUN_DURATION = 0.3f;
    public static final float HIT_IMMUNITY_DURATION = 0.2f;
    // Гравитация
    public static final float GRAVITY = -100f;

    // Сетевые настройки
    public static final int PORT = 9090;
    public static final int FRAME_RATE = 60;
    public static final int FRAME_DELAY_MS = 1000 / FRAME_RATE;
    public static final float FRAME_DELAY_1_60 = 1f / 60f;

    // Уворот
    public static final float DODGE_DURATION = 0.3f;
    public static final float DODGE_COOLDOWN = 1.0f;
    public static final float DODGE_DISTANCE = 100f;

    // Атака
    public static final float ATTACK_DURATION = 0.3f;
    public static final float ATTACK_COOLDOWN = 0.7f;

    // Прыжок
    public static final float JUMP_COOLDOWN = 0.2f;

    // Физика Box2D
    public static final float STEP_TIME = 1f / 60f;
    public static final int VELOCITY_ITERATIONS = 8;
    public static final int POSITION_ITERATIONS = 3;
    public static final float SCALE = 0.05f;

    // UI
    public static final int BUTTON_WIDTH = 80;
    public static final int BUTTON_HEIGHT = 80;
    public static final float BUTTON_ALPHA = 0.7f;
    public static final float JOYSTICK_BG_RADIUS = 80f;
    public static final float JOYSTICK_HANDLE_RADIUS = 30f;

    // Категории коллизий
    public static final short PLAYER_BIT = 2;
    public static final short PLATFORM_BIT = 4;

    // Верхняя панель
    public static final int TOP_PANEL_HEIGHT = 100;
    public static final int PLAYER_MAX_LIVES = 3;
    public static final float OUT_OF_BOUNDS_RESPAWN_TIME = 3.0f;
    public static final int MATCH_DURATION_SECONDS = 600;

    // Границы арены
    public static final float ARENA_LEFT_BOUND = -PLAYER_WIDTH;
    public static final float ARENA_RIGHT_BOUND = SCREEN_WIDTH + PLAYER_WIDTH;
    public static final float ARENA_TOP_BOUND = SCREEN_HEIGHT + PLAYER_HEIGHT;
    public static final float ARENA_BOTTOM_BOUND = -PLAYER_HEIGHT;

    // Начальная растановка игроков
    public static final int START_PLAYER_SERVER_X = 100;
    public static final int START_PLAYER_SERVER_Y = 400;
    public static final int START_PLAYER_CLIENT_X = SCREEN_WIDTH - 150;
    public static final int START_PLAYER_CLIENT_Y = 400;
}
