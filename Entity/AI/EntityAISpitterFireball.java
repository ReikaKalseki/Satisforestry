package Reika.Satisforestry.Entity.AI;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import Reika.DragonAPI.Auxiliary.Trackers.KeyWatcher;
import Reika.DragonAPI.Auxiliary.Trackers.KeyWatcher.Key;
import Reika.DragonAPI.Auxiliary.Trackers.TickScheduler;
import Reika.DragonAPI.Instantiable.Event.ScheduledTickEvent;
import Reika.DragonAPI.Instantiable.Event.ScheduledTickEvent.ScheduledEntitySpawn;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.Entity.EntitySpitter;
import Reika.Satisforestry.Entity.EntitySpitterFireball;
import Reika.Satisforestry.Entity.EntitySplittingSpitterFireball;

public class EntityAISpitterFireball extends EntityAIBase
{
	private final EntitySpitter entityHost;

	private final int maxRangedAttackTime;
	private final double minDistSq;
	private final double maxDistSq;
	private final double entityMoveSpeed;

	private final double fireballSpeed;
	private final float fireballDamage;

	private EntityLivingBase attackTarget;
	private int rangedAttackTime;
	private int ticksOfSight;
	private double currentDistSq;

	public EntityAISpitterFireball(EntitySpitter e, double speed, int maxTime, double mind, double maxd, double fs, float fd) {
		rangedAttackTime = -1;
		entityHost = e;
		entityMoveSpeed = speed;
		maxRangedAttackTime = maxTime;
		minDistSq = mind * mind;
		maxDistSq = maxd * maxd;

		fireballSpeed = fs;
		fireballDamage = fd;

		this.setMutexBits(3);
	}

	@Override
	public final boolean shouldExecute() {
		attackTarget = entityHost.getAttackTarget();
		this.fetchDistance();
		return attackTarget != null && this.isDistanceAppropriate();
	}

	@Override
	public final boolean continueExecuting() {
		return this.shouldExecute() || !entityHost.getNavigator().noPath();
	}

	private final boolean isDistanceAppropriate() {
		return currentDistSq >= 0 && currentDistSq <= maxDistSq && currentDistSq >= minDistSq;
	}

	@Override
	public final void resetTask() {
		attackTarget = null;
		ticksOfSight = 0;
		rangedAttackTime = -1;
		currentDistSq = -1;
	}

	@Override
	public final void updateTask() {
		if (attackTarget == null)
			return;
		boolean flag = entityHost.getEntitySenses().canSee(attackTarget);

		if (flag) {
			++ticksOfSight;
		}
		else {
			ticksOfSight = 0;
		}

		if (currentDistSq <= maxDistSq && ticksOfSight >= 20) {
			entityHost.getNavigator().clearPathEntity();
		}
		else {
			entityHost.getNavigator().tryMoveToEntityLiving(attackTarget, entityMoveSpeed);
		}

		entityHost.getLookHelper().setLookPositionWithEntity(attackTarget, 30.0F, 30.0F);

		if (--rangedAttackTime == 0) {
			if (!flag || !this.isDistanceAppropriate()) {
				return;
			}

			this.fireFireball();
			rangedAttackTime = maxRangedAttackTime;
		}
		else if (rangedAttackTime < 0) {
			rangedAttackTime = maxRangedAttackTime;
		}
	}

	protected final void fetchDistance() {
		if (entityHost == null || !entityHost.isEntityAlive() || attackTarget == null || !attackTarget.isEntityAlive()) {
			currentDistSq = -1;
			return;
		}
		currentDistSq = entityHost.getDistanceSq(attackTarget.posX, attackTarget.boundingBox.minY, attackTarget.posZ);
	}

	protected final void fireFireball() {
		World world = entityHost.worldObj;
		Random rand = entityHost.getRNG();
		double dl = 4;
		double vx = 0;
		double vz = 0;
		if (attackTarget instanceof EntityPlayer) {
			EntityPlayer ep = (EntityPlayer)attackTarget;
			boolean fwd = KeyWatcher.instance.isKeyDown(ep, Key.FORWARD);
			boolean back = KeyWatcher.instance.isKeyDown(ep, Key.BACK);
			boolean left = KeyWatcher.instance.isKeyDown(ep, Key.LEFT);
			boolean right = KeyWatcher.instance.isKeyDown(ep, Key.RIGHT);
			double v = attackTarget.isSneaking() ? 0.3 : 1;
			double fwdSpeed = fwd == back ? 0 : (fwd ? v : -v);
			double sideSpeed = left == right ? 0 : (left ? v : -v); //+ve to left
			float f4 = MathHelper.sin(attackTarget.rotationYaw * (float)Math.PI / 180.0F);
			float f5 = MathHelper.cos(attackTarget.rotationYaw * (float)Math.PI / 180.0F);
			vx = sideSpeed * f5 - fwdSpeed * f4;
			vz = fwdSpeed * f5 + sideSpeed * f4;
		}
		double dx = attackTarget.posX+vx*dl - entityHost.posX;
		double dy = this.getYTarget(attackTarget, entityHost)+attackTarget.motionY*dl;
		double dz = attackTarget.posZ+vz*dl - entityHost.posZ;
		this.doFireFireball(world, entityHost, attackTarget, dx, dy, dz, fireballSpeed, fireballDamage);
	}

	protected void doFireFireball(World world, EntitySpitter src, EntityLivingBase tgt, double vx, double vy, double vz, double sp, float dmg) {
		EntitySpitterFireball esf = new EntitySpitterFireball(world, src, vx, vy, vz, sp, dmg);
		esf.posY = entityHost.posY + entityHost.height / 2.0F + 0.5D;
		world.spawnEntityInWorld(esf);
	}

	public static class EntityAISpitterClusterFireball extends EntityAISpitterFireball {

		public EntityAISpitterClusterFireball(EntitySpitter e, double speed, int maxTime, double mind, double maxd, double fs, float fd) {
			super(e, speed, maxTime, mind, maxd, fs, fd);
		}

		@Override
		protected void doFireFireball(World world, EntitySpitter src, EntityLivingBase tgt, double vx, double vy, double vz, double sp, float dmg) {
			for (int i = 0; i < 18; i++) {
				double vx2 = ReikaRandomHelper.getRandomPlusMinus(vx, 0.3);
				double vy2 = ReikaRandomHelper.getRandomPlusMinus(vy, 0.15);
				double vz2 = ReikaRandomHelper.getRandomPlusMinus(vz, 0.3);
				EntitySpitterFireball esf = new EntitySpitterFireball(world, src, vx2, vy2, vz2, sp, dmg);
				esf.posY = src.posY + src.height / 2.0F + 0.5D;
				double px = ReikaRandomHelper.getRandomPlusMinus(src.posX, 0.5);
				double pz = ReikaRandomHelper.getRandomPlusMinus(src.posZ, 0.5);
				esf.setLocationAndAngles(px, esf.posY, pz, 0, 0);
				//world.spawnEntityInWorld(esf);
				TickScheduler.instance.scheduleEvent(new ScheduledTickEvent(new ScheduledEntitySpawn(esf)), 1+i*5);
				//queueEntitySpawn(esf, world, i*4);
			}
		}

	}

	public static class EntityAISpitterSplittingFireball extends EntityAISpitterFireball {

		public EntityAISpitterSplittingFireball(EntitySpitter e, double speed, int maxTime, double mind, double maxd, double fs, float fd) {
			super(e, speed, maxTime, mind, maxd, fs, fd);
		}

		@Override
		protected void doFireFireball(World world, EntitySpitter src, EntityLivingBase tgt, double vx, double vy, double vz, double sp, float dmg) {
			EntitySplittingSpitterFireball esf = new EntitySplittingSpitterFireball(world, src, tgt, vx, vy, vz, sp, dmg);
			esf.posY = src.posY + src.height / 2.0F + 0.5D;
			world.spawnEntityInWorld(esf);
		}

	}

	public static double getYTarget(EntityLivingBase e, EntitySpitter src) {
		return e.boundingBox.minY + e.height / 2.0F - (src.posY + src.height / 2.0F);
	}
}