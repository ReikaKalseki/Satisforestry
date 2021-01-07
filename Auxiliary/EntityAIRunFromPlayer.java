package Reika.Satisforestry.Auxiliary;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.Vec3;

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
	/** The class of the entity we should avoid */
	private static final String __OBFID = "CL_00001574";

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
	public boolean shouldExecute()
	{
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
		ItemStack held = ep.getCurrentEquippedItem();
		if (held != null && held.getItem() == Satisforestry.paleberry)
			return false;
		double vel = 0;//ReikaMathLibrary.py3d(closestPlayer.lastTickPosX-closestPlayer.posX, 0, closestPlayer.lastTickPosZ-closestPlayer.posZ);
		return vel >= 0.2 || ep.getDistanceSqToEntity(e) <= 12;
	}

	@Override
	public boolean continueExecuting() {
		return !pathfinder.noPath() && isThreatening(doggo, closestPlayer);
	}

	@Override
	public void startExecuting() {
		pathfinder.setPath(entityPath, speedWhenFar);
	}

	@Override
	public void resetTask() {
		closestPlayer = null;
	}

	@Override
	public void updateTask() {
		if (doggo.getDistanceSqToEntity(closestPlayer) < 25) {
			doggo.getNavigator().setSpeed(speedWhenClose);
		}
		else {
			doggo.getNavigator().setSpeed(speedWhenFar);
		}
	}
}