package Reika.Satisforestry.Entity.AI;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.Vec3;

import Reika.Satisforestry.Entity.EntityLizardDoggo;

public class EntityAISlowlyBackFromPlayer extends EntityAIBase {

	private EntityLizardDoggo doggo;
	private double speed;
	private EntityPlayer closestPlayer;
	private double maximumDistance;
	/** The PathEntity of our entity */
	private PathEntity entityPath;
	/** The PathNavigate of our entity */
	private PathNavigate pathfinder;

	public EntityAISlowlyBackFromPlayer(EntityLizardDoggo e, double dd, double sp) {
		doggo = e;
		maximumDistance = dd;
		speed = sp;
		pathfinder = e.getNavigator();
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		if (doggo.isTamed()) {
			return false;
		}

		closestPlayer = doggo.worldObj.getClosestPlayerToEntity(doggo, maximumDistance);

		if (closestPlayer == null)
			return false;

		if (EntityAIRunFromPlayer.hasPaleberry(closestPlayer))
			return false;

		double dx = doggo.posX*2-closestPlayer.posX;
		double dy = doggo.posY*2-closestPlayer.posY;
		double dz = doggo.posZ*2-closestPlayer.posZ;
		Vec3 vec3 = Vec3.createVectorHelper(dx, dy, dz);//RandomPositionGenerator.findRandomTargetBlockAwayFrom(doggo, 8, 2, Vec3.createVectorHelper(closestPlayer.posX, closestPlayer.posY, closestPlayer.posZ));

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
		return !pathfinder.noPath() && !EntityAIRunFromPlayer.hasPaleberry(closestPlayer);
	}

	@Override
	public void startExecuting() {
		pathfinder.setPath(entityPath, speed);
	}

	@Override
	public void resetTask() {
		closestPlayer = null;
		doggo.setBackwards(false);
	}

	@Override
	public void updateTask() {
		doggo.getNavigator().setSpeed(speed);
		doggo.setBackwards(true);
		doggo.rotationYawHead = -(float)Math.toDegrees(Math.atan2(doggo.posX-closestPlayer.posX, doggo.posZ-closestPlayer.posZ));
	}
}