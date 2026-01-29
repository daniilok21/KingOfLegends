package io.github.some_example_name.game;
import java.io.Serializable;

public class PlayerInput implements Serializable {
    private static final long serialVersionUID = 1L;
    public boolean moveRight, moveLeft, jump, dodge, attack, attackUp, attackDown, wantToGoDown;

    public String playerName;
    public float x, y;
    public float vx, vy;
    public int health;
    public boolean facingRight;
    public boolean isAttacking;
    public AttackDirection attackDir;
    public boolean isDodging;
    public boolean isOnGround;
    public int jumpsRemaining;
    public boolean isClimbing;
}
