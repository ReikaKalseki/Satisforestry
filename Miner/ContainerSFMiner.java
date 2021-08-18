package Reika.Satisforestry.Miner;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import Reika.DragonAPI.Base.CoreContainer;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;


public class ContainerSFMiner extends CoreContainer {

	private final TileNodeHarvester tile;

	public ContainerSFMiner(EntityPlayer player, TileNodeHarvester te) {
		super(player, te, te.getOutput());
		tile = te;

		this.addSlotNoClick(0, 80, 55);

		OverclockingInv clock = te.getOverClockingHandler();
		this.addSlotToContainer(new OverclockSlot(clock, 0, 80, 104));
		this.addSlotToContainer(new OverclockSlot(clock, 1, 105, 104));
		this.addSlotToContainer(new OverclockSlot(clock, 2, 130, 104));

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

		private final OverclockingInv reference;
		private final int slotIndex;

		public OverclockSlot(OverclockingInv ii, int idx, int x, int y) {
			super(ii, idx, x, y);
			reference = ii;
			slotIndex = idx;
		}

		@Override
		public boolean isItemValid(ItemStack is) {
			return reference.isItemValidForSlot(slotIndex, is);
		}

		@Override
		public boolean canTakeStack(EntityPlayer ep) {
			return slotIndex == reference.getLastOccupiedSlot();//slotNumber == 2 || reference.getItems()[slotNumber+1] == null;
		}

	}



}
