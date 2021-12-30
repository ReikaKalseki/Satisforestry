package Reika.Satisforestry.Entity.AI;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import Reika.Satisforestry.Entity.EntityLizardDoggo;


public class EntityAIDepositItem extends EntityAIBase {

	private static final double SPEED = 0.3;

	/** The entity we are attached to */
	private final EntityLizardDoggo doggo;
	private final EntityPlayer theOwner;
	private final int distanceToDetectChest;
	/** The PathEntity of our entity */
	private PathEntity entityPath;
	/** The PathNavigate of our entity */
	private final PathNavigate pathfinder;
	private TileEntityChest closestChest;
	private boolean wasSitting;

	public EntityAIDepositItem(EntityLizardDoggo e, int dd) {
		doggo = e;
		distanceToDetectChest = dd;
		pathfinder = e.getNavigator();

		this.setMutexBits(255);

		theOwner = (EntityPlayer)e.getOwner();
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {
		if (!doggo.isTamed() || (!doggo.hasItem() && !doggo.justDepositedItem(false))) {
			return false;
		}

		closestChest = this.findChest();

		if (closestChest == null)
			return false;

		if (doggo.getDistanceSq(closestChest.xCoord+0.5, closestChest.yCoord+0.5, closestChest.zCoord+0.5) > distanceToDetectChest*distanceToDetectChest)
			return false;

		Vec3 vec3 = Vec3.createVectorHelper(closestChest.xCoord+0.5, closestChest.yCoord+1, closestChest.zCoord+0.5);
		entityPath = pathfinder.getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
		return entityPath == null ? false : entityPath.isDestinationSame(vec3);
	}

	private TileEntityChest findChest() {
		int x = MathHelper.floor_double(doggo.posX);
		int y = MathHelper.floor_double(doggo.posY+doggo.height/4);
		int z = MathHelper.floor_double(doggo.posZ);
		int r = distanceToDetectChest;
		TileEntityChest ret = null;
		for (int i = -r; i <= r; i++) {
			for (int k = -r; k <= r; k++) {
				for (int j = -1; j <= 1; j++) {
					TileEntity te = doggo.worldObj.getTileEntity(x+i, y+j, z+k);
					if (te instanceof TileEntityChest) {
						if (ret == null || doggo.getDistanceSq(ret.xCoord+0.5, ret.yCoord+0.5, ret.zCoord+0.5) > doggo.getDistanceSq(te.xCoord+0.5, te.yCoord+0.5, te.zCoord+0.5))
							ret = (TileEntityChest)te;
					}
				}
			}
		}
		return ret;
	}

	@Override
	public boolean continueExecuting() {
		return !pathfinder.noPath() && closestChest != null && !closestChest.isInvalid() && (doggo.hasItem() || doggo.justDepositedItem(true)) && entityPath != null;
	}

	@Override
	public void startExecuting() {
		if (this.isTooFarToBother()) {
			pathfinder.clearPathEntity();
			entityPath = null;
		}
		else {
			pathfinder.setPath(entityPath, SPEED);
			wasSitting = doggo.isSitting();
		}
	}

	@Override
	public void resetTask() {
		closestChest = null;
		if (wasSitting)
			doggo.setSitting(true);
	}

	private boolean isTooFarToBother() {
		return theOwner == null || doggo.getDistanceSqToEntity(theOwner) >= 1024;
	}

	@Override
	public void updateTask() {
		Vec3 vec0 = Vec3.createVectorHelper(closestChest.xCoord+0.5, closestChest.yCoord+0.5, closestChest.zCoord+0.5);
		if (closestChest != null && doggo.hasItem() && (doggo.getDistanceSq(vec0.xCoord, vec0.yCoord, vec0.zCoord) <= 2.5 || this.isTooFarToBother())) {
			doggo.tryPutItemInChest(closestChest);
			if (wasSitting)
				doggo.setSitting(true);
		}
		else {
			doggo.setSitting(false);
			doggo.getNavigator().setSpeed(doggo.hasItem() ? SPEED : SPEED*1.11);
			if (!doggo.hasItem()) {
				if (wasSitting) {
					doggo.setSitting(true);
				}
				else {
					Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(doggo, 5, 1, vec0);

					if (vec3 != null) {
						entityPath = pathfinder.getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
						if (entityPath != null && !entityPath.isDestinationSame(vec3)) {
							entityPath = null;
						}
						if (entityPath != null)
							pathfinder.setPath(entityPath, SPEED*1.11);
					}
				}
			}
		}
	}
}
