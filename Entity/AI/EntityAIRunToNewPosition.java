package Reika.Satisforestry.Entity.AI;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.Vec3;

import Reika.Satisforestry.Entity.EntitySpitter;

public class EntityAIRunToNewPosition extends EntityAIBase {

	private EntitySpitter entity;
	private double xPosition;
	private double yPosition;
	private double zPosition;

	public EntityAIRunToNewPosition(EntitySpitter e) {
		entity = e;
		this.setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {
		if (entity.getEntityToAttack() == null) {
			return false;
		}
		else {
			Vec3 vec3 = RandomPositionGenerator.findRandomTarget(entity, 5, 4);

			if (vec3 == null) {
				return false;
			}
			else {
				xPosition = vec3.xCoord;
				yPosition = vec3.yCoord;
				zPosition = vec3.zCoord;
				return true;
			}
		}
	}

	@Override
	public boolean continueExecuting() {
		return !entity.getNavigator().noPath();
	}

	@Override
	public void startExecuting() {
		entity.getNavigator().tryMoveToXYZ(xPosition, yPosition, zPosition, entity.getAIMoveSpeed());
	}
}