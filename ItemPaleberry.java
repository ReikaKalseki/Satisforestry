package Reika.Satisforestry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class ItemPaleberry extends ItemFood {

	public ItemPaleberry() {
		super(4, 1F, false);
		this.setMaxStackSize(32);
		this.setAlwaysEdible();
		this.setCreativeTab(Satisforestry.tabCreative);
	}

	@Override
	protected void onFoodEaten(ItemStack is, World world, EntityPlayer ep) {
		ep.heal(2); //heal one heart
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected String getIconString() {
		return "satisforestry:paleberry";
	}

}
