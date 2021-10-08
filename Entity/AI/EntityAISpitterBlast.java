package Reika.Satisforestry.Entity.AI;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;

import Reika.DragonAPI.Libraries.ReikaEntityHelper;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.SFPacketHandler.SFPackets;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Entity.EntitySpitter;
import Reika.Satisforestry.Registry.SFSounds;

public class EntityAISpitterBlast extends EntityAIBase {

	private final EntitySpitter spitter;

	private final double maxDistance;
	private final float damageScale;

	private EntityLivingBase target;

	public EntityAISpitterBlast(EntitySpitter e, double maxDist, float scale) {
		spitter = e;
		damageScale = scale;
		maxDistance = maxDist;
		this.setMutexBits(0);
	}

	@Override
	public boolean shouldExecute() {
		if (!spitter.isBlastReady())
			return false;
		target = spitter.getAttackTarget();

		if (target == null || !target.isEntityAlive()) {
			return false;
		}
		double d0 = spitter.getDistanceSqToEntity(target);
		return d0 <= maxDistance*maxDistance;
	}

	@Override
	public boolean continueExecuting() {
		return this.shouldExecute();
	}

	@Override
	public void startExecuting() {
		spitter.getLookHelper().setLookPositionWithEntity(target, 10.0F, spitter.getVerticalFaceSpeed());
		target.attackEntityFrom(DamageSource.causeMobDamage(spitter), damageScale*4);
		ReikaEntityHelper.knockbackEntity(spitter, target, damageScale, 0.1);
		target.motionY = 0.4*damageScale;
		ReikaPacketHelper.sendDataPacketWithRadius(Satisforestry.packetChannel, SFPackets.SPITTERBLAST.ordinal(), spitter, 64, spitter.getEntityId());
		SFSounds.SPITTERBLAST.playSound(spitter, 1, (float)ReikaRandomHelper.getRandomBetween(0.8, 1.2));
		spitter.resetBlastTimer();
	}
}