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
    private float hitStunTimer = 0f;
    private float hitImmunityTimer = 0f;
    private boolean isInHitStun = false;
    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private AttackDirection currentAttackDirection = AttackDirection.SIDE;
    private Rectangle attackHitbox = new Rectangle();
    private boolean facingRight = true;
    private int health = 100;
    private int maxHealth = 100;

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
        if (isInHitStun) {
            hitStunTimer -= delta;
            if (hitStunTimer <= 0) {
                endHitStun();
            }
        }
        dodgeCooldown -= delta;
        hitImmunityTimer -= delta;
        if (hitImmunityTimer < 0) hitImmunityTimer = 0;
        if (dodgeCooldown < 0) dodgeCooldown = 0;

        checkGroundStatus();
        updateFacingDirection();
    }
    public void applyHitStun(float duration) {
        if (hitImmunityTimer > 0) return;

        isInHitStun = true;
        hitStunTimer = duration;
        hitImmunityTimer = GameSettings.HIT_IMMUNITY_DURATION;
    }
    private void endHitStun() {
        isInHitStun = false;
        hitStunTimer = 0f;
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
        if (isAttacking && !canAttack()) return;

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

    public boolean checkHit(PlayerObject target) {
        if (!isAttacking || target.hasHitImmunity()) return false;

        updateAttackHitbox();

        if (attackHitbox.overlaps(target.getBounds())) {
            System.out.println("Попал. Атака: " + currentAttackDirection);

            int damage = calculateDamage();
            applyKnockback(target, damage);

            return true;
        }
        return false;
    }

    private int calculateDamage() {
        switch (currentAttackDirection) {
            case SIDE:
                return 4;
            case UP:
                return 10;
            case DOWN:
                return 6;
            default:
                return 4;
        }
    }

    private void applyKnockback(PlayerObject target, int damage) {
        Vector2 knockbackDirection = new Vector2();
        float forse = 1000f;

        switch (currentAttackDirection) {
            case SIDE:
                knockbackDirection.set(facingRight ? 1 : -1, 0.3f);
                break;
            case UP:
                knockbackDirection.set(0, 1);
                forse = 1800f;
                break;
            case DOWN:
                knockbackDirection.set(facingRight ? 0.3f : -0.3f, -0.7f);
                forse = 1200f;
                break;
        }

        knockbackDirection.nor();

        float bonusForceWithHealth = 1 + (100 - target.getHealth()) / 100f;
        float totalForce = forse * bonusForceWithHealth;

        target.applyHitStun(GameSettings.HIT_STUN_DURATION);
        target.takeDamage(damage);

        Vector2 knockbackImpulse = knockbackDirection.scl(totalForce);
        target.getBody().applyLinearImpulse(knockbackImpulse, target.getBody().getWorldCenter(), true);

        System.out.println("ОТДАЧА. Сила = " + totalForce + ", Направление = " + knockbackDirection);
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
    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
        System.out.println("Игрок получил " + damage + " урона. HP = " + health);
    }

    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }

    public boolean isDead() {
        return health <= 0;
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
    public boolean getFacingRight() {
        return facingRight;
    }

    public void stopAttacking() {
        isAttacking = false;
        attackTimer = 0f;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public boolean isInHitStun() {
        return isInHitStun;
    }

    public boolean hasHitImmunity() {
        return hitImmunityTimer > 0;
    }

    public boolean canMove() {
        return !isInHitStun && !isDodging;
    }

    public boolean canJump() {
        return !isInHitStun && jumpsRemaining > 0;
    }

    public boolean canAttack() {
        return !isInHitStun && !isAttacking;
    }

    public boolean canDodge() {
        return !isInHitStun && dodgeCooldown <= 0 && !isDodging;
    }

    public boolean canReceiveInput() {
        return !isInHitStun;
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
