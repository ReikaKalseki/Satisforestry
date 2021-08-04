package Reika.Satisforestry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import Reika.DragonAPI.Base.CoreContainer;
import Reika.DragonAPI.Instantiable.BasicInventory;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
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

		this.addSlot(0, 80, 55);

		this.addSlotToContainer(new Slot(clock, 1, 80, 104));
		this.addSlotToContainer(new Slot(clock, 2, 105, 104));
		this.addSlotToContainer(new Slot(clock, 3, 130, 104));

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

	@Override
	public ItemStack slotClick(int ID, int par2, int par3, EntityPlayer ep) {
		ItemStack is = super.slotClick(ID, par2, par3, ep);
		Slot s = (Slot)inventorySlots.get(ID);
		if (s.inventory == clock) {
			ReikaJavaLibrary.pConsole(s.slotNumber);
		}
		return is;
	}

	private static class OverclockingInvDelegate extends BasicInventory {

		private final TileNodeHarvester tile;

		public OverclockingInvDelegate(TileNodeHarvester te) {
			super("Overclock", 4);
			tile = te;
			ItemStack is = te.getOverclockingItem();
			for (int i = 0; i < te.getOverclockingStep(); i++)
				if (i != 0)
					this.setInventorySlotContents(i, is);
		}

		@Override
		public boolean isItemValidForSlot(int slot, ItemStack is) {
			return ReikaItemHelper.matchStacks(is, tile.getOverclockingItem());
		}

	}

}
