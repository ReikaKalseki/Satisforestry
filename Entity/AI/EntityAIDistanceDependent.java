package Reika.Satisforestry.Entity.AI;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;

public abstract class EntityAIDistanceDependent extends EntityAIBase {

	protected final double minDistSq;
	protected final double maxDistSq;

	protected double currentDistSq;

	public EntityAIDistanceDependent(double min, double max) {
		minDistSq = min*min;
		maxDistSq = max*max;
	}

	protected final boolean isDistanceAppropriate() {
		return currentDistSq <= maxDistSq && (currentDistSq >= minDistSq || minDistSq <= 0);
	}

	protected final void fetchDistance(Entity src, Entity to) {
		if (src == null || !src.isEntityAlive() || to == null || !to.isEntityAlive()) {
			currentDistSq = -1;
			return;
		}
		currentDistSq = src.getDistanceSq(to.posX, to.boundingBox.minY, to.posZ);
	}

	@Override
	public void resetTask() {
		currentDistSq = -1;
	}
}