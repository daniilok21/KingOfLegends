package io.github.some_example_name.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> jumpAnimation;
    private Animation<TextureRegion> attackAnimation;
    private Animation<TextureRegion> dodgeAnimation;
    private Animation<TextureRegion> hitAnimation;
    private Animation<TextureRegion> invocationAnimation;
    private Animation<TextureRegion> climbAnimation;

    private float stateTime = 0;
    private float pivotOffsetX = 48f;
    private float pivotOffsetY = 51f;
    private float[] ArrayPivotOffsetX;
    private float[] ArrayPivotOffsetY;
    private float visualScale = 1.6f;

    private int jumpsRemaining = 2;
    private boolean isOnGround = false;
    private boolean isDodging = false;
    private float dodgeTimer = 0f;
    private float dodgeCooldown = 0f;
    private float hitStunTimer = 0f;
    private float hitImmunityTimer = 0f;
    private boolean isInHitStun = false;
    private boolean isAttacking = false;
    private boolean isInvoking = false;
    private boolean isClimbing = false;
    private float invocationDuration = 1.0f;
    private float attackTimer = 0f;
    private float attackCooldown = 0f;
    private AttackDirection currentAttackDirection = AttackDirection.SIDE;
    private Rectangle attackHitbox = new Rectangle();
    private boolean facingRight = true;
    private int health = 100;
    private int maxHealth = 100;
    private boolean wantsToGoDown = false;
    private boolean canControl = true;

    private float lastVelocityY = 0f;
    private boolean wasMovingUpBeforeHit = false;
    private float headHitCooldown = 0.15f;

    public PlayerObject(int x, int y, int width, int height, String[] texturePaths, int[] framesPerAnimation, float[] ArrayPivotOffsetX, float[] ArrayPivotOffsetY, World world) {
        super(null, x, y, width, height, GameSettings.PLAYER_BIT, world, true, BodyDef.BodyType.DynamicBody);

        loadAnimations(texturePaths, framesPerAnimation);
        this.ArrayPivotOffsetX = ArrayPivotOffsetX;
        this.ArrayPivotOffsetY = ArrayPivotOffsetY;

        if (body != null && body.getFixtureList().size > 0) {
            Fixture fixture = body.getFixtureList().first();
            Filter filter = fixture.getFilterData();
            filter.maskBits = GameSettings.PLATFORM_BIT;
            fixture.setFilterData(filter);
        }
    }

    private void loadAnimations(String[] texturePaths, int[] framesPerAnimation) {
        Texture idleSheet = new Texture(texturePaths[0]);
        int idleFrameWidth = idleSheet.getWidth() / framesPerAnimation[0];
        int idleFrameHeight = idleSheet.getHeight();
        idleAnimation = createAnimation(idleSheet, idleFrameWidth, idleFrameHeight,
            framesPerAnimation[0], 0.15f);

        Texture runSheet = new Texture(texturePaths[1]);
        int runFrameWidth = runSheet.getWidth() / framesPerAnimation[1];
        int runFrameHeight = runSheet.getHeight();
        runAnimation = createAnimation(runSheet, runFrameWidth, runFrameHeight,
            framesPerAnimation[1], 0.1f);

        Texture jumpSheet = new Texture(texturePaths[2]);
        int jumpFrameWidth = jumpSheet.getWidth() / framesPerAnimation[2];
        int jumpFrameHeight = jumpSheet.getHeight();
        jumpAnimation = createAnimation(jumpSheet, jumpFrameWidth, jumpFrameHeight,
            framesPerAnimation[2], 0.1f);

        Texture attackSheet = new Texture(texturePaths[3]);
        int attackFrameWidth = attackSheet.getWidth() / framesPerAnimation[3];
        int attackFrameHeight = attackSheet.getHeight();
        attackAnimation = createAnimation(attackSheet, attackFrameWidth, attackFrameHeight,
            framesPerAnimation[3], 0.07f);

        Texture dodgeSheet = new Texture(texturePaths[4]);
        int dodgeFrameWidth = dodgeSheet.getWidth() / framesPerAnimation[4];
        int dodgeFrameHeight = dodgeSheet.getHeight();
        dodgeAnimation = createAnimation(dodgeSheet, dodgeFrameWidth, dodgeFrameHeight,
            framesPerAnimation[4], 0.1f);

        Texture hitSheet = new Texture(texturePaths[5]);
        int hitFrameWidth = hitSheet.getWidth() / framesPerAnimation[5];
        int hitFrameHeight = hitSheet.getHeight();
        hitAnimation = createAnimation(hitSheet, hitFrameWidth, hitFrameHeight,
            framesPerAnimation[5], 0.1f);

        Texture invocationSheet = new Texture(texturePaths[6]);
        int invocationWidth = invocationSheet.getWidth() / framesPerAnimation[6];
        int invocationHeight = invocationSheet.getHeight();
        invocationAnimation = createAnimation(invocationSheet, invocationWidth, invocationHeight,
            framesPerAnimation[6], 0.2f);

        Texture climbingSheet = new Texture(texturePaths[7]);
        int climbingWidth = invocationSheet.getWidth() / framesPerAnimation[7];
        int climbingHeight = invocationSheet.getHeight();
        climbAnimation = createAnimation(climbingSheet, climbingWidth, climbingHeight,
            framesPerAnimation[7], 0.1f);
    }

    private Animation<TextureRegion> createAnimation(Texture texture, int frameWidth, int frameHeight, int frameCount, float frameDuration) {
        TextureRegion[] frames = new TextureRegion[frameCount];

        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(texture, i * frameWidth, 0, frameWidth, frameHeight);
        }

        return new Animation<>(frameDuration, frames);
    }

    public void update(float delta) {
        stateTime += delta;
        if (isInvoking && stateTime >= invocationDuration) {
            isInvoking = false;
            stateTime = 0;
        }
        if (isDodging && dodgeTimer > 0) {
            dodgeTimer += delta;
            if (dodgeTimer >= GameSettings.DODGE_DURATION) {
                setIsDodging(false);
            }
        }
        if (isAttacking) {
            attackTimer += delta;
            if (attackTimer >= GameSettings.ATTACK_DURATION) {
                isAttacking = false;
                attackTimer = 0f;
            }
        }
        if (isInHitStun && hitStunTimer > 0) {
            hitStunTimer -= delta;
            if (hitStunTimer <= 0) {
                setInHitStun(false);
            }
        }
        dodgeCooldown -= delta;
        hitImmunityTimer -= delta;
        attackCooldown -= delta;
        headHitCooldown -= delta;
        if (hitImmunityTimer < 0) hitImmunityTimer = 0;
        if (dodgeCooldown < 0) dodgeCooldown = 0;
        if (attackCooldown < 0) attackCooldown = 0;
        if (headHitCooldown < 0) headHitCooldown = 0;

        checkGroundStatus(delta);
        updateFacingDirection();
    }

    @Override
    public void draw(SpriteBatch batch) {
        Animation<TextureRegion> currentAnim = idleAnimation;
        TextureRegion currentFrame = null;
        if (isInvoking) {
            pivotOffsetX = ArrayPivotOffsetX[6];
            pivotOffsetY = ArrayPivotOffsetY[6];

            float animDur = invocationAnimation.getAnimationDuration();
            float prepTime = 0.3f;
            float targetEndTime = invocationDuration - prepTime;
            float reverseStartTime = Math.max(animDur, targetEndTime - animDur);
            float frameTime;

            if (stateTime < animDur) {
                frameTime = stateTime;
            } else if (stateTime < reverseStartTime) {
                frameTime = animDur - 0.01f;
            } else if (stateTime < targetEndTime) {
                float elapsedInReverse = stateTime - reverseStartTime;
                float reverseDuration = targetEndTime - reverseStartTime;
                frameTime = animDur * (1.0f - (elapsedInReverse / reverseDuration));
            } else {
                frameTime = 0;
            }

            frameTime = Math.max(0, Math.min(frameTime, animDur - 0.01f));
            currentFrame = invocationAnimation.getKeyFrame(frameTime, false);
        } else if (isClimbing) {
            pivotOffsetX = ArrayPivotOffsetX[7];
            pivotOffsetY = ArrayPivotOffsetY[7];
            currentAnim = climbAnimation;
            currentFrame = climbAnimation.getKeyFrames()[0];
        }
        else if (isAttacking) {
            pivotOffsetX = ArrayPivotOffsetX[3];
            pivotOffsetY = ArrayPivotOffsetY[3];
            currentAnim = attackAnimation;
            currentFrame = attackAnimation.getKeyFrame(stateTime, false);
            if (attackAnimation.isAnimationFinished(stateTime)) {
                isAttacking = false;
                stateTime = 0;
                currentAnim = idleAnimation;
            }
        } else if (isDodging) {
            pivotOffsetX = ArrayPivotOffsetX[4];
            pivotOffsetY = ArrayPivotOffsetY[4];
            currentAnim = dodgeAnimation;
        } else if (isInHitStun) {
            pivotOffsetX = ArrayPivotOffsetX[5];
            pivotOffsetY = ArrayPivotOffsetY[5];
            currentAnim = hitAnimation;
        }
        else if (!isOnGround) {
            currentAnim = jumpAnimation;
            pivotOffsetX = ArrayPivotOffsetX[2];
            pivotOffsetY = ArrayPivotOffsetY[2];
            if (jumpAnimation.isAnimationFinished(stateTime)) {
                TextureRegion[] frames = jumpAnimation.getKeyFrames();
                currentFrame = frames[frames.length - 1];
            }
        } else if (Math.abs(body.getLinearVelocity().x) > 0.1f) {
            pivotOffsetX = ArrayPivotOffsetX[1];
            pivotOffsetY = ArrayPivotOffsetY[1];
            currentAnim = runAnimation;
        }

        if (currentFrame == null) {
            pivotOffsetX = ArrayPivotOffsetX[0];
            pivotOffsetY = ArrayPivotOffsetY[0];
            currentFrame = currentAnim.getKeyFrame(stateTime, true);
        }

        float frameWidth = currentFrame.getRegionWidth() * visualScale;
        float frameHeight = currentFrame.getRegionHeight() * visualScale;

        float hitboxCenterX = getX() + width / 2;
        float hitboxCenterY = getY() + height / 2;

        float drawX, drawY;

        if (facingRight) {
            drawX = hitboxCenterX - frameWidth / 2 + pivotOffsetX;
        } else {
            drawX = hitboxCenterX - frameWidth / 2 - pivotOffsetX;
        }

        drawY = hitboxCenterY - frameHeight / 2 + pivotOffsetY;

        batch.draw(currentFrame, drawX, drawY, frameWidth / 2, frameHeight / 2, frameWidth, frameHeight, facingRight ? 1.0f : -1.0f, 1.0f, 0);
    }

    public void applyHitStun(float duration) {
        if (hitImmunityTimer > 0) return;

        setInHitStun(true);
        hitStunTimer = duration;
        hitImmunityTimer = GameSettings.HIT_IMMUNITY_DURATION;
    }
    private void endHitStun() {
        setInHitStun(false);
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

    public void startInvocation() {
        startInvocation(1.0f);
    }

    public void startInvocation(float duration) {
        isInvoking = true;
        stateTime = 0;
        this.invocationDuration = duration;
    }

    public void startAttack(AttackDirection direction) {
        if (!canAttack()) return;

        isAttacking = true;
        attackTimer = 0f;
        stateTime = 0;
        attackCooldown = GameSettings.ATTACK_COOLDOWN;
        currentAttackDirection = direction;

        updateAttackHitbox();
    }

    private void updateAttackHitbox() {
        if (!isAttacking) return;

        float hitboxX = 0, hitboxY = 0;
        float hitboxWidth = 0, hitboxHeight = 0;

        switch (currentAttackDirection) {
            case SIDE:
                hitboxWidth = width + width;
                hitboxHeight = height;
                if (facingRight) {
                    hitboxX = getX();
                    hitboxY = getY();
                }
                else {
                    hitboxX = getX() - width;
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
        if (!isAttacking || target.hasHitImmunity() || attackAnimation.getKeyFrameIndex(stateTime) < attackAnimation.getKeyFrames().length - 2) return false;

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
        float forse = 40f;

        switch (currentAttackDirection) {
            case SIDE:
                knockbackDirection.set(facingRight ? 1 : -1, 0.3f);
                break;
            case UP:
                knockbackDirection.set(0, 1);
                forse = 55f;
                break;
            case DOWN:
                knockbackDirection.set(facingRight ? 0.3f : -0.3f, -0.7f);
                forse = 48f;
                break;
        }

        knockbackDirection.nor();

        float bonusForceWithHealth = 1 + (100 - target.getHealth()) / 100f;
        float totalForce = forse * bonusForceWithHealth;

        target.applyHitStun(GameSettings.HIT_STUN_DURATION);
        target.takeDamage(damage);

        Vector2 knockbackImpulse = knockbackDirection.scl(totalForce);
        target.getBody().applyLinearImpulse(knockbackImpulse, target.getBody().getWorldCenter(), true);
    }

    private void checkGroundStatus(float delta) {
        float velocityY = body.getLinearVelocity().y;
        float prevVelocityY = lastVelocityY;
        lastVelocityY = velocityY;

        if (prevVelocityY > 0.5f) {
            wasMovingUpBeforeHit = true;
        }

        if (Math.abs(velocityY) <= 0.1f) {
            if (!isOnGround) {
                if (wasMovingUpBeforeHit && headHitCooldown > 0) {
                    wasMovingUpBeforeHit = false;
                }
                else {
                    isOnGround = true;
                    jumpsRemaining = 2;
                }
            }
        }
        else {
            isOnGround = false;
            if (velocityY < -0.1f) {
                wasMovingUpBeforeHit = false;
            }
        }

        if (isOnGround && velocityY > 0.5f) {
            isOnGround = false;
        }
    }
    public void registerHeadHit() {
        wasMovingUpBeforeHit = true;
        headHitCooldown = 0.15f;
    }

    public boolean jump(float force) {
        if (jumpsRemaining > 0 && canControl) {
            if (isDodging) {
                setIsDodging(false);
                endDodge();
            }

            Vector2 currentVelocity = body.getLinearVelocity();
            body.setLinearVelocity(currentVelocity.x, 0);
            body.applyLinearImpulse(new Vector2(0, force), body.getWorldCenter(), true);
            jumpsRemaining--;
            stateTime = 0;
            isOnGround = false;
            System.out.println(jumpsRemaining);
            return true;
        }
        return false;
    }

    public boolean dodge(float directionX) {
        if (!canControl || dodgeCooldown > 0 || isDodging) {
            return false;
        }

        setIsDodging(true);
        dodgeTimer = 0.001f;
        dodgeCooldown = GameSettings.DODGE_COOLDOWN;

        if (directionX != 0) {
            Vector2 currentPos = body.getPosition();
            Vector2 s = new Vector2(directionX, 0).nor().scl(GameSettings.DODGE_DISTANCE * GameSettings.SCALE);
            Vector2 targetPos = new Vector2(currentPos).add(s);

            final Vector2 finalPos = new Vector2(targetPos);
            final float dirX = directionX;

            body.getWorld().rayCast((fixture, point, normal, fraction) -> {
                if ((fixture.getFilterData().categoryBits & GameSettings.PLATFORM_BIT) != 0) {
                    float margin = (width / 2f + 1) * GameSettings.SCALE;
                    finalPos.set(point.x - (dirX * margin), point.y);
                    return fraction;
                }
                return -1;
            }, currentPos, targetPos);

            body.setTransform(finalPos, body.getAngle());
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }
        else {
            body.setLinearVelocity(0, 0);
        }

        return true;
    }

    public void setCanControl(boolean canControl) {
        this.canControl = canControl;
        if (!canControl) {
            body.setLinearVelocity(0, 0);
        }
    }

    public boolean canControl() {
        return canControl;
    }
    public void setWantsToGoDown(boolean wantsToGoDown) {
        this.wantsToGoDown = wantsToGoDown;
    }

    public boolean wantsToGoDown() {
        return wantsToGoDown;
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

    public void setHealth(int health) {
        this.health = health;
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
        dodgeTimer = 0f;
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

    public float getHitImmunityTimer() { return hitImmunityTimer; }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public void stopAttacking() {
        isAttacking = false;
        attackTimer = 0f;
    }

    public void setAttacking(boolean attacking) {
        if (this.isAttacking != attacking) {
            this.isAttacking = attacking;
            if (attacking) stateTime = 0;
        }
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public boolean isInvoking() {
        return isInvoking;
    }

    public boolean isInvocationFinished() {
        return stateTime >= invocationDuration;
    }

    public void setIsInvoking(boolean invoking) {
        if (this.isInvoking && !invoking) {
            stateTime = 0;
        }
        if (invoking && !this.isInvoking) {
            stateTime = 0;
        }
        this.isInvoking = invoking;
    }

    public void setIsInvoking(boolean invoking, float duration) {
        if (this.isInvoking && !invoking) {
            stateTime = 0;
        }
        if (invoking && !this.isInvoking) {
            stateTime = 0;
        }
        this.isInvoking = invoking;
        this.invocationDuration = duration;
    }

    public void setInvocationDuration(float duration) {
        this.invocationDuration = duration;
    }

    public float getInvocationDuration() {
        return invocationDuration;
    }

    public void setStateTime(float time) {
        this.stateTime = time;
    }

    public float getStateTime() {
        return stateTime;
    }

    public boolean isInHitStun() {
        return isInHitStun;
    }

    public boolean isClimbing() {
        return isClimbing;
    }

    public void setIsClimbing(boolean isClimbing) {
        this.isClimbing = isClimbing;
    }

    public void setInHitStun(boolean hitStun) {
        if (this.isInHitStun != hitStun) {
            this.isInHitStun = hitStun;
            if (hitStun) {
                stateTime = 0;
            } else {
                hitStunTimer = 0;
            }
        }
    }

    public void setIsDodging(boolean dodging) {
        if (this.isDodging != dodging) {
            this.isDodging = dodging;
            if (dodging) {
                stateTime = 0;
            } else {
                dodgeTimer = 0;
                endDodge();
            }
        }
    }

    public void setIsOnGround(boolean onGround) {
        if (this.isOnGround != onGround) {
            this.isOnGround = onGround;
            if (!onGround) stateTime = 0;
        }
    }

    public boolean hasHitImmunity() {
        return hitImmunityTimer > 0;
    }

    public boolean canMove() {
        return canControl && !isInHitStun && !isDodging && !isAttacking && !isInvoking;
    }
    public void setHitImmunityTimer(float hitImmunityTimer) {
        this.hitImmunityTimer = hitImmunityTimer;
    }

    public boolean canJump() {
        return !isAttacking && !isInHitStun && jumpsRemaining > 0 && !isInvoking;
    }

    public boolean canAttack() {
        return canControl && !isInHitStun && !isAttacking && attackCooldown == 0 && !isInvoking;
    }

    public boolean canDodge() {
        return !isAttacking && canControl && !isInHitStun && dodgeCooldown <= 0 && !isDodging && !isInvoking;
    }

    public boolean canReceiveInput() {
        return canControl && !isInHitStun && !isInvoking;
    }

    public AttackDirection getCurrentAttackDirection() {
        return currentAttackDirection;
    }

    public Rectangle getAttackHitbox() {
        updateAttackHitbox();
        return attackHitbox;
    }

    public void setPivotOffsetX(float offset) {
        this.pivotOffsetX = offset;
    }

    public void setPivotOffsetY(float offset) {
        this.pivotOffsetY = offset;
    }

    public void setVisualScale(float scale) {
        this.visualScale = scale;
    }

    public float getPivotOffsetX() {
        return pivotOffsetX;
    }

    public float getPivotOffsetY() {
        return pivotOffsetY;
    }

    public void setJumpsRemaining(int jumps) {
        if (this.jumpsRemaining > jumps) {
            stateTime = 0;
            isOnGround = false;
        }
        this.jumpsRemaining = jumps;
    }

    public Rectangle getPlayerHitbox() {
        return new Rectangle(getX(), getY(), width, height);
    }


    @Override
    public void hit() {
        if (isDodging) {
            setIsDodging(false);
            body.setGravityScale(1.0f);
        }
    }
}
