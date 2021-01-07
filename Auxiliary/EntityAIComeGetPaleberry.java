package Reika.Satisforestry.Auxiliary;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import Reika.Satisforestry.Entity.EntityLizardDoggo;

public class EntityAIComeGetPaleberry extends EntityAIBase {

	private EntityLizardDoggo doggo;
	private double speed;
	private EntityPlayer closestPlayer;
	private double maximumDistance;
	/** The PathEntity of our entity */
	private PathEntity entityPath;
	/** The PathNavigate of our entity */
	private PathNavigate pathfinder;

	private int runTick;

	private static final double MIN_DIST = 2.5;

	public EntityAIComeGetPaleberry(EntityLizardDoggo e, double dd, double sp) {
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

		if (closestPlayer.getDistanceSqToEntity(doggo) <= MIN_DIST*MIN_DIST)
			return false;

		if (!EntityAIRunFromPlayer.hasPaleberry(closestPlayer))
			return false;

		return true;
	}

	@Override
	public boolean continueExecuting() {
		return !pathfinder.noPath() && EntityAIRunFromPlayer.hasPaleberry(closestPlayer) && closestPlayer.getDistanceSqToEntity(doggo) > MIN_DIST*MIN_DIST;
	}

	@Override
	public void startExecuting() {
		runTick = 0;
		doggo.getNavigator().setAvoidsWater(false);
	}

	@Override
	public void resetTask() {
		closestPlayer = null;
		pathfinder.clearPathEntity();
		doggo.getNavigator().setAvoidsWater(false);
		doggo.setLured(false);
	}

	@Override
	public void updateTask() {
		doggo.getLookHelper().setLookPositionWithEntity(closestPlayer, 10.0F, doggo.getVerticalFaceSpeed());
		doggo.setLured(true);

		if (--runTick <= 0) {
			runTick = 10;

			if (!pathfinder.tryMoveToEntityLiving(closestPlayer, speed)) {
				int i = MathHelper.floor_double(closestPlayer.posX) - 2;
				int j = MathHelper.floor_double(closestPlayer.posZ) - 2;
				int k = MathHelper.floor_double(closestPlayer.boundingBox.minY);

				World world = doggo.worldObj;

				for (int l = 0; l <= 4; ++l) {
					for (int i1 = 0; i1 <= 4; ++i1) {
						if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && World.doesBlockHaveSolidTopSurface(world, i + l, k - 1, j + i1) && !world.getBlock(i + l, k, j + i1).isNormalCube() && !world.getBlock(i + l, k + 1, j + i1).isNormalCube()) {
							doggo.setLocationAndAngles(i + l + 0.5F, k, j + i1 + 0.5F, doggo.rotationYaw, doggo.rotationPitch);
							pathfinder.clearPathEntity();
							return;
						}
					}
				}
			}
		}
	}
}