package com.packtpub.libgdx.bludbourne.desktop;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.packtpub.libgdx.bludbourne.BludBourneGame;
import sun.security.krb5.internal.crypto.Des;

public class DesktopLauncher {
	/** Debugging */
	private static final String TAG = DesktopLauncher.class.getSimpleName();

	public static void main (String[] arg) {
		// Application properties
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Bludbourne";
		config.useGL30 = false;
		config.width = 800;
		config.height = 600;
		config.vSyncEnabled = true;

		// Starting backend with the application listener instance & config
		new LwjglApplication(new BludBourneGame(), config);
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Gdx.app.debug(TAG, "Starting Application");
		Gdx.app.debug(TAG, "Window Dimensions (" + config.width + ", " + config.height + ")");
	}
}
