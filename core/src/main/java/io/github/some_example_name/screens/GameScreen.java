package io.github.some_example_name.screens;

import com.badlogic.gdx.ScreenAdapter;
import io.github.some_example_name.MyGdxGame;

public class GameScreen extends ScreenAdapter {

    MyGdxGame myGdxGame;

    public GameScreen(MyGdxGame myGdxGame) {
        this.myGdxGame = myGdxGame;
    }

}
