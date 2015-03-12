package com.example.scene;

import java.io.IOException;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.SAXUtils;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.level.EntityLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
import org.andengine.util.level.simple.SimpleLevelLoader;
import org.xml.sax.Attributes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.example.base.BaseScene;
import com.example.manager.ResourcesManager;
import com.example.manager.SceneManager;
import com.example.manager.SceneManager.SceneType;
import com.example.object.Enemy;
import com.example.object.Player;
import com.example.object.Player.playerState;



public class GameScene extends BaseScene implements IOnSceneTouchListener{

private int score = 0;
	
	private HUD gameHUD;
	
	private PhysicsWorld physicsWorld;
	
	public static int width;
	public static int height;
	private static final String TAG_ENTITY = "entity";
	private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
	private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
	
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLOOR = "floor";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1 = "platform1";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2 = "platform2";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3 = "platform3";
	
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN = "coin";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_ENEMY = "enemy";
	
	int DownX,UpX;
	int DownY,UpY;

	private Player player;
	private Enemy enemy;
	
	private Text scoreText;
	private Text gameEndText;
	private boolean gameEndStateDisplayed = false;
	private static int levelID = 1;
	public enum gameState{
		WIN,
		LOSE
	}
	private playerState currentPlayerState = playerState.STOP;
	@Override
	public void createScene() {
		// TODO Auto-generated method stub
		createBackground();
		createHUD();
		createPhysics();
		loadLevel();
		
		setOnSceneTouchListener(this); 
	}

	@Override
	public void onBackKeyPressed() {
		// TODO Auto-generated method stub
		SceneManager.getInstance().loadMenuScene(engine);
	}

	@Override
	public SceneType getSceneType() {
		// TODO Auto-generated method stub
		return SceneType.SCENE_GAME;
	}

	@Override
	public void disposeScene() {
		// TODO Auto-generated method stub

		camera.setHUD(null);
		camera.setChaseEntity(null); //TODO
		camera.setCenter(400, 240);
		this.detachSelf();
		this.dispose();
	}

	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent)
	{
		if(player.getX() < width){
			if (pSceneTouchEvent.isActionDown()){
				DownY = (int)pSceneTouchEvent.getY();
				DownX = (int)pSceneTouchEvent.getX();
				System.out.println(DownX+","+DownY);
				if(DownY >= 240){
					if(DownX > 400)	
						currentPlayerState = playerState.JUMP_FORWARD;
					else
						currentPlayerState = playerState.JUMP_BACKWARD;
				}			
				else{
					if(DownX > 400)	
						currentPlayerState = playerState.FORWARD;
					else
						currentPlayerState = playerState.BACKWARD;
				}		
			}
			else if(pSceneTouchEvent.isActionUp()){
				currentPlayerState = playerState.STOP;
				}
			setMotion(currentPlayerState);
			System.out.println("GameScene.onSceneTouchEvent");
		}
		else{
			currentPlayerState = playerState.STOP;
			setMotion(currentPlayerState);
			player.onWin();
			if (pSceneTouchEvent.isActionDown()){
				levelID++;
				SceneManager.getInstance().loadMenuScene(engine);
			}
		}
		return false;
	}
	
	
	//Class Logic
	

	
	private void setMotion(playerState state){
		switch(state){
		case FORWARD:
			player.goForward();
			break;
		case BACKWARD:
			player.goBackward();
			break;
		case STOP:
			player.stop();
			break;
		case JUMP_FORWARD:
		case JUMP_BACKWARD:		
			if(Player.getFootOnFloor()){
				player.jump(state);					
			}
			break;
		}			
	}
	
	private void addToScore(int i)
	{
		score += i;
		scoreText.setText("Score: " + score);
	}
	
	private void createHUD()
	{
		gameHUD = new HUD();
		
		scoreText = new Text(20, 420, resourcesManager.font, "Score: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
		scoreText.setAnchorCenter(0, 0);	
		scoreText.setText("Score: 0");
		gameHUD.attachChild(scoreText);
		
		camera.setHUD(gameHUD);
	}
	
	private void createBackground()
	{
		setBackground(new Background(Color.BLUE));
	}
	
	private void createPhysics()
	{
		physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -17), false); 
		physicsWorld.setContactListener(contactListener());
		registerUpdateHandler(physicsWorld);
	}
	
	private ContactListener contactListener()
	{
		ContactListener contactListener = new ContactListener()
		{
			public void beginContact(Contact contact)
			{
				System.out.println("BeginContact");
				final Fixture x1 = contact.getFixtureA();
				final Fixture x2 = contact.getFixtureB();

				if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
				{

					if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
					{
						if (x2.getBody().getUserData().equals("player"))
						{
							player.setFootOnFloor(true);
						}
					}
					if (x1.getBody().getUserData().equals("platform2") && x2.getBody().getUserData().equals("player"))
					{
						engine.registerUpdateHandler(new TimerHandler(0.2f, new ITimerCallback()
						{									
						    public void onTimePassed(final TimerHandler pTimerHandler)
						    {
						    	pTimerHandler.reset();
						    	engine.unregisterUpdateHandler(pTimerHandler);
						    	x1.getBody().setType(BodyType.DynamicBody);
						    }
						}));
					}
					
					if (x1.getBody().getUserData().equals("platform3") && x2.getBody().getUserData().equals("player"))
					{
						x1.getBody().setType(BodyType.DynamicBody);
					}
					
				}
			}

			public void endContact(Contact contact)
			{
				final Fixture x1 = contact.getFixtureA();
				final Fixture x2 = contact.getFixtureB();

				if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
				{
					if (x2.getBody().getUserData().equals("player"))
					{
						player.setFootOnFloor(true);
					}
				}
			}

			public void preSolve(Contact contact, Manifold oldManifold)
			{

			}

			public void postSolve(Contact contact, ContactImpulse impulse)
			{

			}
		};
		return contactListener;
	}
	
	//Dealing with GameOver 
	private void createGameEndText(gameState state)
	{
		
		switch(state){
		case WIN:
			gameEndText = new Text(0, 0, resourcesManager.font, "You Win!\nClick to go to the next level", vbom);
			break;
		case LOSE:
			gameEndText = new Text(0, 0, resourcesManager.font, "Game Over!", vbom);
			break;
		}
		System.out.println("createGameEndText");
		
	}
	
	private void displayGameEndState(gameState state)
	{
		createGameEndText(state);
		camera.setChaseEntity(null);
		gameEndText.setPosition(camera.getCenterX(), camera.getCenterY());
		attachChild(gameEndText);
		gameEndStateDisplayed = true;
		System.out.println("displayGameEndState");
		
	}


	private void loadLevel()
	{
		System.out.println("load level "+levelID);
		final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);
		
		final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
		
		levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL)
		{
			public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException 
			{
				width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
				height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
				
				camera.setBounds(0, 0, width, height); // here we set camera bounds
				camera.setBoundsEnabled(true);

				return GameScene.this;
			}
		});
		
		levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(TAG_ENTITY)
		{
			public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException
			{
				final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
				final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
				final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
				
				final Sprite levelObject;
				
				if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLOOR))
				{
					levelObject = new Sprite(x, y, resourcesManager.floor_region, vbom);
					PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("floor");
				} 
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1))
				{
					levelObject = new Sprite(x, y, resourcesManager.platform1_region, vbom);
					final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("platform1");
					
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2))
				{
					levelObject = new Sprite(x, y, resourcesManager.platform2_region, vbom);
					final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("platform2");
					physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
				}
				
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3))
				{
					levelObject = new Sprite(x, y, resourcesManager.platform3_region, vbom);
					final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("platform3");
					physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
				}
			
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN))
				{
					levelObject = new Sprite(x, y, resourcesManager.coin_region, vbom)
					{
						@Override
						protected void onManagedUpdate(float pSecondsElapsed) 
						{
							super.onManagedUpdate(pSecondsElapsed);

							if (player.collidesWith(this))
							{
								addToScore(10);
								this.setVisible(false);
								this.setIgnoreUpdate(true);
							}
						}
					};
					levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
				}	
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER))
				{
					
					player = new Player(x, y, vbom, camera, physicsWorld)
					{					
						@Override
						public void onDie()
						{
							if (!gameEndStateDisplayed)
							{
								displayGameEndState(gameState.LOSE);
							}
						}

						@Override
						public void onWin() {
							// TODO Auto-generated method stub
							System.out.println("onWin");
							if (!gameEndStateDisplayed)
							{
								displayGameEndState(gameState.WIN);
							}
						}
					};
					levelObject = player;
				}	
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_ENEMY))
				{
					
					enemy = new Enemy(x, y, vbom, camera, physicsWorld)
					{
						@Override
						public void goRound() {
							// TODO Auto-generated method stub
							System.out.println("x = "+x);
							System.out.println("enemyx = "+ enemy.getX());
							if( enemy.getX() == x){
								enemy.goForward();
							}
							else if(enemy.getX() == x + 100){
								enemy.goBackward();
							}
						}					

					};
					levelObject = enemy;
				}	
				else
				{
					throw new IllegalArgumentException();
				}

				levelObject.setCullingEnabled(true);

				return levelObject;
			}
		});
		levelLoader.loadLevelFromAsset(activity.getAssets(), "level/" + levelID + ".lvl");
		System.out.println("GameScene.loadLevel "+ levelID);
	}

}
