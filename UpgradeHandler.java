package Reika.Satisforestry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.IC2Handler;
import Reika.Satisforestry.Registry.SFBlocks;

import cofh.api.energy.IEnergyProvider;
import cofh.api.tileentity.IAugmentable;

public class UpgradeHandler {

	public static final UpgradeHandler instance = new UpgradeHandler();

	private static final String NBT_KEY = "slugDelegate";

	private static final int[] UPGRADE_COUNTS = {1, 2, 5};

	private Class ic2Upgradeable;
	//private Class teUpgradeable;
	private Class eioUpgradeable;

	private UpgradeHandler() {
		if (ModList.IC2.isLoaded()) {
			ic2Upgradeable = this.loadClass("ic2.core.upgrade.IUpgradableBlock");
		}
		if (ModList.ENDERIO.isLoaded()) {
			eioUpgradeable = this.loadClass("crazypants.enderio.machine.AbstractPoweredMachineEntity");
		}
		if (ModList.THERMALEXPANSION.isLoaded()) {
			//this.teUpgradeable = this.loadClass("cofh.thermalexpansion.block.TileAugmentable")
		}
	}

	private Class loadClass(String s) {
		try {
			return Class.forName(s);
		}
		catch (ClassNotFoundException e) {
			Satisforestry.logger.logError("Could not find class for upgrade handler: "+s);
			return null;
		}
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

	public void addToSlot(IInventory ii, int slot, Container c, EntityPlayer ep) {
		if (ii == null)
			return;
		ItemStack is = ep.inventory.getItemStack();
		if (SFBlocks.SLUG.matchWith(is) && is.stackSize == 1) {
			int tier = is.getItemDamage()%3;
			ItemStack convert = this.getSlugUpgrade(ii, slot, c, tier);
			if (convert != null) {
				ItemStack get = this.makeSlugUpgrade(convert, is);
				//ii.setInventorySlotContents(slot, get);
				ep.inventory.setItemStack(get);
			}
		}
	}

	private ItemStack getSlugUpgrade(IInventory ii, int slot, Container c, int tier) {
		if (ic2Upgradeable != null && ic2Upgradeable.isAssignableFrom(ii.getClass())) {
			if (slot >= 36) //ic2 containers put player inv first
				slot -= 36;
			if (this.containerAccepts(c, slot, IC2Handler.IC2Stacks.OVERCLOCK.getItem())) {
				return ReikaItemHelper.getSizedItemStack(IC2Handler.IC2Stacks.OVERCLOCK.getItem(), UPGRADE_COUNTS[tier]);
			}
		}
		if (ii instanceof IAugmentable) {
			int base = ii instanceof IEnergyProvider ? 80 : 128;
			base += tier;
			ItemStack item = ReikaItemHelper.lookupItem("ThermalExpansion:augment:"+base);
			if (this.containerAccepts(c, slot, item)) {
				return item;
			}
		}
		if (eioUpgradeable != null && eioUpgradeable.isAssignableFrom(ii.getClass())) {
			ItemStack item = ReikaItemHelper.lookupItem("EnderIO:itemBasicCapacitor:"+tier);
			if (this.containerAccepts(c, slot, item) ) {
				return item;
			}
		}
		return null;
	}

	private boolean containerAccepts(Container c, int slot, ItemStack item) {
		return slot >= 0 && slot < c.inventorySlots.size() && c.getSlot(slot).isItemValid(item);
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
