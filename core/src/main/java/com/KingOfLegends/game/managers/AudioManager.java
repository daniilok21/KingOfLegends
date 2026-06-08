package com.KingOfLegends.game.managers;

import com.KingOfLegends.game.GameSettings;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.KingOfLegends.game.GameResources;
import java.util.Random;

public class AudioManager {

    private Music menuMusic;
    private Music menuWaiting;
    private Music gameMusic1;
    private Music gameMusic2;
    private Music currentGameMusic;
    private Sound victoryMusic;
    private Sound hitSound;
    private Sound clickSound;

    private Random random = new Random();

    public AudioManager() {
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal(GameResources.MENU_BACKGROUND_MUSIC_PATH));
        menuWaiting = Gdx.audio.newMusic(Gdx.files.internal(GameResources.MENU_BACKGROUND_WAITING_MUSIC_PATH));
        gameMusic1 = Gdx.audio.newMusic(Gdx.files.internal(GameResources.GAME_BACKGROUND_1_MUSIC_PATH));
        gameMusic2 = Gdx.audio.newMusic(Gdx.files.internal(GameResources.GAME_BACKGROUND_2_MUSIC_PATH));
        hitSound = Gdx.audio.newSound(Gdx.files.internal(GameResources.HIT_SOUND_PATH));
        clickSound = Gdx.audio.newSound(Gdx.files.internal(GameResources.CLICK_SOUND_PATH));
        victoryMusic = Gdx.audio.newSound(Gdx.files.internal(GameResources.GAME_VICRORY_MUSIC_PATH));

        menuMusic.setLooping(true);
        menuMusic.setVolume(0.15f);
        menuWaiting.setLooping(true);
        menuWaiting.setVolume(0.2f);
        gameMusic1.setLooping(true);
        gameMusic1.setVolume(0.2f);
        gameMusic2.setLooping(true);
        gameMusic2.setVolume(0.2f);
    }

    public void playMenuMusic() {
        stopGameMusic();
        stopWaitingMusic();
        if (!menuMusic.isPlaying()) {
            menuMusic.play();
        }
    }

    public void stopMenuMusic() {
        if (menuMusic.isPlaying()) {
            menuMusic.stop();
        }
    }
    public void playWaitingMusic() {
        stopGameMusic();
        stopMenuMusic();
        if (!menuWaiting.isPlaying()) {
            menuWaiting.play();
        }
    }

    public void stopWaitingMusic() {
        if (menuWaiting.isPlaying()) {
            menuWaiting.stop();
        }
    }
    public void playGameMusic(int musicIndex) {
        stopMenuMusic();
        stopWaitingMusic();
        if (currentGameMusic != null && currentGameMusic.isPlaying()) return;

        if (musicIndex == 0) {
            currentGameMusic = gameMusic1;
        } else {
            currentGameMusic = gameMusic2;
        }
        currentGameMusic.play();
    }

    public int getRandomMusicIndex() {
        return random.nextInt(2);
    }

    public void stopGameMusic() {
        if (currentGameMusic != null && currentGameMusic.isPlaying()) {
            currentGameMusic.stop();
        }
        currentGameMusic = null;
    }

    public void playHitSound() {
        hitSound.play(GameSettings.SOUND_VOLUME);
    }
    public void playClickSound() {
        clickSound.play(GameSettings.SOUND_VOLUME);
    }

    public void applyVolumes() {
        menuMusic.setVolume(GameSettings.MUSIC_VOLUME);
        menuWaiting.setVolume(GameSettings.MUSIC_VOLUME);
        gameMusic1.setVolume(GameSettings.MUSIC_VOLUME);
        gameMusic2.setVolume(GameSettings.MUSIC_VOLUME);
    }

    public void playVictorySound() {
        victoryMusic.play(GameSettings.SOUND_VOLUME);
    }

    public void stopMenuMusicCompletely() {
        stopMenuMusic();
        stopWaitingMusic();
    }

    public void dispose() {
        menuMusic.dispose();
        menuWaiting.dispose();
        gameMusic1.dispose();
        gameMusic2.dispose();
        hitSound.dispose();
        clickSound.dispose();
        victoryMusic.dispose();
    }
}
