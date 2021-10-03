package Reika.Satisforestry.Entity.AI;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;

import Reika.DragonAPI.Libraries.ReikaEntityHelper;
import Reika.Satisforestry.Entity.EntitySpitter;

public class EntityAISpitterBlast extends EntityAIBase {

	private final EntitySpitter spitter;

	private final double maxDistance;
	private final float damageScale;

	private EntityLivingBase target;

	public EntityAISpitterBlast(EntitySpitter e, double maxDist, float scale) {
		spitter = e;
		damageScale = scale;
		maxDistance = maxDist;
		this.setMutexBits(5);
	}

	@Override
	public boolean shouldExecute() {
		target = spitter.getAttackTarget();

		if (target == null || !target.isEntityAlive()) {
			return false;
		}
		double d0 = spitter.getDistanceSqToEntity(target);
		return d0 <= maxDistance*maxDistance;
	}

	@Override
	public boolean continueExecuting() {
		return true;
	}

	@Override
	public void startExecuting() {
		target.attackEntityFrom(DamageSource.causeMobDamage(spitter), damageScale*4);
		ReikaEntityHelper.knockbackEntity(spitter, target, 1.5*damageScale);
	}
}