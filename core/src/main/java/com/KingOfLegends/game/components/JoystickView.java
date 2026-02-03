package com.KingOfLegends.game.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import com.KingOfLegends.game.GameSettings;

public class JoystickView extends View {

    private Texture backgroundTexture;
    private Texture handleTexture;

    private Vector2 handlePosition = new Vector2();
    private Vector2 joystickCenter = new Vector2();

    private boolean isCaptured = false;
    private int capturedPointer = -1;

    public JoystickView(float x, float y, String bgTexturePath, String handleTexturePath) {
        super(x, y, GameSettings.JOYSTICK_BG_RADIUS * 2, GameSettings.JOYSTICK_BG_RADIUS * 2);

        backgroundTexture = new Texture(bgTexturePath);
        handleTexture = new Texture(handleTexturePath);

        joystickCenter.set(x + GameSettings.JOYSTICK_BG_RADIUS, y + GameSettings.JOYSTICK_BG_RADIUS);
        handlePosition.set(joystickCenter);
    }

    public boolean processTouch(float touchX, float touchY, boolean isTouchDown, int pointer) {
        if (isTouchDown) {
            if (!isCaptured && isHit(touchX, touchY)) {
                isCaptured = true;
                capturedPointer = pointer;
                updateHandlePosition(touchX, touchY);
                return true;
            }
            else if (isCaptured && capturedPointer == pointer) {
                updateHandlePosition(touchX, touchY);
                return true;
            }
        }
        else {
            if (isCaptured && capturedPointer == pointer) {
                reset();
                return true;
            }
        }
        return false;
    }

    private void updateHandlePosition(float touchX, float touchY) {
        Vector2 touch = new Vector2(touchX, touchY);
        Vector2 direction = new Vector2(touch).sub(joystickCenter);

        float distance = direction.len();
        if (distance > GameSettings.JOYSTICK_BG_RADIUS) {
            direction.nor().scl(GameSettings.JOYSTICK_BG_RADIUS);
        }

        handlePosition.set(joystickCenter).add(direction);
    }

    public void reset() {
        handlePosition.set(joystickCenter);
        isCaptured = false;
        capturedPointer = -1;
    }

    public Vector2 getDirection() {
        if (!isCaptured) return Vector2.Zero;

        Vector2 dir = new Vector2(handlePosition).sub(joystickCenter);
        if (dir.len() < 10f) {
            return Vector2.Zero;
        }
        return dir.nor();
    }

    public boolean isCaptured() {return isCaptured;}
    public int getCapturedPointer() {return capturedPointer;}

    public boolean isNeutral() { return !isCaptured || getDirection().len() < 0.1f; }
    public boolean isUp() { return isCaptured && getDirection().y > 0.5f; }
    public boolean isDown() { return isCaptured && getDirection().y < -0.5f; }
    public boolean isLeft() { return isCaptured && getDirection().x < -0.5f; }
    public boolean isRight() { return isCaptured && getDirection().x > 0.5f; }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(backgroundTexture, x, y, width, height);
        batch.draw(handleTexture,
            handlePosition.x - GameSettings.JOYSTICK_HANDLE_RADIUS,
            handlePosition.y - GameSettings.JOYSTICK_HANDLE_RADIUS,
            GameSettings.JOYSTICK_HANDLE_RADIUS * 2,
            GameSettings.JOYSTICK_HANDLE_RADIUS * 2);
    }

    @Override
    public boolean isHit(float tx, float ty) {
        float distance = Vector2.dst(tx, ty, joystickCenter.x, joystickCenter.y);
        return distance <= GameSettings.JOYSTICK_BG_RADIUS;
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        handleTexture.dispose();
    }
}
