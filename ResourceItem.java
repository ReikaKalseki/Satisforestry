package Reika.Satisforestry;

import net.minecraft.item.ItemStack;

public class ResourceItem {

	public final ItemStack item;
	public final int spawnWeight;

	public ResourceItem(ItemStack is, int w) {
		item = is.copy();
		spawnWeight = w;
	}

}
