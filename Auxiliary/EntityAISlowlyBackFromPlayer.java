package Reika.Satisforestry.Auxiliary;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.Vec3;

import Reika.Satisforestry.Entity.EntityLizardDoggo;

public class EntityAISlowlyBackFromPlayer extends EntityAIBase {
	/** The entity we are attached to */
	private EntityLizardDoggo doggo;
	private double speed;
	private EntityPlayer closestPlayer;
	private double maximumDistance;
	/** The PathEntity of our entity */
	private PathEntity entityPath;
	/** The PathNavigate of our entity */
	private PathNavigate pathfinder;
	/** The class of the entity we should avoid */
	private static final String __OBFID = "CL_00001574";

	public EntityAISlowlyBackFromPlayer(EntityLizardDoggo e, double dd, double sp)
	{
		doggo = e;
		maximumDistance = dd;
		speed = sp;
		pathfinder = e.getNavigator();
		this.setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute()
	{
		if (doggo.isTamed()) {
			return false;
		}

		closestPlayer = doggo.worldObj.getClosestPlayerToEntity(doggo, maximumDistance);

		if (closestPlayer == null)
			return false;

		if (!EntityAIRunFromPlayer.isThreatening(doggo, closestPlayer))
			return false;

		Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(doggo, 20, 4, Vec3.createVectorHelper(closestPlayer.posX, closestPlayer.posY, closestPlayer.posZ));

		if (vec3 == null) {
			return false;
		}
		else if (closestPlayer.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord) < closestPlayer.getDistanceSqToEntity(doggo)) {
			return false;
		}
		else {
			entityPath = pathfinder.getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
			return entityPath == null ? false : entityPath.isDestinationSame(vec3);
		}
	}

	@Override
	public boolean continueExecuting() {
		return !pathfinder.noPath() && this.isThreatened();
	}

	@Override
	public void startExecuting() {
		pathfinder.setPath(entityPath, speed);
	}

	@Override
	public void resetTask() {
		closestPlayer = null;
	}

	@Override
	public void updateTask() {
		doggo.getNavigator().setSpeed(speed);
	}
}