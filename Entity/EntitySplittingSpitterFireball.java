package Reika.Satisforestry.Entity;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.Entity.AI.EntityAISpitterFireball;
import Reika.Satisforestry.Registry.SFSounds;
import Reika.Satisforestry.Render.SpitterFireParticle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class EntitySplittingSpitterFireball extends EntitySpitterFireball {

	private DecimalPosition spawnLocation;
	private EntityLivingBase target;

	public EntitySplittingSpitterFireball(World world, EntitySpitter e, EntityLivingBase tgt, double vx, double vy, double vz, double sp, float dmg) {
		super(world, e, vx, vy, vz, sp, dmg);
		spawnLocation = new DecimalPosition(this);
		target = tgt;
	}

	public EntitySplittingSpitterFireball(World world) {
		super(world);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EntityFX spawnLifeParticle(double x, double y, double z) {
		float s = (float)ReikaRandomHelper.getRandomBetween(1.25, 1.5);
		int l = ReikaRandomHelper.getRandomBetween(6, 12);
		SpitterFireParticle fx = new SpitterFireParticle(worldObj, x, y, z, this.getSpitterType());
		fx.setScale(s*3).setLife(l).setRapidExpand();
		return fx;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		//ReikaJavaLibrary.pConsole(target != null ? this.getDistanceSqToEntity(target) : -1, Side.SERVER);
		if (!worldObj.isRemote && (ticksExisted >= 40 || (target != null && this.getDistanceSqToEntity(target) <= 36))) {
			this.split();
		}
		if (ticksExisted >= 200)
			this.setDead();
	}

	private void split() {
		this.setDead();
		if (target == null) {
			return;
		}
		SFSounds.SPITTERBALLHIT.playSound(this);
		for (int i = 0; i < 12; i++) {
			double xc = target.posX;
			double zc = target.posZ;
			double dy = EntityAISpitterFireball.getYTarget(target, (EntitySpitter)shootingEntity);
			dy = target.posY+target.height/2-posY;
			xc = ReikaRandomHelper.getRandomPlusMinus(xc, 2.5);
			dy = ReikaRandomHelper.getRandomPlusMinus(dy, 0.5);
			zc = ReikaRandomHelper.getRandomPlusMinus(zc, 2.5);
			double dx = xc-posX;
			double dz = zc-posZ;
			EntitySpitterFireball esf = new EntitySpitterFireball(worldObj, (EntitySpitter)shootingEntity, dx, dy, dz, 1.2, this.getDamage());
			esf.setLocationAndAngles(posX, posY, posZ, 0, 0);
			//ReikaJavaLibrary.pConsole(shootingEntity+"&"+target+": "+esf);
			worldObj.spawnEntityInWorld(esf);
		}
	}

}
