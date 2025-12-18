package io.github.some_example_name.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import java.io.Serializable;

import io.github.some_example_name.GameSettings;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    public float serverCubeX = 100;
    public float serverCubeY = 300;
    public float clientCubeX = 100;
    public float clientCubeY = 200;

    public float serverVelocityX = 0;
    public float serverVelocityY = 0;
    public float clientVelocityX = 0;
    public float clientVelocityY = 0;

    public transient Body serverBody;
    public transient Body clientBody;

    public transient boolean serverMoveLeft = false;
    public transient boolean serverMoveRight = false;
    public transient boolean serverJump = false;
    public transient boolean clientMoveLeft = false;
    public transient boolean clientMoveRight = false;
    public transient boolean clientJump = false;

    public void updateFromPhysics() {
        if (serverBody != null) {
            Vector2 pos = serverBody.getPosition();
            Vector2 vel = serverBody.getLinearVelocity();
            serverCubeX = pos.x / GameSettings.SCALE;
            serverCubeY = pos.y / GameSettings.SCALE;
            serverVelocityX = vel.x;
            serverVelocityY = vel.y;
        }

        if (clientBody != null) {
            Vector2 pos = clientBody.getPosition();
            Vector2 vel = clientBody.getLinearVelocity();
            clientCubeX = pos.x / GameSettings.SCALE;
            clientCubeY = pos.y / GameSettings.SCALE;
            clientVelocityX = vel.x;
            clientVelocityY = vel.y;
        }
    }

    public void applyToPhysics() {
        if (serverBody != null) {
            serverBody.setTransform(
                serverCubeX * GameSettings.SCALE,
                serverCubeY * GameSettings.SCALE,
                0
            );
            serverBody.setLinearVelocity(serverVelocityX, serverVelocityY);
        }

        if (clientBody != null) {
            clientBody.setTransform(
                clientCubeX * GameSettings.SCALE,
                clientCubeY * GameSettings.SCALE,
                0
            );
            clientBody.setLinearVelocity(clientVelocityX, clientVelocityY);
        }
    }

    public GameState clone() {
        GameState copy = new GameState();
        copy.serverCubeX = this.serverCubeX;
        copy.serverCubeY = this.serverCubeY;
        copy.clientCubeX = this.clientCubeX;
        copy.clientCubeY = this.clientCubeY;
        copy.serverVelocityX = this.serverVelocityX;
        copy.serverVelocityY = this.serverVelocityY;
        copy.clientVelocityX = this.clientVelocityX;
        copy.clientVelocityY = this.clientVelocityY;
        return copy;
    }
}
