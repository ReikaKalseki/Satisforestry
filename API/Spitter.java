package Reika.Satisforestry.API;

import net.minecraft.entity.EntityLivingBase;

public interface Spitter {

	/** What type of spitter this is. 0 is basic, 1 is red alpha, 2 is green alpha. If you have no idea what that means, look at the Satisfactory wiki. */
	public int getTypeIndex();

	/** Causes the spitter to fire at the entity. Will choose attack type based on distance. */
	public void fireFireballAt(EntityLivingBase target);

	/** Causes the spitter to do its short-range knockback blast attack. */
	public void doKnockbackBlast(EntityLivingBase target);

	public double getSightDistance();

}
