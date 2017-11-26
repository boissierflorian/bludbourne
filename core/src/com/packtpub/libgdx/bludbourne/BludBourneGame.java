package com.packtpub.libgdx.bludbourne;

import com.badlogic.gdx.Game;
import com.packtpub.libgdx.bludbourne.screens.MainGameScreen;

public class BludBourneGame extends Game {
	/** The main game screen instance */
	public static final MainGameScreen mainGameScreen =
			new MainGameScreen();
	
	@Override
	public void create () {
		setScreen(mainGameScreen);
	}

	
	@Override
	public void dispose () {
		mainGameScreen.dispose();
	}
}
