package com.example.object;

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

public abstract class Enemy extends AnimatedSprite{
	
	private Body body;
	public Enemy(float pX, float pY, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld)
	{
		super(pX, pY, ResourcesManager.getInstance().enemy_region, vbo);
		createPhysics(camera, physicsWorld);
	}

	private void createPhysics(final Camera camera, PhysicsWorld physicsWorld)
	{		
		body = PhysicsFactory.createBoxBody(physicsWorld, this, BodyType.DynamicBody, PhysicsFactory.createFixtureDef(0, 0, 0));

		body.setUserData("enemy");
		body.setFixedRotation(true);
		
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, false)
		{
			@Override
	        public void onUpdate(float pSecondsElapsed)
	        {
				super.onUpdate(pSecondsElapsed);
				camera.onUpdate(0.1f);
				goRound();
	        }
		});
	}
	
	public abstract void goRound();
	public void goForward()
	{	
		final long[] ENEMY_ANIMATE = new long[] { 100, 100, 100 ,100,100 };
		body.setLinearVelocity(new Vector2(5, body.getLinearVelocity().y));
		animate(ENEMY_ANIMATE, 0, 4, true);
		System.out.println("player.goforward");
	}
	public void goBackward()
	{
		final long[] ENEMY_ANIMATE = new long[] { 100, 100, 100 ,100,100};
		body.setLinearVelocity(new Vector2(-5, body.getLinearVelocity().y)); 	
		animate(ENEMY_ANIMATE, 0, 4, true);
	}
}
