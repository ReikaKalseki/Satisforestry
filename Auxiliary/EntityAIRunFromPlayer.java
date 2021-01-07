package Reika.Satisforestry.Auxiliary;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.Vec3;

import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Entity.EntityLizardDoggo;

public class EntityAIRunFromPlayer extends EntityAIBase {
	/** The entity we are attached to */
	private EntityLizardDoggo doggo;
	private double speedWhenFar;
	private double speedWhenClose;
	private EntityPlayer closestPlayer;
	private double distanceToDetectPlayer;
	/** The PathEntity of our entity */
	private PathEntity entityPath;
	/** The PathNavigate of our entity */
	private PathNavigate pathfinder;
	private int scaredTick;

	public EntityAIRunFromPlayer(EntityLizardDoggo e, double dd, double sfar, double sclose)
	{
		doggo = e;
		distanceToDetectPlayer = dd;
		speedWhenFar = sfar;
		speedWhenClose = sclose;
		pathfinder = e.getNavigator();
		this.setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {
		if (doggo.isTamed()) {
			return false;
		}

		closestPlayer = doggo.worldObj.getClosestPlayerToEntity(doggo, distanceToDetectPlayer);

		if (closestPlayer == null)
			return false;

		if (!isThreatening(doggo, closestPlayer))
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

	static boolean isThreatening(EntityLizardDoggo e, EntityPlayer ep) {
		if (ep.isSneaking())
			return false;
		if (ep.isJumping || !ep.onGround || ep.isSprinting())
			return true;
		if (hasPaleberry(ep))
			return false;
		double vel = ReikaMathLibrary.py3d(ep.lastTickPosX-ep.posX, 0, ep.lastTickPosZ-ep.posZ); //always zero speed no matter how it is measured!?
		return vel >= 0.2 || ep.getDistanceSqToEntity(e) <= 9;
	}

	static boolean hasPaleberry(EntityPlayer ep) {
		ItemStack held = ep.getCurrentEquippedItem();
		return held != null && held.getItem() == Satisforestry.paleberry;
	}

	@Override
	public boolean continueExecuting() {
		return !pathfinder.noPath() && (isThreatening(doggo, closestPlayer) || scaredTick > 0);
	}

	@Override
	public void startExecuting() {
		pathfinder.setPath(entityPath, speedWhenFar);
		scaredTick = 15;
	}

	@Override
	public void resetTask() {
		closestPlayer = null;
		scaredTick = 0;
	}

	@Override
	public void updateTask() {
		if (doggo.getDistanceSqToEntity(closestPlayer) < 25) {
			doggo.getNavigator().setSpeed(speedWhenClose);
		}
		else {
			doggo.getNavigator().setSpeed(speedWhenFar);
		}
		if (scaredTick > 0)
			scaredTick--;
	}
}