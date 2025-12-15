package io.github.some_example_name.game;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    // Серверное
    public int serverCubeX = 100;
    public int serverCubeY = 300;

    // Клиентское
    public int clientCubeX = 100;
    public int clientCubeY = 200;

    public void update() {

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
