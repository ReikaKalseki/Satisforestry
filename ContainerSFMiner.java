package Reika.Satisforestry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import Reika.DragonAPI.Base.CoreContainer;
import Reika.DragonAPI.Instantiable.BasicInventory;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.Satisforestry.Blocks.TileNodeHarvester;


public class ContainerSFMiner extends CoreContainer {

	private final TileNodeHarvester tile;

	private final OverclockingInvDelegate clock;

	public ContainerSFMiner(EntityPlayer player, TileNodeHarvester te) {
		super(player, te, te.getOutput());
		tile = te;
		clock = new OverclockingInvDelegate(te);

		this.addSlotNoClick(0, 80, 55);

		this.addSlotToContainer(new OverclockSlot(clock, 1, 80, 104));
		this.addSlotToContainer(new OverclockSlot(clock, 2, 105, 104));
		this.addSlotToContainer(new OverclockSlot(clock, 3, 130, 104));

		this.addPlayerInventoryWithOffset(player, 0, 53);
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		for (int i = 0; i < crafters.size(); i++) {
			ICrafting icrafting = (ICrafting)crafters.get(i);
			icrafting.sendProgressBarUpdate(this, 0, (int)(1000*tile.progressFactor));
			icrafting.sendProgressBarUpdate(this, 1, (int)(1000*tile.powerBar));
		}
	}

	@Override
	public void updateProgressBar(int par1, int par2)
	{
		switch(par1) {
			case 0: tile.progressFactor = par2/1000F; break;
			case 1: tile.powerBar = par2/1000F; break;
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return super.canInteractWith(player) || ReikaMathLibrary.py3d(tile.xCoord+0.5-player.posX, (tile.yCoord+0.5-player.posY)/2, tile.zCoord+0.5-player.posZ) <= 12;
	}
	/*
	@Override
	public ItemStack slotClick(int ID, int par2, int par3, EntityPlayer ep) {
		ItemStack is = super.slotClick(ID, par2, par3, ep);
		if (ID >= 0 && ID < inventorySlots.size() && !ep.worldObj.isRemote && ((Slot)inventorySlots.get(ID)).inventory == clock) {
			clock.setOverclockLevels();
		}
		return is;
	}*/

	private static class OverclockSlot extends Slot {

		private final OverclockingInvDelegate reference;

		public OverclockSlot(OverclockingInvDelegate ii, int idx, int x, int y) {
			super(ii, idx, x, y);
			reference = ii;
		}

		@Override
		public boolean isItemValid(ItemStack is) {
			return reference.isItemValidForSlot(slotNumber, is);
		}

		@Override
		public boolean canTakeStack(EntityPlayer ep) {
			return slotNumber == 3 || reference.getItems()[slotNumber+1] == null;
		}

	}

	private static class OverclockingInvDelegate extends BasicInventory {

		private final TileNodeHarvester tile;

		public OverclockingInvDelegate(TileNodeHarvester te) {
			super("Overclock", 4, 1);
			if (!te.worldObj.isRemote) {
				ItemStack is = te.getOverclockingItem();
				int step = te.getOverclockingStep(); //cacche since can change due to onSlotChange
				for (int i = 0; i <= step; i++)
					if (i != 0) {
						this.setInventorySlotContents(i, is);
					}
			}
			tile = te;
		}

		@Override
		protected void onSlotChange(int slot) {
			if (tile != null && tile.worldObj != null && !tile.worldObj.isRemote)
				this.setOverclockLevels();
		}

		private void setOverclockLevels() {
			int lvl = 0;
			for (int i = 1; i <= 3; i++) {
				if (this.getItems()[i] != null)
					lvl++;
			}
			tile.setOverclock(lvl);
			//ReikaJavaLibrary.pConsole(lvl+" from "+Arrays.toString(this.getItems()));
		}

		@Override
		public boolean isItemValidForSlot(int slot, ItemStack is) {
			return (slot == 1 || this.getItems()[slot-1] != null) && ReikaItemHelper.matchStacks(is, tile.getOverclockingItem());
		}

	}

}
