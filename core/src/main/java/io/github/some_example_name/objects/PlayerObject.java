package io.github.some_example_name.objects;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;


public class PlayerObject extends GameObject {
    public PlayerObject(String texturePath, int x, int y, int width, int height,
                        short cBits, World world, boolean isRedPlayer) {
        super(texturePath, x, y, width, height, cBits, world, true, BodyDef.BodyType.DynamicBody);

    }
}
