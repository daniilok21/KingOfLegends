package io.github.some_example_name.net;

import io.github.some_example_name.game.GameState;
import io.github.some_example_name.game.AttackDirection;
import java.io.Serializable;

public class NetworkPacket implements Serializable {
    private static final long serialVersionUID = 1L;

    public GameState.GameStatus status;
    public float countdown;

    public float sX, sY;
    public float sVX, sVY;
    public int sHealth;
    public int sLives;
    public boolean sFacingRight;
    public boolean sIsAttacking;
    public AttackDirection sAttackDir;
    public boolean sInHitStun;
    public boolean sIsOut;
    public boolean sNeedRespawn;

    public float cX, cY;
    public float cVX, cVY;
    public int cHealth;
    public int cLives;
    public boolean cFacingRight;
    public boolean cIsAttacking;
    public AttackDirection cAttackDir;
    public boolean cIsDodging;
    public boolean cInHitStun;
    public boolean cIsOut;
    public boolean cNeedRespawn;

    public float matchTimer;
}
