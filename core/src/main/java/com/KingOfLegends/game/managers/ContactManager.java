package com.KingOfLegends.game.managers;

import com.badlogic.gdx.physics.box2d.*;

import com.KingOfLegends.game.GameSettings;
import com.KingOfLegends.game.objects.GameObject;
import com.KingOfLegends.game.objects.OneWayPlatformObject;
import com.KingOfLegends.game.objects.PlayerObject;

public class ContactManager {

    World world;

    public ContactManager(World world) {
        this.world = world;

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();

                GameObject objA = (GameObject) fixA.getUserData();
                GameObject objB = (GameObject) fixB.getUserData();

                if (objA != null && objB != null) {
                    handleBeginContact(objA, objB, contact);
                }
            }

            @Override
            public void endContact(Contact contact) {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();

                GameObject objA = (GameObject) fixA.getUserData();
                GameObject objB = (GameObject) fixB.getUserData();

                if (objA != null && objB != null) {
                    handleEndContact(objA, objB);
                }
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();

                GameObject objA = (GameObject) fixA.getUserData();
                GameObject objB = (GameObject) fixB.getUserData();

                if (objA != null && objB != null) {
                    handlePreSolve(contact, objA, objB);
                }
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
    }

    private void handleBeginContact(GameObject objA, GameObject objB, Contact contact) {
        if ((objA.cBits == GameSettings.PLAYER_BIT && objB.cBits == GameSettings.PLATFORM_BIT) ||
            (objB.cBits == GameSettings.PLAYER_BIT && objA.cBits == GameSettings.PLATFORM_BIT)) {

            PlayerObject playerObj = null;
            GameObject platformObj = null;

            if (objA instanceof PlayerObject && objB.cBits == GameSettings.PLATFORM_BIT) {
                playerObj = (PlayerObject) objA;
                platformObj = objB;
            }
            else if (objB instanceof PlayerObject && objA.cBits == GameSettings.PLATFORM_BIT) {
                playerObj = (PlayerObject) objB;
                platformObj = objA;
            }

            if (playerObj != null && platformObj != null) {
                if (!(platformObj instanceof OneWayPlatformObject)) {
                    if (playerObj.getBody().getLinearVelocity().y > 0 && playerObj.getY() + playerObj.getHeight() <= platformObj.getY() + 10) {
                        playerObj.registerHeadHit();
                    }
                }
                else {
                    playerObj.hit();
                }
            }
        }
    }

    private void handleEndContact(GameObject objA, GameObject objB) {
    }

    private void handlePreSolve(Contact contact, GameObject objA, GameObject objB) {
        OneWayPlatformObject platform = null;
        PlayerObject player = null;

        if (objA instanceof OneWayPlatformObject && objB instanceof PlayerObject) {
            platform = (OneWayPlatformObject) objA;
            player = (PlayerObject) objB;
        }
        else if (objB instanceof OneWayPlatformObject && objA instanceof PlayerObject) {
            platform = (OneWayPlatformObject) objB;
            player = (PlayerObject) objA;
        }

        if (platform != null && player != null) {
            float playerBottom = player.getY();
            float playerTop = player.getY() + player.getHeight();
            float platformTop = platform.getY() + platform.getHeight();
            float platformBottom = platform.getY();
            float playerVelocityY = player.getBody().getLinearVelocity().y;

            if (player.wantsToGoDown() && playerBottom >= platformTop - 5) {
                contact.setEnabled(false);
                return;
            }
            if (playerTop <= platformBottom + 5 && playerVelocityY > 0) {
                contact.setEnabled(false);
                return;
            }
            if (playerVelocityY <= 0 && playerBottom >= platformTop - 5) {
                return;
            }
            contact.setEnabled(false);
        }
    }
}
