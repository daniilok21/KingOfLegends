package io.github.some_example_name.game;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    // Серверный кубик (красный)
    public int serverCubeX = 100;
    public int serverCubeY = 300;

    // Клиентский кубик (синий)
    public int clientCubeX = 100;
    public int clientCubeY = 200;

    public void update() {
        // Любая дополнительная логика игры
    }

    public GameState clone() {
        GameState copy = new GameState();
        copy.serverCubeX = this.serverCubeX;
        copy.serverCubeY = this.serverCubeY;
        copy.clientCubeX = this.clientCubeX;
        copy.clientCubeY = this.clientCubeY;
        return copy;
    }
}
