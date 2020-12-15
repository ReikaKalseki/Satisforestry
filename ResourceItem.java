package Reika.Satisforestry;

import net.minecraft.item.ItemStack;

public class ResourceItem {

	public final String id;
	public final ItemStack item;
	public final int spawnWeight;

	public int minCount = 1;
	public int maxCount = 1;

	public ResourceItem(String s, ItemStack is, int w) {
		id = s;
		item = is.copy();
		spawnWeight = w;
	}

}
