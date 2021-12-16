package Reika.Satisforestry.Entity.AI;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import Reika.DragonAPI.Auxiliary.Trackers.KeyWatcher;
import Reika.DragonAPI.Auxiliary.Trackers.KeyWatcher.Key;
import Reika.DragonAPI.Auxiliary.Trackers.TickScheduler;
import Reika.DragonAPI.Instantiable.Event.ScheduledTickEvent;
import Reika.DragonAPI.Instantiable.Event.ScheduledTickEvent.ScheduledEvent;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.SFAux;
import Reika.Satisforestry.Entity.EntitySpitter;
import Reika.Satisforestry.Entity.EntitySpitterFireball;
import Reika.Satisforestry.Entity.EntitySplittingSpitterFireball;

import cpw.mods.fml.relauncher.Side;

public class EntityAISpitterFireball extends EntityAIDistanceDependent
{
	private final EntitySpitter entityHost;

	private final int baseCooldown;

	private final double fireballSpeed;
	private final float fireballDamage;

	private EntityLivingBase attackTarget;
	private int ticksOfSight;

	protected final Vec3 targetVelocity = Vec3.createVectorHelper(0, 0, 0);

	public EntityAISpitterFireball(EntitySpitter e, int maxTime, double mind, double maxd, double fs, float fd) {
		super(mind, maxd);
		entityHost = e;
		baseCooldown = maxTime;

		fireballSpeed = fs;
		fireballDamage = fd;

		this.setMutexBits(9);
	}

	@Override
	public final boolean shouldExecute() {
		int cool = this.getCooldown();
		if (entityHost.getAttackTime() < cool)
			return false;
		attackTarget = entityHost.getAttackTarget();
		if (attackTarget == null)
			return false;
		this.fetchDistance(entityHost, attackTarget);
		return this.isDistanceAppropriate();
	}

	public final int getCooldown() {
		int cool = baseCooldown;
		if (entityHost.riddenByEntity instanceof EntityPlayer) {
			int slug = SFAux.getSlugHelmetTier((EntityLivingBase)entityHost.riddenByEntity);
			if (slug > 0) {
				cool *= 1-0.25*slug;
			}
		}
		cool /= entityHost.getFireRateScale(this);
		return cool;
	}

	@Override
	public final boolean continueExecuting() {
		return this.shouldExecute();// || !entityHost.getNavigator().noPath();
	}

	@Override
	public final void resetTask() {
		super.resetTask();
		attackTarget = null;
		ticksOfSight = 0;
	}

	@Override
	public final void updateTask() {
		if (attackTarget == null)
			return;
		//ReikaJavaLibrary.pConsole(entityHost.getSpitterType()+" "+entityHost+" executing "+this+" "+fireballSpeed+"/"+fireballDamage);
		entityHost.getLookHelper().setLookPositionWithEntity(attackTarget, 10.0F, entityHost.getVerticalFaceSpeed());
		boolean flag = entityHost.getEntitySenses().canSee(attackTarget);

		if (flag) {
			++ticksOfSight;
		}
		else {
			ticksOfSight = 0;
		}
		/*
		if (currentDistSq <= maxDistSq && ticksOfSight >= 20) {
			entityHost.getNavigator().clearPathEntity();
		}
		else {
			entityHost.getNavigator().tryMoveToEntityLiving(attackTarget, entityMoveSpeed*2.5);
		}
		 */

		entityHost.getLookHelper().setLookPositionWithEntity(attackTarget, 30.0F, 30.0F);
		/*
		if (--rangedAttackTime == 0) {
			if (!flag || !this.isDistanceAppropriate()) {
				return;
			}

			this.fireFireball();
			rangedAttackTime = maxRangedAttackTime;
		}
		else if (rangedAttackTime < 0) {
			rangedAttackTime = maxRangedAttackTime;
		}*/
		//ReikaJavaLibrary.pConsole(entityHost.getSpitterType()+" "+new DecimalPosition(entityHost)+" ticking "+this);
	}

	@Override
	public void startExecuting() {
		if (this.isDistanceAppropriate()) {
			this.fireFireball();
			entityHost.updateAttackTime();
		}
	}

	public final void fireAt(EntityLivingBase e) {
		attackTarget = e;
		this.fireFireball();
	}

	protected final void fireFireball() {
		//ReikaJavaLibrary.pConsole("Firing "+this.getClass().getSimpleName()+" d="+Math.sqrt(currentDistSq)+" from "+entityHost.getSpitterType()+" @ "+new DecimalPosition(entityHost));
		World world = entityHost.worldObj;
		this.updateTargeting(attackTarget);
		this.doFireFireball(world, entityHost, attackTarget, fireballSpeed, fireballDamage);
	}

	protected final void updateTargeting(EntityLivingBase tgt) {
		Random rand = entityHost.getRNG();
		double dl = 4;
		double vx = 0;
		double vz = 0;
		if (tgt instanceof EntityPlayer) {
			EntityPlayer ep = (EntityPlayer)tgt;
			boolean fwd = KeyWatcher.instance.isKeyDown(ep, Key.FORWARD);
			boolean back = KeyWatcher.instance.isKeyDown(ep, Key.BACK);
			boolean left = KeyWatcher.instance.isKeyDown(ep, Key.LEFT);
			boolean right = KeyWatcher.instance.isKeyDown(ep, Key.RIGHT);
			double v = tgt.isSneaking() ? 0.3 : 1;
			double fwdSpeed = fwd == back ? 0 : (fwd ? v : -v);
			double sideSpeed = left == right ? 0 : (left ? v : -v); //+ve to left
			float f4 = MathHelper.sin(tgt.rotationYaw * (float)Math.PI / 180.0F);
			float f5 = MathHelper.cos(tgt.rotationYaw * (float)Math.PI / 180.0F);
			vx = sideSpeed * f5 - fwdSpeed * f4;
			vz = fwdSpeed * f5 + sideSpeed * f4;
		}
		double dx = tgt.posX+vx*dl - entityHost.posX;
		double dy = this.getYTarget(tgt, entityHost)+tgt.motionY*dl;
		double dz = tgt.posZ+vz*dl - entityHost.posZ;
		targetVelocity.xCoord = dx;
		targetVelocity.yCoord = dy;
		targetVelocity.zCoord = dz;
	}

	protected void doFireFireball(World world, EntitySpitter src, EntityLivingBase tgt, double sp, float dmg) {
		EntitySpitterFireball esf = new EntitySpitterFireball(world, src, targetVelocity.xCoord, targetVelocity.yCoord, targetVelocity.zCoord, sp, dmg);
		esf.posY = entityHost.posY + entityHost.height / 2.0F + 0.5D;
		world.spawnEntityInWorld(esf);
	}

	public static class EntityAISpitterClusterFireball extends EntityAISpitterFireball {

		public EntityAISpitterClusterFireball(EntitySpitter e, int maxTime, double mind, double maxd, double fs, float fd) {
			super(e,  maxTime, mind, maxd, fs, fd);
		}

		@Override
		protected void doFireFireball(World world, EntitySpitter src, EntityLivingBase e, double sp, float dmg) {
			for (int i = 0; i < 18; i++) {
				TickScheduler.instance.scheduleEvent(new ScheduledTickEvent(new ScheduledEvent() {
					@Override
					public void fire() {
						EntityLivingBase tgt = e;
						if ((tgt.isDead || tgt.getHealth() <= 0) && !(tgt instanceof EntityPlayer)) {
							tgt = src.findNearTarget();
							if (tgt == null)
								return;
						}
						EntityAISpitterClusterFireball.this.updateTargeting(tgt);
						double vx2 = ReikaRandomHelper.getRandomPlusMinus(targetVelocity.xCoord, 0.3);
						double vy2 = ReikaRandomHelper.getRandomPlusMinus(targetVelocity.yCoord, 0.15);
						double vz2 = ReikaRandomHelper.getRandomPlusMinus(targetVelocity.zCoord, 0.3);
						EntitySpitterFireball esf = new EntitySpitterFireball(world, src, vx2, vy2, vz2, sp, dmg);
						esf.posY = src.posY + src.height / 2.0F + 0.5D;
						double px = ReikaRandomHelper.getRandomPlusMinus(src.posX, 0.5);
						double pz = ReikaRandomHelper.getRandomPlusMinus(src.posZ, 0.5);
						if (src.riddenByEntity != null) {
							Vec3 vec = src.getLookVec();
							px += vec.xCoord*1.5;
							pz += vec.zCoord*1.5;
						}
						esf.setLocationAndAngles(px, esf.posY, pz, 0, 0);
						world.spawnEntityInWorld(esf);
					}

					@Override
					public boolean runOnSide(Side s) {
						return s == Side.SERVER;
					}

				}), 1+i*5);
				//queueEntitySpawn(esf, world, i*4);
			}
		}

	}

	public static class EntityAISpitterSplittingFireball extends EntityAISpitterFireball {

		public EntityAISpitterSplittingFireball(EntitySpitter e, int maxTime, double mind, double maxd, double fs, float fd) {
			super(e,  maxTime, mind, maxd, fs, fd);
		}

		@Override
		protected void doFireFireball(World world, EntitySpitter src, EntityLivingBase tgt, double sp, float dmg) {
			EntitySplittingSpitterFireball esf = new EntitySplittingSpitterFireball(world, src, tgt, targetVelocity.xCoord, targetVelocity.yCoord, targetVelocity.zCoord, sp, dmg);
			esf.posY = src.posY + src.height / 2.0F + 0.5D;
			world.spawnEntityInWorld(esf);
		}

	}

	public static double getYTarget(EntityLivingBase e, EntitySpitter src) {
		return e.boundingBox.minY + e.height / 2.0F - (src.posY + src.height / 2.0F);//+(true ? -1.5 : 0);
	}
}