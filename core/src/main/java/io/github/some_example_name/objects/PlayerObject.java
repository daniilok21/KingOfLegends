package io.github.some_example_name.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import io.github.some_example_name.GameSettings;
import io.github.some_example_name.game.AttackDirection;

public class PlayerObject extends GameObject {

    private int jumpsRemaining = 2;
    private boolean isOnGround = false;

    private boolean isDodging = false;
    private float dodgeTimer = 0f;
    private float dodgeCooldown = 0f;

    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private AttackDirection currentAttackDirection = AttackDirection.SIDE;
    private Rectangle attackHitbox = new Rectangle();
    private boolean facingRight = true;

    public PlayerObject(int x, int y, int width, int height, String texturePath, World world) {
        super(texturePath, x, y, width, height, GameSettings.PLAYER_BIT, world, true, BodyDef.BodyType.DynamicBody);
        if (body != null && body.getFixtureList().size > 0) {
            Fixture fixture = body.getFixtureList().first();
            Filter filter = fixture.getFilterData();
            filter.maskBits = GameSettings.PLATFORM_BIT;
            fixture.setFilterData(filter);
        }
    }

    public void update(float delta) {
        if (isDodging) {
            dodgeTimer += delta;
            if (dodgeTimer >= GameSettings.DODGE_DURATION) {
                isDodging = false;
                endDodge();
                dodgeTimer = 0f;
            }
        }
        if (isAttacking) {
            attackTimer += delta;
            if (attackTimer >= GameSettings.ATTACK_DURATION) {
                isAttacking = false;
                attackTimer = 0f;
            }
        }
        dodgeCooldown -= delta;
        if (dodgeCooldown < 0) dodgeCooldown = 0;

        checkGroundStatus();
        updateFacingDirection();
    }

    private void updateFacingDirection() {
        float velocityX = body.getLinearVelocity().x;

        if (velocityX > 0.1f) {
            facingRight = true;
        }
        else if (velocityX < -0.1f) {
            facingRight = false;
        }
    }



    public void startAttack(AttackDirection direction) {
        if (isAttacking) return;

        isAttacking = true;
        attackTimer = 0f;
        currentAttackDirection = direction;

        updateAttackHitbox();
    }

    private void updateAttackHitbox() {
        if (!isAttacking) return;

        float hitboxX = 0, hitboxY = 0;
        float hitboxWidth = 0, hitboxHeight = 0;

        switch (currentAttackDirection) {
            case SIDE:
                hitboxWidth = width + width / 2f;
                hitboxHeight = height;
                if (facingRight) {
                    hitboxX = getX();
                    hitboxY = getY();
                }
                else {
                    hitboxX = getX() - width / 2f;
                    hitboxY = getY();
                }
                break;

            case UP:
                hitboxWidth = width;
                hitboxHeight = height + height / 2f;
                hitboxX = getX();
                hitboxY = getY();
                break;

            case DOWN:
                hitboxWidth = width;
                hitboxHeight = height + height / 2f;
                hitboxX = getX();
                hitboxY = getY() - height / 2f;
                break;
        }

        attackHitbox.set(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
    }

    public boolean checkHit(PlayerObject player) {
        if (!isAttacking) return false;

        updateAttackHitbox();

        if (attackHitbox.overlaps(player.getBounds())) {
            System.out.println("Попал. Атака: " + currentAttackDirection);
            return true;
        }
        return false;
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
        if (jumpsRemaining > 0) {
            if (isDodging) {
                isDodging = false;
                dodgeTimer = 0f;
                endDodge();
            }

            Vector2 currentVelocity = body.getLinearVelocity();
            body.setLinearVelocity(currentVelocity.x, 0);
            body.applyLinearImpulse(new Vector2(0, force), body.getWorldCenter(), true);
            jumpsRemaining--;
            return true;
        }
        return false;
    }

    public boolean dodge(float directionX) {
        if (dodgeCooldown > 0 || isDodging) {
            return false;
        }

        isDodging = true;
        dodgeTimer = 0f;
        dodgeCooldown = GameSettings.DODGE_COOLDOWN;

        if (directionX != 0) {
            Vector2 currentPos = body.getPosition();
            Vector2 s = new Vector2(directionX, 0).nor().scl(GameSettings.DODGE_DISTANCE * GameSettings.SCALE);
            Vector2 newPos = new Vector2(currentPos).add(s);
            body.setTransform(newPos, body.getAngle());
        }
        else {
            body.setLinearVelocity(0, 0);
        }

//        body.setGravityScale(0.2f);

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
    public boolean getFacingRight() {
        return facingRight;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public AttackDirection getCurrentAttackDirection() {
        return currentAttackDirection;
    }

    public Rectangle getAttackHitbox() {
        updateAttackHitbox();
        return attackHitbox;
    }
    public Rectangle getPlayerHitbox() {
        return new Rectangle(getX(), getY(), width, height);
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
