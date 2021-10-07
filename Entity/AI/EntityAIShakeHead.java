package Reika.Satisforestry.Entity.AI;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;

import Reika.Satisforestry.Entity.EntitySpitter;

public class EntityAIShakeHead extends EntityAIBase {

	private EntitySpitter spitter;
	private PathNavigate pathfinder;

	public EntityAIShakeHead(EntitySpitter e, double dd, double sp) {
		spitter = e;
		pathfinder = e.getNavigator();
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		return this.doCheck(true);
	}

	private boolean doCheck(boolean start) {
		return spitter.getEntityToAttack() == null && spitter.onGround && (start ? spitter.canInitiateHeadshake() : spitter.canContinueHeadshake());
	}

	@Override
	public boolean continueExecuting() {
		return this.doCheck(false);
	}

	@Override
	public void startExecuting() {
		pathfinder.clearPathEntity();
		spitter.initiateHeadShake();
	}

	@Override
	public void resetTask() {
		pathfinder.clearPathEntity();
	}

	@Override
	public void updateTask() {
		pathfinder.clearPathEntity();
	}
}