package com.packtpub.libgdx.bludbourne.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.packtpub.libgdx.bludbourne.Entity;
import com.packtpub.libgdx.bludbourne.MapManager;
import com.packtpub.libgdx.bludbourne.PlayerController;

public class MainGameScreen implements Screen {
	/** Debugging */
	private static final String TAG = MainGameScreen.class.getSimpleName();

	/** Viewport properties */
	private static class VIEWPORT {
		static float viewportWidth;
		static float viewportHeight;
		static float virtualWidth;
		static float virtualHeight;
		static float physicalWidth;
		static float physicalHeight;
		static float aspectRatio;
	}

	/** Input manager */
	private PlayerController controller;

	/** Player frame */
	private TextureRegion currentPlayerFrame;

	/** Handles position */
	private Sprite currentPlayerSprite;

	/** Map renderer */
	private OrthogonalTiledMapRenderer mapRenderer;

	/** Camera */
	public static OrthographicCamera camera;

	/** Map Manager */
	private static MapManager mapMgr;

	/** The player entity */
	private static Entity player;

	public MainGameScreen() {
		mapMgr = new MapManager();
	}

	@Override
	public void show() {
		// camera viewport
		setupViewport(10, 10);

		// camera setup
		camera = new OrthographicCamera();
		camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

		// map renderer setup
		mapRenderer = new OrthogonalTiledMapRenderer(mapMgr.getCurrentMap(), MapManager.UNIT_SCALE);
		mapRenderer.setView(camera);

		// placing player
		player = new Entity();
		player.init(mapMgr.getPlayerStartUnitScaled().x, mapMgr.getPlayerStartUnitScaled().y);

		currentPlayerSprite = player.getFrameSprite();
		controller = new PlayerController(player);
		Gdx.input.setInputProcessor(controller);
	}

	@Override
	public void render(float delta) {
		// Clear the previous frame
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// Preferable to lock and center the camera to the player position
		camera.position.set(currentPlayerSprite.getX(), currentPlayerSprite.getY(), 0f);
		camera.update();

		// Update animation
		player.update(delta);
		currentPlayerFrame = player.getFrame();

		// Check collisions with portals
		updatePortalLayerActivation(player.boundingBox);

		// Can we move
		if (!isCollisionWithMapLayer(player.boundingBox)) {
			player.setNextPositionToCurrent();
		}
		controller.update(delta);

		// Draw map
		mapRenderer.setView(camera);
		mapRenderer.render();

		// Draw player
		mapRenderer.getBatch().begin();
		mapRenderer.getBatch().draw(currentPlayerFrame, currentPlayerSprite.getX(), currentPlayerSprite.getY(), 1, 1);
		mapRenderer.getBatch().end();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}
	
	private void setupViewport(int width, int height) {
		// Make the viewport a percentage of the total display area
		VIEWPORT.virtualWidth = width;
		VIEWPORT.virtualHeight = height;
		
		// Current viewport dimensions
		VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
		VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;
		
		// Pixel dimensions
		VIEWPORT.physicalWidth = Gdx.graphics.getWidth();
		VIEWPORT.physicalHeight = Gdx.graphics.getHeight();
		
		// aspect ratio for current viewport
		VIEWPORT.aspectRatio = (VIEWPORT.virtualWidth / VIEWPORT.virtualHeight);
		
		// update viewport if there could be skewing
		if (VIEWPORT.physicalWidth / VIEWPORT.physicalHeight >= VIEWPORT.aspectRatio) {
			// Letterbox left and right
			VIEWPORT.viewportWidth = VIEWPORT.viewportHeight * (VIEWPORT.physicalWidth / VIEWPORT.physicalHeight);
			VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;
		} else {
			// Letterbox above and below
			VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
			VIEWPORT.viewportHeight = VIEWPORT.viewportWidth * (VIEWPORT.physicalHeight / VIEWPORT.physicalWidth);
		}
		
		Gdx.app.debug(TAG, "WorldRenderer: virtual: (" + VIEWPORT.virtualWidth + ", " + VIEWPORT.virtualHeight + ")");
		Gdx.app.debug(TAG, "WorldRenderer: viewport: (" + VIEWPORT.viewportWidth + ", " + VIEWPORT.viewportHeight + ")");
		Gdx.app.debug(TAG, "WorldRenderer: physical: (" + VIEWPORT.physicalWidth + ", " + VIEWPORT.physicalHeight + ")");
	}
	
	private boolean isCollisionWithMapLayer(Rectangle boundingBox) {
		MapLayer mapCollisionLayer = mapMgr.getCollisionLayer();
		
		if (mapCollisionLayer == null) {
			return false;
		}
		
		Rectangle rectangle = null;
		
		for (MapObject object : mapCollisionLayer.getObjects()) {
			if (object instanceof RectangleMapObject) {
				rectangle = ((RectangleMapObject) object).getRectangle();
				if (boundingBox.overlaps(rectangle)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean updatePortalLayerActivation(Rectangle boundingBox) {
		MapLayer mapPortalLayer = mapMgr.getPortalLayer();
		
		if (mapPortalLayer == null) {
			return false;
		}
		
		Rectangle rectangle = null;
		
		for (MapObject object : mapPortalLayer.getObjects()) {
			if (object instanceof RectangleMapObject) {
				rectangle = ((RectangleMapObject) object).getRectangle();
				if (boundingBox.overlaps(rectangle)) {
					String mapName = object.getName();
					if (mapName == null) {
						return false;
					}
					
					mapMgr.setClosestStartPositionFromScaledUnits(player.getCurrentPosition());
					mapMgr.loadMap(mapName);
					player.init(mapMgr.getPlayerStartUnitScaled().x, mapMgr.getPlayerStartUnitScaled().y);
					mapRenderer.setMap(mapMgr.getCurrentMap());
					Gdx.app.debug(TAG, "Portal Activated");
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void dispose() {
		player.dispose();
		controller.dispose();
		Gdx.input.setInputProcessor(null);
		mapRenderer.dispose();
	}
}