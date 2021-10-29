package Reika.Satisforestry.Entity.AI;

import net.minecraft.util.Vec3;

import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;
import Reika.Satisforestry.Entity.EntitySpitter;

public class EntityAIRunToNewPosition extends EntityAISpitterReposition {

	public EntityAIRunToNewPosition(EntitySpitter e) {
		super(e, 0, e.getSpitterType().getPursuitDistance()*0.8);
	}

	@Override
	protected Vec3 getTargetPosition() {
		//return RandomPositionGenerator.findRandomTarget(entity, 4, 3);
		double dist = ReikaRandomHelper.getRandomBetween(3.5, 7.5, entity.getRNG());
		double[] xyz = ReikaPhysicsHelper.polarToCartesianFast(dist, 0, entity.getRNG().nextDouble()*360);
		return Vec3.createVectorHelper(attackTarget.posX+xyz[0], attackTarget.posY+xyz[1], attackTarget.posY+xyz[2]);
	}

	@Override
	protected boolean needsTargetPosition() {
		return true;
	}

	@Override
	protected boolean isRunning() {
		return true;
	}

	@Override
	protected double getStoppingDistance() {
		return 0.5;
	}
}