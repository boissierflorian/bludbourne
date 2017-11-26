package com.packtpub.libgdx.bludbourne;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Entity {
	// Debugging
	private static final String TAG = Entity.class.getSimpleName();

	// Sprite path
	private static final String defaultSpritePath = "sprites/characters/Warrior.png";

	// Velocity
	private Vector2 velocity;

	// ID
	private String entityID;

	// Directions
	private Direction currentDirection = Direction.LEFT;
	private Direction previousDirection = Direction.UP;

	// Animations
	private Animation<TextureRegion> walkLeftAnimation;
	private Animation<TextureRegion> walkRightAnimation;
	private Animation<TextureRegion> walkUpAnimation;
	private Animation<TextureRegion> walkDownAnimation;

	// Images
	private Array<TextureRegion> walkLeftFrames;
	private Array<TextureRegion> walkRightFrames;
	private Array<TextureRegion> walkUpFrames;
	private Array<TextureRegion> walkDownFrames;

	/** Next player position in map coordinates */
	protected Vector2 nextPlayerPosition;
	/** Current player position in map coordinates */
	protected Vector2 currentPlayerPosition;
	// Current state
	protected State state = State.IDLE;
	// Animation timer
	protected float frameTime = 0f;
	// Entity position
	protected Sprite frameSprite = null;
	// Current frame
	protected TextureRegion currentFrame = null;

	// Frame dimensions
	public final int FRAME_WIDTH = 16;
	public final int FRAME_HEIGHT = 16;

	/** Bounding box dimensions in real coordinates */
	public static Rectangle boundingBox;

	public enum State {
		IDLE, WALKING;
	}

	public enum Direction {
		UP, RIGHT, DOWN, LEFT
	}

	public Entity() {
		initEntity();
	}

	public void initEntity() {
		this.entityID = UUID.randomUUID().toString();
		this.nextPlayerPosition = new Vector2();
		this.currentPlayerPosition = new Vector2();
		this.boundingBox = new Rectangle();
		this.velocity = new Vector2(5f, 5f);

		Utility.loadTextureAsset(defaultSpritePath);
		loadDefaultSprite();
		loadAllAnimations();
	}

	public void update(float delta) {
		frameTime = (frameTime + delta) % 5; // Avoid overflow
		// We want the hitbox to be at the feet for a better feel
		setBoundingBoxSize(0f, 0.5f);
	}

	public void init(float startX, float startY) {
		Gdx.app.debug(TAG, "Player init (" + startX + ", " + startY + ")");
		this.currentPlayerPosition.x = startX;
		this.currentPlayerPosition.y = startY;

		this.nextPlayerPosition.x = startX;
		this.nextPlayerPosition.y = startY;
	}

	public void setBoundingBoxSize(float percentageWidthReduced, float percentageHeightReduced) {
		// Update the current bounding box
		float width;
		float height;

		float widthReductionAmount = 1.0f - percentageWidthReduced;
		float heightReductionAmount = 1.0f - percentageHeightReduced;

		if (widthReductionAmount > 0 && widthReductionAmount < 1) {
			width = FRAME_WIDTH * widthReductionAmount;
		} else {
			width = FRAME_WIDTH;
		}

		if (heightReductionAmount > 0 && heightReductionAmount < 1) {
			height = FRAME_HEIGHT * heightReductionAmount;
		} else {
			height = FRAME_HEIGHT;
		}

		if (width == 0 || height == 0) {
			Gdx.app.debug(TAG, "Width and Height are 0!! " + width + ":" + height);
		}

		// Need to account for the unitscale, since the map coordinates will be in
		// pixels
		float minX;
		float minY;

		if (MapManager.UNIT_SCALE > 0) {
			minX = nextPlayerPosition.x / MapManager.UNIT_SCALE;
			minY = nextPlayerPosition.y / MapManager.UNIT_SCALE;
		} else {
			minX = nextPlayerPosition.x;
			minY = nextPlayerPosition.y;
		}

		boundingBox.set(minX, minY, width, height);
	}

	private void loadDefaultSprite() {
		Texture texture = Utility.getTextureAsset(defaultSpritePath);
		TextureRegion[][] texturesFrames = TextureRegion.split(texture, FRAME_WIDTH, FRAME_HEIGHT);
		frameSprite = new Sprite(texturesFrames[0][0].getTexture(), 0, 0, FRAME_WIDTH, FRAME_HEIGHT);
		currentFrame = texturesFrames[0][0];
	}

	private void loadAllAnimations() {
		// Walking animation
		Texture texture = Utility.getTextureAsset(defaultSpritePath);
		TextureRegion[][] texturesFrames = TextureRegion.split(texture, FRAME_WIDTH, FRAME_HEIGHT);
		walkDownFrames = new Array<TextureRegion>(4);
		walkLeftFrames = new Array<TextureRegion>(4);
		walkRightFrames = new Array<TextureRegion>(4);
		walkUpFrames = new Array<TextureRegion>(4);

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				TextureRegion region = texturesFrames[i][j];
				if (region == null) {
					Gdx.app.debug(TAG, "Got null animation frame " + i + "," + j);
				}

				switch (i) {
					case 0:
						walkDownFrames.insert(j, region);
						break;
					case 1:
						walkLeftFrames.insert(j, region);
						break;
					case 2:
						walkRightFrames.insert(j, region);
						break;
					case 3:
						walkUpFrames.insert(j, region);
						break;
				}
			}
		}
		
		walkDownAnimation = new Animation<TextureRegion>(0.25f, walkDownFrames, Animation.PlayMode.LOOP);
		walkLeftAnimation = new Animation<TextureRegion>(0.25f, walkLeftFrames, Animation.PlayMode.LOOP);
		walkRightAnimation = new Animation<TextureRegion>(0.25f, walkRightFrames, Animation.PlayMode.LOOP);
		walkUpAnimation = new Animation<TextureRegion>(0.25f, walkUpFrames, Animation.PlayMode.LOOP);
	}
	
	public void dispose() {
		Utility.unloadAsset(defaultSpritePath);
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	public Sprite getFrameSprite() {
		return frameSprite;
	}
	
	public TextureRegion getFrame() {
		return currentFrame;
	}
	
	/** @return the current player position in map units */
	public Vector2 getCurrentPosition() {
		return currentPlayerPosition;
	}
	
	public void setCurrentPosition(float currentPositionX, float currentPositionY) {
		frameSprite.setX(currentPositionX);
		frameSprite.setY(currentPositionY);
		
		this.currentPlayerPosition.x = currentPositionX;
		this.currentPlayerPosition.y = currentPositionY;
	}
	
	public void setDirection(Direction direction, float deltaTime) {
		this.previousDirection = this.currentDirection;
		this.currentDirection = direction;
		
		// look into the appropriate variable when changing position
		switch (currentDirection) {
			case DOWN:
				currentFrame = walkDownAnimation.getKeyFrame(frameTime);
				break;
			case LEFT:
				currentFrame = walkLeftAnimation.getKeyFrame(frameTime);
				break;
			case UP:
				currentFrame = walkUpAnimation.getKeyFrame(frameTime);
				break;
			case RIGHT:
				currentFrame = walkRightAnimation.getKeyFrame(frameTime);
				break;
			default:
				break;
		}
	}
	
	public void setNextPositionToCurrent() {
		setCurrentPosition(nextPlayerPosition.x, nextPlayerPosition.y);
	}
	
	public void calculateNextPosition(Direction currentDirection, float deltaTime) {
		float testX = currentPlayerPosition.x;
		float testY = currentPlayerPosition.y;
		
		velocity.scl(deltaTime);
		
		switch (currentDirection) {
			case LEFT:
				testX -= velocity.x;
				break;
			case RIGHT:
				testX += velocity.x;
				break;
			case UP:
				testY += velocity.y;
				break;
			case DOWN:
				testY -= velocity.y;
				break;
			default:
				break;
		}
		
		nextPlayerPosition.x = testX;
		nextPlayerPosition.y = testY;
		
		velocity.scl(1 / deltaTime);
	}
	
	public Direction getCurrentDirection() { return currentDirection; }
}
