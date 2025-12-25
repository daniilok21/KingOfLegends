package io.github.some_example_name.objects;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

import io.github.some_example_name.GameSettings;

public class PlatformObject extends GameObject {

    public PlatformObject(float x, float y, float width, float height,
                          String texturePath, World world) {
        super(texturePath,
            (int) (x + width / 2),
            (int) (y + height / 2),
            (int) width,
            (int) height,
            GameSettings.PLATFORM_BIT,
            world,
            true,
            BodyDef.BodyType.StaticBody);
    }
}
