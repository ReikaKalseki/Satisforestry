package Reika.Satisforestry.Entity;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.world.World;

public class EntityLizardDoggo extends EntityTameable {

	public EntityLizardDoggo(World w) {
		super(w);
	}

	@Override
	public EntityAgeable createChild(EntityAgeable e) {
		return null;
	}

}
