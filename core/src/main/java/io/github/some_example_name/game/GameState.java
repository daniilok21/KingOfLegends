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
    public int serverHealth = 100;
    public int clientHealth = 100;

    public boolean serverAttacking = false;
    public AttackDirection serverAttackDirection = AttackDirection.SIDE;
    public boolean clientAttacking = false;
    public AttackDirection clientAttackDirection = AttackDirection.SIDE;

    public enum GameStatus {
        WAITING, // ждем
        COUNTDOWN, // отсчет
        PLAYING // играем
    }
    public GameStatus gameStatus = GameStatus.WAITING;
    public float countdownTimer = 3.0f;

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

    public void applyToPhysics(float alpha) {
        if (serverBody != null) {
            Vector2 targetPos = new Vector2(
                serverCubeX * GameSettings.SCALE,
                serverCubeY * GameSettings.SCALE
            );
            Vector2 currentPos = serverBody.getPosition();
            Vector2 newPos = currentPos.lerp(targetPos, alpha);

            serverBody.setTransform(newPos, 0);
            serverBody.setLinearVelocity(serverVelocityX, serverVelocityY);
        }

        if (clientBody != null) {
            Vector2 targetPos = new Vector2(
                clientCubeX * GameSettings.SCALE,
                clientCubeY * GameSettings.SCALE
            );
            Vector2 currentPos = clientBody.getPosition();
            Vector2 newPos = currentPos.lerp(targetPos, alpha);

            clientBody.setTransform(newPos, 0);
            clientBody.setLinearVelocity(clientVelocityX, clientVelocityY);
        }
    }

    public GameState clone() {
        GameState copy = new GameState();
        copy.gameStatus = this.gameStatus;
        copy.countdownTimer = this.countdownTimer;
        copy.serverCubeX = this.serverCubeX;
        copy.serverCubeY = this.serverCubeY;
        copy.clientCubeX = this.clientCubeX;
        copy.clientCubeY = this.clientCubeY;
        copy.serverVelocityX = this.serverVelocityX;
        copy.serverVelocityY = this.serverVelocityY;
        copy.clientVelocityX = this.clientVelocityX;
        copy.clientVelocityY = this.clientVelocityY;
        copy.serverHealth = this.serverHealth;
        copy.clientHealth = this.clientHealth;
        copy.serverAttacking = this.serverAttacking;
        copy.serverAttackDirection = this.serverAttackDirection;
        copy.clientAttacking = this.clientAttacking;
        copy.clientAttackDirection = this.clientAttackDirection;
        return copy;
    }
}
