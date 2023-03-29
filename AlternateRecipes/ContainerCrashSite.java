package Reika.Satisforestry.AlternateRecipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;

import Reika.DragonAPI.Base.CoreContainer;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.Satisforestry.Blocks.BlockCrashSite.TileCrashSite;


public class ContainerCrashSite extends CoreContainer {

	private final TileCrashSite tile;

	public ContainerCrashSite(EntityPlayer player, TileCrashSite te) {
		super(player, te);
		tile = te;

		this.addSlot(0, 26, 74);

		this.addPlayerInventoryWithOffset(player, 0, 20);
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		for (int i = 0; i < crafters.size(); i++) {
			ICrafting icrafting = (ICrafting)crafters.get(i);
			icrafting.sendProgressBarUpdate(this, 0, (int)(1000*tile.progressFactor));
		}
	}

	@Override
	public void updateProgressBar(int par1, int par2)
	{
		switch(par1) {
			case 0: tile.progressFactor = par2/1000F; break;
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return super.canInteractWith(player) || ReikaMathLibrary.py3d(tile.xCoord+0.5-player.posX, (tile.yCoord+0.5-player.posY)/2, tile.zCoord+0.5-player.posZ) <= 8;
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

}
