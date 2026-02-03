package com.KingOfLegends.game.net;

import com.KingOfLegends.game.game.GameState;
import com.KingOfLegends.game.game.AttackDirection;
import java.io.Serializable;

public class NetworkPacket implements Serializable {
    private static final long serialVersionUID = 1L;

    public GameState.GameStatus status;
    public float countdown;

    public String sName;
    public float sX, sY;
    public float sVX, sVY;
    public int sHealth;
    public int sLives;
    public int sJumps;
    public boolean sFacingRight;
    public boolean sIsAttacking;
    public AttackDirection sAttackDir;
    public boolean sInHitStun;
    public boolean sIsDodging;
    public boolean sOnGround;
    public boolean sIsOut;
    public boolean sNeedRespawn;
    public boolean sIsInvoking;
    public boolean sIsClimbing;
    public float sInvocationDuration;

    public String cName;
    public float cX, cY;
    public float cVX, cVY;
    public int cHealth;
    public int cLives;
    public int cJumps;
    public boolean cFacingRight;
    public boolean cIsAttacking;
    public AttackDirection cAttackDir;
    public boolean cIsDodging;
    public boolean cInHitStun;
    public boolean cOnGround;
    public boolean cIsOut;
    public boolean cNeedRespawn;
    public boolean cIsInvoking;
    public boolean cIsClimbing;
    public float cInvocationDuration;

    public float matchTimer;
    public int musicIndex;
}
