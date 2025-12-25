package io.github.some_example_name.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.TimeUtils;

import io.github.some_example_name.GameSettings;

public class PlayerObject extends GameObject {

    public PlayerObject(int x, int y, int width, int height, String texturePath, World world) {
        super(texturePath, x, y, width, height, GameSettings.PLAYER_BIT, world);
        body.setLinearDamping(10);
    }

    @Override
    public void hit() {
    }

}
