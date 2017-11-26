package com.packtpub.libgdx.bludbourne;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;

import java.util.Hashtable;

public class MapManager {
	/** Debugging */
	private static final String TAG = MapManager.class.getSimpleName();
	
	/** Maps names associated with filepaths  **/
	private Hashtable<String, String> mapTable;

	/** Maps names associated with start positions */
	private Hashtable<String, Vector2> playerStartLocationTable;
	
	// Maps
	private final static String TOP_WORLD = "TOP_WORLD";
	private final static String TOWN = "TOWN";
	private final static String CASTLE_OF_DOOM = "CASTLE_OF_DOOM";
	// Map layers
	private final static String MAP_COLLISION_LAYER = "MAP_COLLISION_LAYER";
	private static final String MAP_SPAWNS_LAYER = "MAP_SPAWNS_LAYER";
	private static final String MAP_PORTAL_LAYER = "MAP_PORTAL_LAYER";
	private final static String PLAYER_START = "PLAYER_START";

	// Starting position (?)
	private Vector2 playerStartPositionRect;

	// Current closest start position
	private Vector2 closestPlayerStartPosition;
	private Vector2 convertedUnits;
	
	/** Player Start in pixels */
	private Vector2 playerStart;

	// Current map instance
	private TiledMap currentMap = null;

	// Current map name
	private String currentMapName;

	// Fast layer access
	private MapLayer collisionLayer = null;
	private MapLayer portalLayer = null;
	private MapLayer spawnsLayer = null;


	// 16 pixels = 1 Unit
	public static final float UNIT_SCALE = 1 / 16f;
	
	public MapManager() {
		// Associating maps with their filepaths
		mapTable = new Hashtable<String, String>();
		mapTable.put(TOP_WORLD, "maps/topworld.tmx");
		mapTable.put(TOWN, "maps/town.tmx");
		mapTable.put(CASTLE_OF_DOOM, "maps/castle_of_doom.tmx");

		// Init start positions
		playerStart = new Vector2(0, 0);
		playerStartLocationTable = new Hashtable<String, Vector2>();
		playerStartLocationTable.put(TOP_WORLD, playerStart.cpy());
		playerStartLocationTable.put(TOWN, playerStart.cpy());
		playerStartLocationTable.put(CASTLE_OF_DOOM, playerStart.cpy());

		// Init
		playerStartPositionRect = new Vector2(0, 0);
		closestPlayerStartPosition = new Vector2(0, 0);
		convertedUnits = new Vector2(0, 0);
	}

	/** Loads the specified map */
	public void loadMap(String mapName) {
		// Reset the start location
		playerStart.set(0, 0);

		// Get the map path
		String mapFullPath = mapTable.get(mapName);

		// Map path check
		if (mapFullPath == null || mapFullPath.isEmpty()) {
			Gdx.app.debug(TAG, "Map is invalid");
			return;
		}

		// Dispose the current map if there is any
		if (currentMap != null) {
			currentMap.dispose();
		}

		// Load the map
		Utility.loadMapAsset(mapFullPath);

		// Asset checking
		if (Utility.isAssetLoaded(mapFullPath)) {
			// Update references
			currentMap = Utility.getMapAsset(mapFullPath);
			currentMapName = mapName;
		} else {
			Gdx.app.debug(TAG, "Map not loaded");
			return;
		}

		// Update references for fast access
		collisionLayer = currentMap.getLayers().get(MAP_COLLISION_LAYER);
		if (collisionLayer == null) {
			Gdx.app.debug(TAG, "No collision layer !");
		}
		
		portalLayer = currentMap.getLayers().get(MAP_PORTAL_LAYER);
		if (portalLayer == null) {
			Gdx.app.debug(TAG, "No portal layer !");
		}
		
		spawnsLayer = currentMap.getLayers().get(MAP_SPAWNS_LAYER);
		if (spawnsLayer == null) {
			Gdx.app.debug(TAG, "No spawn layer !");
		} else {
			Vector2 start = playerStartLocationTable.get(currentMapName);
			
			if (start.isZero()) {
				setClosestStartPosition(playerStart);
				start = playerStartLocationTable.get(currentMapName);
			}
			playerStart.set(start.x, start.y);
		}
	}

	/** @return the current map */
	public TiledMap getCurrentMap() {
		// First loading
		if (currentMap == null) {
			currentMapName = TOWN;
			loadMap(currentMapName);
		}
		return currentMap;
	}

	/** @return the collision layer */
	public MapLayer getCollisionLayer() { return collisionLayer; }

	/** @return the portal layer */
	public MapLayer getPortalLayer() { return portalLayer; }
	
	/** @return the player start coordinates in map units */
	public Vector2 getPlayerStartUnitScaled() {
		Vector2 playerStart = this.playerStart.cpy();
		playerStart.set(playerStart.x * UNIT_SCALE, playerStart.y * UNIT_SCALE);
		return playerStart;
	}
	
	private void setClosestStartPosition(final Vector2 position) {
		// Get last known position on this map
		playerStartPositionRect.set(0, 0);
		closestPlayerStartPosition.set(0, 0);
		float shortestDistance = 0f;
		
		// Go through all player start positions and choose closets to
		// last known position
		for (MapObject object : spawnsLayer.getObjects()) {
			if (object.getName().equalsIgnoreCase(PLAYER_START)) {
				((RectangleMapObject) object).getRectangle().getPosition(playerStartPositionRect);
				float distance = position.dst2(playerStartPositionRect);
				
				if (distance < shortestDistance || shortestDistance == 0) {
					closestPlayerStartPosition.set(playerStartPositionRect);
					shortestDistance = distance;
				}
			}
		}
		
		playerStartLocationTable.put(currentMapName, closestPlayerStartPosition.cpy());
	}
	
	public void setClosestStartPositionFromScaledUnits(Vector2 position) {
		if (UNIT_SCALE <= 0) return;
		
		convertedUnits.set(position.x / UNIT_SCALE, position.y / UNIT_SCALE);
		setClosestStartPosition(convertedUnits);
	}
	
}