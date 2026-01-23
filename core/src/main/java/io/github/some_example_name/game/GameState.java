package io.github.some_example_name.game;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    public float serverCubeX = 100;
    public float serverCubeY = 300;
    public float clientCubeX = 100;
    public float clientCubeY = 200;

    public boolean serverAttacking = false;
    public AttackDirection serverAttackDirection = AttackDirection.SIDE;
    public boolean clientAttacking = false;
    public AttackDirection clientAttackDirection = AttackDirection.SIDE;

    public enum GameStatus {
        WAITING,
        COUNTDOWN,
        PLAYING,
        FINISHED
    }
    public GameStatus gameStatus = GameStatus.WAITING;
    public float countdownTimer = 3.0f;
}
