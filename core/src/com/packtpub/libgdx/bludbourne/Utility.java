package com.packtpub.libgdx.bludbourne;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import javax.rmi.CORBA.Util;

public class Utility {
	/** Tag for debugging */
	private static final String TAG = Utility.class.getSimpleName();

	/** AssetManger role is to load/unload assets such as textures/sounds/etc. */
	public static final AssetManager assetManager = new AssetManager();

	/** Maps a filepath to a FileHandle */
	private static InternalFileHandleResolver filePathResolver = new InternalFileHandleResolver();

	/**
	 * Unloads the specified asset
	 * @param assetFilenamePath
	 */
	public static void unloadAsset(String assetFilenamePath) {
		// once the asset manager is done loading
		if (assetManager.isLoaded(assetFilenamePath)) {
			Gdx.app.debug(TAG, "Unload: " + assetFilenamePath);
			assetManager.unload(assetFilenamePath);
		} else {
			Gdx.app.debug(TAG, "Asset is not loaded; Nothing to unload: " + assetFilenamePath);
		}
	}

	/**
	 * Loads the map
	 * @param mapFilenamePath
	 */
	public static void loadMapAsset(String mapFilenamePath) {
		// Argument check
		if (mapFilenamePath == null || mapFilenamePath.isEmpty()) {
			return;
		}
		
		// Does the map exist ?
		if (filePathResolver.resolve(mapFilenamePath).exists()) {
			// TiledMap Loader isn't loaded by default
			// TiledMap will be found in the working directory
			assetManager.setLoader(TiledMap.class, new TmxMapLoader(filePathResolver));

			// Load the map
			Gdx.app.debug(TAG, "Loading Map: " + mapFilenamePath);
			assetManager.load(mapFilenamePath, TiledMap.class);
			
			// Until we add loading screen
			// just block until we load the map
			assetManager.finishLoadingAsset(mapFilenamePath);

			Gdx.app.debug(TAG, "Map has been loaded: " + mapFilenamePath);
		} else {
			Gdx.app.debug(TAG, "Map doesn't exist !: " + mapFilenamePath);
		}
	}

	/** Returns the map associated with the given filepath */
	public static TiledMap getMapAsset(String mapFilenamePath) {
		TiledMap map = null;
		
		// the map must be loaded
		if (assetManager.isLoaded(mapFilenamePath)) {
			map = assetManager.get(mapFilenamePath, TiledMap.class);
		} else {
			Gdx.app.debug(TAG, "Map is not loaded: " + mapFilenamePath);
		}
		
		return map;
	}

	/** Loads a texture from the given path */
	public static void loadTextureAsset(String textureFilenamePath) {
		// Argument check
		if (textureFilenamePath == null || textureFilenamePath.isEmpty()) {
			return;
		}
		
		// Does the texture exist ?
		if (filePathResolver.resolve(textureFilenamePath).exists()) {
			// Load the texture
			Gdx.app.debug(TAG, "Loading Texture: " + textureFilenamePath);
			assetManager.load(textureFilenamePath, Texture.class);

			// Synchrone loading
			assetManager.finishLoadingAsset(textureFilenamePath);
		} else {
			Gdx.app.debug(TAG, "Texture doesn't exist !: " + textureFilenamePath);
		}
	}

	/** @return the texture associated with the given file path*/
	public static Texture getTextureAsset(String textureFilenamePath) {
		Texture texture = null;
		
		// the texture must be loaded
		if (assetManager.isLoaded(textureFilenamePath)) {
			texture = assetManager.get(textureFilenamePath, Texture.class);
		} else {
			Gdx.app.debug(TAG, "Texture is not loaded: " + textureFilenamePath);
		}



		return texture;
	}

	/** @see AssetManager#getProgress() */
	public static float loadCompleted() {
		return assetManager.getProgress();
	}

	/** @see AssetManager#getQueuedAssets() */
	public static int numberAssetsQueued() {
		return assetManager.getQueuedAssets();
	}

	/** @see AssetManager#update() */
	public static boolean updateAssetLoading() {
		return assetManager.update();
	}

	/** @see AssetManager#isLoaded(String) */
	public static boolean isAssetLoaded(String fileName) {
		return assetManager.isLoaded(fileName);
	}
}