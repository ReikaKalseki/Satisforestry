package Reika.Satisforestry.Entity.AI;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;

import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.Satisforestry.Entity.EntitySpitter;

public abstract class EntityAISpitterReposition extends EntityAIDistanceDependent {

	protected final EntitySpitter entity;

	protected EntityLivingBase attackTarget;

	private double xPosition;
	private double yPosition;
	private double zPosition;

	public EntityAISpitterReposition(EntitySpitter e, double min, double max) {
		super(min, max);
		entity = e;
		this.setMutexBits(9);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public final boolean shouldExecute() {
		if (entity.canReposition() && this.isTargetValid()) {
			Vec3 vec3 = this.getTargetPosition();
			if (vec3 == null) {
				xPosition = Double.NaN;
				yPosition = Double.NaN;
				zPosition = Double.NaN;
				if (this.needsTargetPosition())
					return false;
			}
			else {
				xPosition = vec3.xCoord;
				yPosition = vec3.yCoord;
				zPosition = vec3.zCoord;
			}
			return true;
		}
		else {
			return false;
		}
	}

	protected abstract boolean needsTargetPosition();
	protected abstract Vec3 getTargetPosition();

	protected final boolean isTargetValid() {
		attackTarget = entity.getAttackTarget();
		if (attackTarget == null) {
			//ReikaJavaLibrary.pConsole(entity.getSpitterType()+" "+new DecimalPosition(entity)+" ending "+this+" becayse no target");
			return false;
		}
		this.fetchDistance(entity, attackTarget);
		//ReikaJavaLibrary.pConsole(entity.getSpitterType()+" "+new DecimalPosition(entity)+" ending "+this+" because wrong dist @ "+Math.sqrt(currentDistSq), !this.isDistanceAppropriate());
		return this.isDistanceAppropriate();
	}

	@Override
	public final boolean continueExecuting() {
		return this.isTargetValid() && !entity.getNavigator().noPath() && this.getDistanceToTarget() > this.getStoppingDistance()*this.getStoppingDistance();
	}

	protected final double getDistanceToTarget() {
		return this.needsTargetPosition() ? entity.getDistanceSq(xPosition, yPosition, zPosition) : entity.getDistanceSqToEntity(attackTarget);
	}

	protected abstract double getStoppingDistance();

	@Override
	public final void resetTask() {
		super.resetTask();
		attackTarget = null;
	}

	@Override
	public final void startExecuting() {
		//ReikaJavaLibrary.pConsole(entity.getSpitterType()+" "+new DecimalPosition(entity)+" executing "+this);
		entity.setRunning(this.isRunning());
		if (Double.isFinite(xPosition) && entity.worldObj.func_147447_a(Vec3.createVectorHelper(xPosition, yPosition+0.5, zPosition), Vec3.createVectorHelper(entity.posX, entity.posY+entity.height/2, entity.posZ), false, true, false) == null) {
			if (entity.getNavigator().tryMoveToXYZ(xPosition, yPosition, zPosition, entity.getAIMoveSpeed()*2))
				entity.setRepositioned();
		}
		else {
			entity.getNavigator().tryMoveToEntityLiving(attackTarget, entity.getAIMoveSpeed()*3);
		}
		ReikaJavaLibrary.pConsole("Repositioning to "+(Double.isFinite(xPosition) ? Vec3.createVectorHelper(xPosition, yPosition, zPosition) : attackTarget));
	}

	@Override
	public void updateTask() {
		entity.setRunning(this.isRunning());
		//ReikaJavaLibrary.pConsole(entity.getSpitterType()+" "+new DecimalPosition(entity)+" ticking "+this);
	}

	@Override
	public boolean isInterruptible() {
		return super.isInterruptible();//false;
	}

	protected abstract boolean isRunning();
}