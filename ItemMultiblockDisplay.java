package Reika.Satisforestry;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class ItemMultiblockDisplay extends Item {

	public ItemMultiblockDisplay() {
		this.setMaxStackSize(1);
		this.setCreativeTab(Satisforestry.tabCreative);
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected String getIconString() {
		return "satisforestry:multiblockpage";
	}

	@Override
	public ItemStack onItemRightClick(ItemStack is, World world, EntityPlayer ep) {
		ep.openGui(Satisforestry.instance, 1, world, is.getItemDamage(), 0, 0);
		return super.onItemRightClick(is, world, ep);
	}

	@Override
	public final void getSubItems(Item i, CreativeTabs tab, List li) {
		li.add(new ItemStack(i, 1, 0));
		li.add(new ItemStack(i, 1, 1));
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer ep, List li, boolean vb) {
		switch(is.getItemDamage()) {
			case 0:
				li.add("Miner Blueprint");
				break;
			case 1:
				li.add("Fracking Pressurizer Blueprint");
				break;
		}
	}

}
