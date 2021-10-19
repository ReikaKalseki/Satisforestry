package Reika.Satisforestry.API;

import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.item.ItemStack;

/** Implemented by EntityLizardDoggo. */
public interface LizardDoggo {

	/** Causes the lizard doggo to "find" an item if it did not previously have one. */
	public void generateItem();

	public boolean hasItem();

	/** Takes the lizard doggo's item, freeing it to find more. */
	public ItemStack takeItem();

	/** see {@link EntityTameable}. */
	public boolean isTamed();

}
