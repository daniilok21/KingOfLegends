package com.KingOfLegends.game;

public class GameSettings {
    // Размеры экрана
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    // Настройки игрока
    public static final int PLAYER_WIDTH = 64;
    public static final int PLAYER_HEIGHT = 100;
    public static final float PLAYER_MOVE_FORCE = 500f;
    public static final float PLAYER_JUMP_FORCE = 60f;
    public static final float PLAYER_MAX_VELOCITY = 20f;
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
    public static final int BUTTON_WIDTH = 120;
    public static final int BUTTON_HEIGHT = 120;
    public static final float BUTTON_ALPHA = 0.7f;
    public static final float JOYSTICK_BG_RADIUS = 100f;
    public static final float JOYSTICK_HANDLE_RADIUS = 40f;

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
    public static final int START_PLAYER_SERVER_X = 485;
    public static final int START_PLAYER_SERVER_Y = 390;
    public static final int START_PLAYER_CLIENT_X = SCREEN_WIDTH - 485;
    public static final int START_PLAYER_CLIENT_Y = 390;

    // Настройки звука и вибрации
    public static float MUSIC_VOLUME = 0.2f;
    public static float SOUND_VOLUME = 0.5f;
    public static boolean VIBRATION_ENABLED = true;

    // Баффы от улучшений
    public static int[] HEALTH_BUFF = {0, 10, 20, 30, 40, 50}; // + к HP
    public static float[] KNOCKBACK_BUFF = {1, 1.05f, 1.1f, 1.15f, 1.2f, 1.25f}; // множитель к отдаче
    public static float[] LUCK_BUFF = {0, 0.02f, 0.03f, 0.05f, 0.07f, 0.09f}; // вероятность 0-1.0 (не включительно)
    public static float[] PROTECT_BUFF = {0, 0.03f, 0.05f, 0.07f, 0.09f, 0.9f}; // вероятность 0-1.0 (не включительно)
    public static float[] CRITICAL_BUFF = {0, 0.05f, 0.08f, 0.1f, 0.14f, 1f}; // вероятность 0-1.0  (не включительно)
    public static float[] MORE_EXP_BUFF = {1, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f}; // множитель
    public static final int EXP_WIN = 100;
    public static final int EXP_LOSE = 30;

}
