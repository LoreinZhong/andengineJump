package com.example.object;


import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.vbo.VertexBufferObjectManager;





import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.example.manager.ResourcesManager;
import com.example.scene.GameScene;


/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public abstract class Player extends AnimatedSprite
{
	// ---------------------------------------------
	// VARIABLES
	// ---------------------------------------------
	
	private Body body;
	public static boolean footOnFloor = true;
	public enum playerState {
		FORWARD,
		STOP,
		BACKWARD,
		JUMP_FORWARD,
		JUMP_BACKWARD}
	public Engine engine;	
	// ---------------------------------------------
	// CONSTRUCTOR
	// ---------------------------------------------
	
	public Player(float pX, float pY, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld)
	{
		super(pX, pY, ResourcesManager.getInstance().player_region, vbo);
		createPhysics(camera, physicsWorld);
		camera.setChaseEntity(this);
	}
	
	// ---------------------------------------------
	// CLASS LOGIC
	// ---------------------------------------------
	
	private void createPhysics(final Camera camera, PhysicsWorld physicsWorld)
	{		
		body = PhysicsFactory.createBoxBody(physicsWorld, this, BodyType.DynamicBody, PhysicsFactory.createFixtureDef(0, 0, 0));

		body.setUserData("player");
		body.setFixedRotation(true);
		
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, false)
		{
			@Override
	        public void onUpdate(float pSecondsElapsed)
	        {
				super.onUpdate(pSecondsElapsed);
				camera.onUpdate(0.1f);
				
				if (getY() <= 0 )
				{	System.out.println(GameScene.width);
					System.out.println(getX());
					onDie();
				}
									
	        }
		});
	}
	
	
	public void goForward()
	{	
		final long[] PLAYER_ANIMATE = new long[] { 100, 100, 100 };
		body.setLinearVelocity(new Vector2(5, body.getLinearVelocity().y));
		animate(PLAYER_ANIMATE, 6, 8, true);
		System.out.println("player.goforward");
	}
	
	public void goBackward()
	{
		final long[] PLAYER_ANIMATE = new long[] { 100, 100, 100 };
		body.setLinearVelocity(new Vector2(-5, body.getLinearVelocity().y)); 	
		animate(PLAYER_ANIMATE, 3, 5, true);
	}
	
	public void stop()
	{
		body.setLinearVelocity(new Vector2(body.getLinearVelocity().x, body.getLinearVelocity().y));
		System.out.println("Player.stop");
		final long[] PLAYER_ANIMATE = new long[] { 100, 100, 100 };
		body.setLinearVelocity(new Vector2(0,0)); 
		animate(PLAYER_ANIMATE, 0, 2, true);
	}

	public void jump(playerState state)
	{
		int vx;
		if(state == playerState.JUMP_FORWARD)
		{
			vx = 3;
			final long[] PLAYER_ANIMATE = new long[] { 100, 100, 100 };
			animate(PLAYER_ANIMATE, 6, 8, true);
		}
		else{
			vx = -3;
			final long[] PLAYER_ANIMATE = new long[] { 100, 100, 100 };
			animate(PLAYER_ANIMATE, 3, 5, true);
		}
		body.setLinearVelocity(new Vector2(body.getLinearVelocity().x + vx, 7)); 
		Player.setFootOnFloor(false);
		System.out.println("Player.jump");
	}
	
	public static boolean getFootOnFloor(){
		return footOnFloor;
	}
	public static void setFootOnFloor(boolean isOnFloor){
		footOnFloor = isOnFloor;
	}
	
	public abstract void onDie();
	public abstract void onWin();
}