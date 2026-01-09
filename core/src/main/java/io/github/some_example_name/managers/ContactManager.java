package io.github.some_example_name.managers;

import com.badlogic.gdx.physics.box2d.*;

import io.github.some_example_name.GameSettings;
import io.github.some_example_name.objects.GameObject;
import io.github.some_example_name.objects.OneWayPlatformObject;
import io.github.some_example_name.objects.PlayerObject;

public class ContactManager {

    World world;

    public ContactManager(World world) {
        this.world = world;

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();

                Object userDataA = fixA.getUserData();
                Object userDataB = fixB.getUserData();

                if (fixA.isSensor() || fixB.isSensor()) {
                    handleSensorContact(userDataA, userDataB, true);
                } else {
                    handleRegularContact(userDataA, userDataB);
                }
            }

            @Override
            public void endContact(Contact contact) {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();

                Object userDataA = fixA.getUserData();
                Object userDataB = fixB.getUserData();

                if (fixA.isSensor() || fixB.isSensor()) {
                    handleSensorContact(userDataA, userDataB, false);
                }
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();

                Object userDataA = fixA.getUserData();
                Object userDataB = fixB.getUserData();

                if (!fixA.isSensor() && !fixB.isSensor()) {
                    handlePreSolve(contact, userDataA, userDataB);
                }
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
    }

    private void handleSensorContact(Object userDataA, Object userDataB, boolean begin) {
        PlayerObject player = null;

        if (userDataA instanceof PlayerObject) {
            PlayerObject potentialPlayer = (PlayerObject) userDataA;
            if (userDataB instanceof GameObject) {
                GameObject other = (GameObject) userDataB;
                if (other.cBits == GameSettings.PLATFORM_BIT) {
                    player = potentialPlayer;
                }
            }
        }

        if (player == null && userDataB instanceof PlayerObject) {
            PlayerObject potentialPlayer = (PlayerObject) userDataB;
            if (userDataA instanceof GameObject) {
                GameObject other = (GameObject) userDataA;
                if (other.cBits == GameSettings.PLATFORM_BIT) {
                    player = potentialPlayer;
                }
            }
        }

        if (player != null) {
            if (begin) {
                player.beginGroundContact();
            } else {
                player.endGroundContact();
            }
        }
    }

    private void handleRegularContact(Object userDataA, Object userDataB) {
        if (userDataA instanceof GameObject && userDataB instanceof GameObject) {
            GameObject objA = (GameObject) userDataA;
            GameObject objB = (GameObject) userDataB;

            if ((objA.cBits == GameSettings.PLAYER_BIT && objB.cBits == GameSettings.PLATFORM_BIT) ||
                (objB.cBits == GameSettings.PLAYER_BIT && objA.cBits == GameSettings.PLATFORM_BIT)) {

                PlayerObject playerObj = null;
                if (objA instanceof PlayerObject) {
                    playerObj = (PlayerObject) objA;
                } else if (objB instanceof PlayerObject) {
                    playerObj = (PlayerObject) objB;
                }

                if (playerObj != null) {
                    playerObj.hit();
                }
            }
        }
    }

    private void handlePreSolve(Contact contact, Object userDataA, Object userDataB) {
        if (userDataA instanceof GameObject && userDataB instanceof GameObject) {
            GameObject objA = (GameObject) userDataA;
            GameObject objB = (GameObject) userDataB;

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
}
