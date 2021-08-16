package Reika.Satisforestry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import Reika.Satisforestry.Registry.SFBlocks;

public class UpgradeHandler {

	public static final UpgradeHandler instance = new UpgradeHandler();

	private static final String NBT_KEY = "slugDelegate";

	private UpgradeHandler() {

	}

	public ItemStack overrideSlotRender(Slot s, ItemStack is) {
		ItemStack slug = this.getSlugNBT(is);
		if (slug != null) {
			return slug;
		}
		return null;
	}

	private ItemStack getSlugNBT(ItemStack is) {
		if (is != null && is.stackTagCompound != null && is.stackTagCompound.hasKey(NBT_KEY)) {
			return ItemStack.loadItemStackFromNBT(is.stackTagCompound.getCompoundTag(NBT_KEY));
		}
		return null;
	}

	public void addToSlot(IInventory ii, int slot, ItemStack is) {
		if (SFBlocks.SLUG.matchWith(is)) {
			int tier = is.getItemDamage()%3;
			ItemStack convert = this.getSlugUpgrade(ii, slot);
			if (convert != null) {
				ii.setInventorySlotContents(slot, this.makeSlugUpgrade(convert, is));
			}
		}
	}

	private ItemStack getSlugUpgrade(IInventory ii, int slot) {
		return null; //TODO
	}

	private ItemStack makeSlugUpgrade(ItemStack convert, ItemStack is) {
		ItemStack ret = convert.copy();
		if (ret.stackTagCompound == null)
			ret.stackTagCompound = new NBTTagCompound();
		NBTTagCompound tag = new NBTTagCompound();
		is.writeToNBT(tag);
		ret.stackTagCompound.setTag(NBT_KEY, tag);
		return ret;
	}

	public void takeFromSlot(IInventory ii, int slot, ItemStack is, EntityPlayer ep) {
		ItemStack slug = this.getSlugNBT(is);
		if (slug != null) {
			ep.inventory.setItemStack(slug);
		}
	}

}
