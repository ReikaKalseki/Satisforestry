package Reika.Satisforestry.Entity;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.Entity.AI.EntityAISpitterFireball;
import Reika.Satisforestry.Render.SpitterFireParticle;


public class EntitySplittingSpitterFireball extends EntitySpitterFireball {

	private DecimalPosition spawnLocation;
	private EntityLivingBase target;

	public EntitySplittingSpitterFireball(World world, EntitySpitter e, EntityLivingBase tgt, double vx, double vy, double vz, float dmg) {
		super(world, e, vx, vy, vz, dmg);
		spawnLocation = new DecimalPosition(this);
		target = tgt;
	}

	public EntitySplittingSpitterFireball(World world) {
		super(world);
	}

	@Override
	public EntityFX spawnLifeParticle(double x, double y, double z) {
		float s = (float)ReikaRandomHelper.getRandomBetween(1.25, 1.5);
		int l = ReikaRandomHelper.getRandomBetween(6, 12);
		SpitterFireParticle fx = new SpitterFireParticle(worldObj, x, y, z, this.getSpitterType());
		fx.setScale(s).setLife(l).setRapidExpand();
		return fx;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (ticksExisted >= 40 || (spawnLocation != null && this.getDistanceSq(spawnLocation.xCoord, spawnLocation.yCoord, spawnLocation.zCoord) >= 144)) {
			this.split();
		}
	}

	private void split() {
		if (target == null) {
			this.setDead();
			return;
		}
		for (int i = 0; i < 4; i++) {
			double dx = target.posX-shootingEntity.posX;
			double dy = EntityAISpitterFireball.getYTarget(target, (EntitySpitter)shootingEntity);
			double dz = target.posZ-shootingEntity.posZ;
			dx = ReikaRandomHelper.getRandomPlusMinus(dx, 0.5);
			dy = ReikaRandomHelper.getRandomPlusMinus(dy, 0.25);
			dz = ReikaRandomHelper.getRandomPlusMinus(dz, 0.5);
			EntitySpitterFireball esf = new EntitySpitterFireball(worldObj, (EntitySpitter)shootingEntity, dx, dy, dz, this.getDamage());
			worldObj.spawnEntityInWorld(esf);
		}
	}

}
