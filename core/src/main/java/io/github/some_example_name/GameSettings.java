package io.github.some_example_name;

public class GameSettings {
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    // Игровые константы
    public static final int CUBE_SIZE = 50;
    public static final int MOVE_SPEED = 5;

    // Сетевые настройки
    public static final int PORT = 9090;
    public static final int FRAME_RATE = 60;
    public static final int FRAME_DELAY_MS = 1000 / FRAME_RATE;

    // физика
    public static final float STEP_TIME = 1f / 60f;
    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 6;
    public static final float SCALE = 0.05f;
}
