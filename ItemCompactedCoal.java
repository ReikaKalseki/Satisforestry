package Reika.Satisforestry;

import net.minecraft.item.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class ItemCompactedCoal extends Item {

	public ItemCompactedCoal() {
		this.setMaxStackSize(32);
		this.setCreativeTab(Satisforestry.tabCreative);
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected String getIconString() {
		return "satisforestry:ccoal";
	}

}
