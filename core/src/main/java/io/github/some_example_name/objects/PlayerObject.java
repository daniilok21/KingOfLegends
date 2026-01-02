package io.github.some_example_name.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

import io.github.some_example_name.GameSettings;

public class PlayerObject extends GameObject {

    private int jumpsRemaining = 2;
    private boolean isOnGround = false;

    private boolean isDodging = false;
    private float jumpCooldown = 0f;
    private float dodgeTimer = 0f;
    private float dodgeCooldown = 0f;
    private Vector2 dodgeDirection = new Vector2();

    public PlayerObject(int x, int y, int width, int height, String texturePath, World world) {
        super(texturePath, x, y, width, height, GameSettings.PLAYER_BIT, world, true, BodyDef.BodyType.DynamicBody);
    }

    public void update(float delta) {
        if (isDodging) {
            dodgeTimer += delta;
            if (dodgeTimer >= GameSettings.DODGE_DURATION) {
                isDodging = false;
                endDodge();
                dodgeTimer = 0f;
                Vector2 vel = body.getLinearVelocity();
                vel.x *= 0.3f;
                body.setLinearVelocity(vel);
            }
        }
        jumpCooldown -= delta;
        dodgeCooldown -= delta;
        if (jumpCooldown < 0) jumpCooldown = 0;
        if (dodgeCooldown < 0) dodgeCooldown = 0;

        checkGroundStatus();
    }

    private void checkGroundStatus() {
        float velocityY = body.getLinearVelocity().y;
        if (Math.abs(velocityY) <= 0.1f) {
            if (!isOnGround) {
                isOnGround = true;
                jumpsRemaining = 2;
            }
        }
        else {
            isOnGround = false;
        }
    }

    public boolean jump(float force) {
        if (jumpsRemaining > 0 && jumpCooldown == 0f) {
            if (isDodging) {
                isDodging = false;
                dodgeTimer = 0f;
                endDodge();
            }
            jumpCooldown = GameSettings.JUMP_COOLDOWN;
            Vector2 currentVelocity = body.getLinearVelocity();
            body.setLinearVelocity(currentVelocity.x, 0);
            body.applyLinearImpulse(new Vector2(0, force), body.getWorldCenter(), true);
            jumpsRemaining--;
            return true;
        }
        return false;
    }

    public boolean dodge(float directionX) {
        if (dodgeCooldown > 0 || isDodging || directionX == 0) {
            return false;
        }

        isDodging = true;
        dodgeTimer = 0f;
        dodgeCooldown = GameSettings.DODGE_COOLDOWN;
        dodgeDirection.set(directionX, 0).nor();

        Vector2 dodgeImpulse = new Vector2(dodgeDirection).scl(GameSettings.DODGE_FORCE);
        body.applyLinearImpulse(dodgeImpulse, body.getWorldCenter(), true);

        body.setGravityScale(0.2f);

        return true;
    }

    private void endDodge() {
        body.setGravityScale(1.0f);
    }

    public int getJumpsRemaining() {
        return jumpsRemaining;
    }

    public boolean isOnGround() {
        return isOnGround;
    }

    public boolean isDodging() {
        return isDodging;
    }

    public float getDodgeCooldown() {
        return dodgeCooldown;
    }

    public boolean canDodge() {
        return dodgeCooldown <= 0 && !isDodging;
    }

    @Override
    public void hit() {
        if (isDodging) {
            isDodging = false;
            dodgeTimer = 0f;
            body.setGravityScale(1.0f);
        }
    }
}
