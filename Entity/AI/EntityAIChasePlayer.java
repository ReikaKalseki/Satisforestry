package Reika.Satisforestry.Entity.AI;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.Vec3;

import Reika.Satisforestry.Entity.EntitySpitter;

public class EntityAIChasePlayer extends EntityAISpitterReposition {

	public EntityAIChasePlayer(EntitySpitter e) {
		super(e, e.getSpitterType().getPursuitDistance(), e.getEntityAttribute(SharedMonsterAttributes.followRange).getAttributeValue());
	}

	@Override
	protected Vec3 getTargetPosition() {
		return null;
	}

	@Override
	protected boolean needsTargetPosition() {
		return false;
	}

	@Override
	protected boolean isRunning() {
		return true;
	}

	@Override
	protected double getStoppingDistance() {
		return entity.getSpitterType().isAlpha() ? 12 : 5;
	}
}