package Reika.Satisforestry.Miner;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import Reika.DragonAPI.Instantiable.BasicInventory;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.Satisforestry.Registry.SFBlocks;

public class OverclockingInv extends BasicInventory {

	private final TileResourceHarvesterBase tile;

	private int currentOverclock;

	public OverclockingInv(TileResourceHarvesterBase te) {
		super("Overclock", 3, 1);
		tile = te;
		this.onSlotChange(0);
	}

	public int getOverclockingLevel() {
		return currentOverclock;
	}

	@Override
	protected void onSlotChange(int slot) {
		this.setOverclockLevels();
	}

	private void setOverclockLevels() {
		ItemStack[] items = this.getItems();
		currentOverclock = 0;
		for (int i = 0; i < 3; i++) {
			if (items[i] != null)
				currentOverclock += this.getOverclockValue(items[i]);
		}
		if (tile != null && tile.worldObj != null && !tile.worldObj.isRemote)
			tile.syncAllData(false);
		//ReikaJavaLibrary.pConsole(lvl+" from "+Arrays.toString(this.getItems()));
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack is) {
		if (slot < 0 || slot != this.getSlotForInsertion())
			return false;
		int value = this.getOverclockValue(is);
		return value > 0 && currentOverclock+value <= 3;
	}

	private int getSlotForInsertion() {
		ItemStack[] items = this.getItems();
		int i = 0;
		while (i < items.length && items[i] != null) {
			i += this.getOverclockValue(items[i]);
		}
		return i < items.length ? i : -1;
	}

	public int getLastOccupiedSlot() {
		ItemStack[] items = this.getItems();
		for (int i = 2; i >= 0; i--) {
			if (items[i] != null)
				return i;
		}
		return -1;
	}

	private int getOverclockValue(ItemStack is) {
		if (SFBlocks.SLUG.matchWith(is))
			return 1+is.getItemDamage()%3;
		else if (ReikaItemHelper.matchStacks(is, tile.getOverclockingItem()))
			return 1;
		return 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound NBT, String tag) {
		super.readFromNBT(NBT, tag);
		this.onSlotChange(0);
	}

}
