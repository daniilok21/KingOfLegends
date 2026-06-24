package com.KingOfLegends.game.screens;


import static com.KingOfLegends.game.GameSettings.SCREEN_HEIGHT;
import static com.KingOfLegends.game.GameSettings.SCREEN_WIDTH;

import com.KingOfLegends.game.GameResources;
import com.KingOfLegends.game.MyGdxGame;
import com.KingOfLegends.game.components.ButtonView;
import com.KingOfLegends.game.components.ImageView;
import com.KingOfLegends.game.components.MovingBackgroundView;
import com.KingOfLegends.game.components.TextView;
import com.KingOfLegends.game.managers.MemoryManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.security.Key;

public class UpgradeScreen extends ScreenAdapter {
    private final MyGdxGame game;
    private MovingBackgroundView background;
    private ImageView board, boardUnderSkills, boardForDescription, frame, exp, person, paper1, paper2, paper3;
    private TextView titleView, hp, lvl, exp_text, hp_text, lvl_text, titleDescribe;
    private TextView skillNameText;
    private TextureRegion[] critical_im, health_im, knockback_im, luck_im, moreExp_im, protect_im, lvl_im;
    private TextureRegion[][] skills_im;
    private Texture healthTexture, knockbackTexture, luckTexture, protectTexture, criticalTexture, moreExpTexture, lvlTexture, expTexture;
    private ButtonView homeButton;
    private int selectedSkillIndex = -1;
    private float[] healthData;
    private float[] knockbackData;
    private float[] luckData;
    private float[] protectData;
    private float[] criticalData;
    private float[] moreExpData;
    private float[][] lvlData = new float[6][4];
    private float[][] data;
    private int[] lvlSkills;
    private ImageView[] plusButtons = new ImageView[6];
    private int expValue;
    private static final String[] SKILL_NAMES = {
        "HEALTH", "KNOCKBACK", "LUCK", "PROTECT", "CRITICAL", "MORE EXP"
    };
    private static final String[] SKILL_DESCRIPTIONS = {
        "Increases max HP\nby 10 per level.\nHigher health means\nless knockback\nfrom enemy attacks",
        "Increases knock-\nbackforce by 5%\nper level.",
        "When you fall off\nthe map and would\nlose a life, there is\na chance to survive\ninstead.",
        "Chance to block\nan incoming hit\ncompletely.",
        "Chance to deal\ndouble damage\non attack.",
        "Increases earned\nEXP by 10%\nper level."
    };

    public UpgradeScreen(MyGdxGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        background = new MovingBackgroundView(GameResources.BACKGROUND_MENU);
        board = new ImageView(35, 35, SCREEN_WIDTH - 70, SCREEN_HEIGHT - 70, GameResources.BOARD_UPGRADE_PATH);
        titleView = new TextView(game.titleUpgradeFont, 170, SCREEN_HEIGHT - 137, "SKILLS");
        float t = 3.5f;
        frame = new ImageView(titleView.getCenterX() - 48 * t / 2f, 35 + board.height / 2f - 80, 48 * t, 72 * t, GameResources.FRAME_UPGRADE_PATH);
        exp = new ImageView(titleView.getCenterX() - 66 * t / 2f, frame.getY() - 15 * t - 5, 66 * t, 15 * t, GameResources.EXP_UPGRADE_PATH);
        exp_text = new TextView(game.defaultUpgradeFont, exp.getX() + exp.getWidth() / 2f + 5, 50, "exp");
        exp_text.setY(exp.getY() - exp_text.getHeight() / 5f);
        hp = new TextView(game.defaultUpgradeFont, 150, 50, "HP");
        hp.setY(exp.getY() - hp.getHeight() - 40f);
        lvl = new TextView(game.defaultUpgradeFont, 150, 50, "LVL");
        lvl.setY(hp.getY() - lvl.getHeight() - 10f);
        person = new ImageView(frame.getX() + 5 * t, frame.getY() - 5 * t, 43 * t, 64 * t, GameResources.PERSON_UPGRADE_SCREEN_PATH);
        t = 3.67f;
        boardForDescription = new ImageView(104 * t + board.getX(), 15 * t + board.getY(), 351.06f, 121 * t, GameResources.BOARD_UNDER_SKILLS_UPGRADE_PATH);
        boardUnderSkills = new ImageView(784.93997f, 15 * t + board.getY(), 351.06f, 121 * t, GameResources.BOARD_UNDER_SKILLS_UPGRADE_PATH);
        t = boardUnderSkills.getHeight() / 9.5f;
        paper1 = new ImageView(boardUnderSkills.getX() - boardUnderSkills.getWidth() / 49f, boardUnderSkills.getY() + t, boardUnderSkills.getWidth() + boardUnderSkills.getWidth() / 24.5f, boardUnderSkills.getHeight() / 5, GameResources.PAPER_UPGRADE_PATH);
        paper2 = new ImageView(boardUnderSkills.getX() - boardUnderSkills.getWidth() / 49f, boardUnderSkills.getY() + 3.75f * t, boardUnderSkills.getWidth() + boardUnderSkills.getWidth() / 24.5f, boardUnderSkills.getHeight() / 5, GameResources.PAPER_UPGRADE_PATH);
        paper3 = new ImageView(boardUnderSkills.getX() - boardUnderSkills.getWidth() / 49f, boardUnderSkills.getY() + 6.5f * t, boardUnderSkills.getWidth() + boardUnderSkills.getWidth() / 24.5f, boardUnderSkills.getHeight() / 5, GameResources.PAPER_UPGRADE_PATH);
        homeButton = new ButtonView(10, SCREEN_HEIGHT - 70, 60, 60, GameResources.BUTTON_HOME);

        healthTexture = new Texture(GameResources.HEALTH_UPGRADE_PATH);
        knockbackTexture = new Texture(GameResources.KNOCKBACK_UPGRADE_PATH);
        luckTexture = new Texture(GameResources.LUCK_UPGRADE_PATH);
        protectTexture = new Texture(GameResources.PROTECT_UPGRADE_PATH);
        criticalTexture = new Texture(GameResources.CRITICAL_UPGRADE_PATH);
        moreExpTexture = new Texture(GameResources.MORE_EXP_UPGRADE_PATH);
        lvlTexture = new Texture(GameResources.LVL_UPGRADES_UPGRADE_PATH);

        setUpSkillsImages();
        setUpDescribeUpgrades();
        lvlSkills = MemoryManager.loadAllSkills();
        expValue = MemoryManager.loadExp();

        hp_text = new TextView(game.defaultUpgradeFont, 320, hp.getY(), "");
        hp_text.setText(lvlSkills[0] * 10 + 100 + "");
        hp_text.setX(hp_text.getX() - hp_text.getWidth());
        lvl_text = new TextView(game.defaultUpgradeFont, 320, lvl.getY(), "");
        lvl_text.setText(MemoryManager.getLvl() + "");
        lvl_text.setX(hp_text.getCenterX() - lvl_text.getWidth() / 2f);
        expTexture = new Texture(GameResources.EXP_PROGRESS_UPGRADE_PATH);
        healthData = new float[] {paper3.getX() + 80, paper3.getY(), paper3.getHeight(), paper3.getHeight()};
        knockbackData = new float[] {paper3.getX() + paper3.getWidth() - paper3.getHeight() - 80, paper3.getY(), paper3.getHeight(), paper3.getHeight()};
        luckData = new float[] {paper2.getX() + 80, paper2.getY(), paper2.getHeight(), paper2.getHeight()};
        protectData = new float[] {paper2.getX() + paper2.getWidth() - paper2.getHeight() - 80, paper2.getY(), paper2.getHeight(), paper2.getHeight()};
        criticalData = new float[] {paper1.getX() + 80, paper1.getY(), paper1.getHeight(), paper1.getHeight()};
        moreExpData = new float[] {paper1.getX() + paper1.getWidth() - paper1.getHeight() - 80, paper1.getY(), paper1.getHeight(), paper1.getHeight()};

        data = new float[][] {healthData, knockbackData, luckData, protectData, criticalData, moreExpData};

        float sqrtFrom2 = (float) (Math.sqrt(2));
        for (int i = 0; i < data.length; i++) {
            float side = data[i][2] / sqrtFrom2;
            float pHeight = side / 1.3f;
            plusButtons[i] = new ImageView(data[i][0] + data[i][2] / 2f + 4, data[i][1] - pHeight / 2f + 8, pHeight, pHeight, GameResources.PLUS_UPGRADE_PATH);
            lvlData[i] = new float[] {data[i][0] + data[i][2] / 2f - pHeight - 4, data[i][1] - pHeight / 2f + 8, pHeight, pHeight};
        }

        while (expValue >= 1000) {
            expValue -= 1000;
            MemoryManager.add1Lvl();
            MemoryManager.add1UpgradePoint();
            lvl_text.setText(MemoryManager.getLvl() + "");
        }
        MemoryManager.saveExp(expValue);
    }

    private void setUpSkillsImages() {
        int frameWidth = healthTexture.getWidth() / 5;
        int frameHeight = healthTexture.getHeight();
        health_im = TextureRegion.split(healthTexture, frameWidth, frameHeight)[0];
        knockback_im = TextureRegion.split(knockbackTexture, frameWidth, frameHeight)[0];
        luck_im = TextureRegion.split(luckTexture, frameWidth, frameHeight)[0];
        protect_im = TextureRegion.split(protectTexture, frameWidth, frameHeight)[0];
        critical_im = TextureRegion.split(criticalTexture, frameWidth, frameHeight)[0];
        moreExp_im = TextureRegion.split(moreExpTexture, frameWidth, frameHeight)[0];
        skills_im = new TextureRegion[][] {health_im, knockback_im, luck_im, protect_im, critical_im, moreExp_im};

        frameWidth = lvlTexture.getWidth() / 6;
        frameHeight = lvlTexture.getHeight();
        lvl_im = TextureRegion.split(lvlTexture, frameWidth, frameHeight)[0];
    }

    private void setUpDescribeUpgrades() {
        titleDescribe = new TextView(game.titleUpgradeFont2, 0, 0, "DESCRIPTION");
        titleDescribe.setCenterX(boardForDescription.getX() + boardForDescription.getWidth() / 2f);
        titleDescribe.setY(boardForDescription.getY() + boardForDescription.getHeight() - titleDescribe.getHeight() - 60);
        skillNameText = new TextView(game.describeUpgradeFont, 0, 0, "");
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touch = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            if (homeButton.isHit(touch.x, touch.y)) {
                game.audioManager.playClickSound();
                game.setScreen(game.menuScreen);
                return;
            }
            boolean showPlusOrNot = MemoryManager.getUpgradePoint() > 0;
            for (int i = 0; i < data.length; i++) {
                if (checkHitRhomb(touch.x, touch.y, data[i])) {
                    System.out.println("Нажат ромб с индексом: " + i);
                    game.audioManager.playClickSound();
                    selectedSkillIndex = (selectedSkillIndex == i) ? -1 : i;
                    break;
                }

                if (lvlSkills[i] < 5 && showPlusOrNot &&
                    touch.x >= plusButtons[i].getX() && touch.x <= plusButtons[i].getX() + plusButtons[i].getWidth() &&
                    touch.y >= plusButtons[i].getY() && touch.y <= plusButtons[i].getY() + plusButtons[i].getHeight()) {

                    lvlSkills[i]++;
                    if (i == 0) {
                        hp_text.setText(lvlSkills[0] * 10 + 100 + "");
                    }
                    game.audioManager.playClickSound();
                    MemoryManager.saveUpgradePoint(MemoryManager.getUpgradePoint() - 1);
                    MemoryManager.saveSkillLevel(i, lvlSkills[i]);
                    selectedSkillIndex = (selectedSkillIndex == i) ? -1 : i;
                    System.out.println("Прокачан навык " + i + ". Текущий уровень: " + lvlSkills[i]);
                    break;
                }
            }
        }
    }
    private boolean checkHitRhomb(float tx, float ty, float[] data) {
        float centerX = data[0] + data[2] / 2f;
        float centerY = data[1] + data[3] / 2f;
        float halfWidth = data[2] / 2f;
        float halfHeight = data[3] / 2f;

        return (Math.abs(tx - centerX) / halfWidth + Math.abs(ty - centerY) / halfHeight) <= 1.0f;
    }

    @Override
    public void render(float delta) {
        handleInput();
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);
        ScreenUtils.clear(new Color(0, 0, 0, 1));

        game.batch.begin();


        float widthExpProgress = 324 - 144.3f;
        float value = Math.max(0f, Math.min(1f, expValue / 1000f));
        float filledWidth = value * widthExpProgress;

        background.draw(game.batch);
        board.draw(game.batch);
        boardUnderSkills.draw(game.batch);
        boardForDescription.draw(game.batch);
        person.draw(game.batch);
        frame.draw(game.batch);
        if (filledWidth > 0) {
            game.batch.draw(expTexture, 144.3f, 243.6f, filledWidth, 258 - 243.6f, 0, 0, value, 1f);
        }
        exp.draw(game.batch);
        titleView.draw(game.batch);
        game.defaultUpgradeFont.setColor(Color.GREEN);
        hp.draw(game.batch);
        game.defaultUpgradeFont.setColor(Color.WHITE);
        lvl.draw(game.batch);
        exp_text.draw(game.batch);
        paper1.draw(game.batch);
        paper2.draw(game.batch);
        paper3.draw(game.batch);
        hp_text.draw(game.batch);
        lvl_text.draw(game.batch);
        titleDescribe.draw(game.batch);
        if (selectedSkillIndex >= 0) {
            float centerX = boardForDescription.getX() + boardForDescription.getWidth() / 2f;
            float startY = titleDescribe.getY() - 50;

            skillNameText.setText(SKILL_NAMES[selectedSkillIndex]);
            skillNameText.setCenterX(centerX);
            skillNameText.setY(startY);
            game.describeUpgradeFont.setColor(Color.YELLOW);
            skillNameText.draw(game.batch);

            game.describeUpgradeFont.setColor(Color.WHITE);
            String[] lines = SKILL_DESCRIPTIONS[selectedSkillIndex].split("\n");
            float lineY = startY - skillNameText.getHeight();
            for (String line : lines) {
                GlyphLayout layout = new GlyphLayout(game.describeUpgradeFont, line);
                game.describeUpgradeFont.draw(game.batch, line, centerX - layout.width / 2f, lineY);
                lineY -= layout.height + 8;
            }
            game.describeUpgradeFont.setColor(Color.WHITE);
        }
        boolean showPlusOrNot = MemoryManager.getUpgradePoint() > 0;
        for (int i = 0; i < data.length; i++) {
            int currentSkillTextureIndex;

            if (lvlSkills[i] == 0) {
                currentSkillTextureIndex = 0;
                game.batch.setColor(0.3f, 0.3f, 0.3f, 0.6f);
            } else {
                currentSkillTextureIndex = lvlSkills[i] - 1;
                game.batch.setColor(Color.WHITE);
            }
            game.batch.draw(skills_im[i][currentSkillTextureIndex], data[i][0], data[i][1], data[i][2], data[i][3]);
            game.batch.setColor(Color.WHITE);

            int currentLvlTextureIndex = Math.max(0, Math.min(lvlSkills[i], 5));
            game.batch.draw(lvl_im[currentLvlTextureIndex], lvlData[i][0], lvlData[i][1], lvlData[i][2], lvlData[i][3]);

            if (lvlSkills[i] < 5 && showPlusOrNot) {
                plusButtons[i].draw(game.batch);
            }
        }
        homeButton.draw(game.batch);

        game.batch.end();
    }
    @Override
    public void dispose() {
        selectedSkillIndex = -1;
        if (background != null) background.dispose();
        if (healthTexture != null) healthTexture.dispose();
        if (knockbackTexture != null) knockbackTexture.dispose();
        if (luckTexture != null) luckTexture.dispose();
        if (protectTexture != null) protectTexture.dispose();
        if (criticalTexture != null) criticalTexture.dispose();
        if (moreExpTexture != null) moreExpTexture.dispose();
        if (lvlTexture != null) lvlTexture.dispose();
    }
}
