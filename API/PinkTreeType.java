package Reika.Satisforestry.API;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface PinkTreeType {

	public String name();

	public int ordinal();

	@SideOnly(Side.CLIENT)
	public int getBasicRenderColor();

	public ItemStack getBaseLog();
	public ItemStack getBaseLeaf();
	public ItemStack getSapling();

}
