package Reika.Satisforestry;

import java.util.List;
import java.util.UUID;

import com.google.common.base.Strings;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
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
		if (isOwned(is, ep))
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
		if (is.stackTagCompound != null) {
			String ownerID = is.stackTagCompound.getString("owner");
			if (!Strings.isNullOrEmpty(ownerID)) {
				String ownerName = is.stackTagCompound.getString("ownerName");
				li.add("This visualization is owned by "+(isOwned(is, ep) ? EnumChatFormatting.GREEN : EnumChatFormatting.RED)+ownerName);
			}
		}
	}

	public static boolean isOwned(ItemStack is, EntityPlayer ep) {
		if (is.stackTagCompound == null)
			return true;
		String ownerID = is.stackTagCompound.getString("owner");
		if (Strings.isNullOrEmpty(ownerID))
			return true;
		return ep.getUniqueID().equals(UUID.fromString(ownerID));
	}

	public static void setOwner(ItemStack is, EntityPlayer ep) {
		is.stackTagCompound = new NBTTagCompound();
		is.stackTagCompound.setString("owner", ep.getUniqueID().toString());
		is.stackTagCompound.setString("ownerName", ep.getCommandSenderName());
	}

}
