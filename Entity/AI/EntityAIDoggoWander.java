package Reika.Satisforestry.Entity.AI;

import net.minecraft.entity.ai.EntityAIWander;

import Reika.Satisforestry.Entity.EntityLizardDoggo;


public class EntityAIDoggoWander extends EntityAIWander {

	private final EntityLizardDoggo doggo;

	public EntityAIDoggoWander(EntityLizardDoggo e, double sp) {
		super(e, sp);
		doggo = e;
	}

	@Override
	public boolean shouldExecute() {
		return super.shouldExecute() && !doggo.isSitting() && !doggo.isTamed();
	}

}
