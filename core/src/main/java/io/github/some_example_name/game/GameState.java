package io.github.some_example_name.game;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    public float cubeX = 100;
    public float cubeY = 200;
    public long timestamp;

    public void update() {
        // Логика (если нужна)
        timestamp = System.currentTimeMillis();
    }
}
