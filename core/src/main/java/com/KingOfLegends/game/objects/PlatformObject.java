package com.KingOfLegends.game.objects;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import com.KingOfLegends.game.GameSettings;

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
        if (body != null && body.getFixtureList().size > 0) {
            Fixture fixture = body.getFixtureList().first();
            Filter filter = fixture.getFilterData();
            filter.maskBits = GameSettings.PLAYER_BIT;
            fixture.setFilterData(filter);
        }
    }
}
