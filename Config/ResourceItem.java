package Reika.Satisforestry.Config;

import java.util.HashMap;

import net.minecraft.item.ItemStack;

import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;

public class ResourceItem extends NodeResource<ItemStack> {

	public ResourceItem(String s, String n, int w, int c, HashMap<String, Object> map) {
		super(s, n, w, c, map);
	}

	@Override
	public ItemStack getItem(NodeItem obj) {
		return obj.item.copy();
	}

	@Override
	public boolean matchItem(NodeItem obj, ItemStack is) {
		return ReikaItemHelper.matchStacks(is, obj.item);
	}

}
