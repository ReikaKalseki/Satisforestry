package Reika.Satisforestry.Entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.Entity.AI.EntityAISpitterFireball;


public class EntitySplittingSpitterFireball extends EntitySpitterFireball {

	private DecimalPosition spawnLocation;
	private EntityLivingBase target;

	public EntitySplittingSpitterFireball(World world, EntitySpitter e, EntityLivingBase tgt, double vx, double vy, double vz, float dmg) {
		super(world, e, vx, vy, vz, dmg);
		spawnLocation = new DecimalPosition(this);
		target = tgt;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (ticksExisted >= 40 || (spawnLocation != null && this.getDistanceSq(spawnLocation.xCoord, spawnLocation.yCoord, spawnLocation.zCoord) >= 64)) {
			this.split();
		}
	}

	private void split() {
		for (int i = 0; i < 4; i++) {
			double dx = target.posX-shootingEntity.posX;
			double dy = EntityAISpitterFireball.getYTarget(target, (EntitySpitter)shootingEntity);
			double dz = target.posZ-shootingEntity.posZ;
			dx = ReikaRandomHelper.getRandomPlusMinus(dx, 0.5);
			dy = ReikaRandomHelper.getRandomPlusMinus(dx, 0.25);
			dz = ReikaRandomHelper.getRandomPlusMinus(dx, 0.5);
			EntitySpitterFireball esf = new EntitySpitterFireball(worldObj, (EntitySpitter)shootingEntity, dx, dy, dz, this.getDamage());
			worldObj.spawnEntityInWorld(esf);
		}
	}

}
