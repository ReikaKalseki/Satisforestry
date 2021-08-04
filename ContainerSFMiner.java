package Reika.Satisforestry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;

import Reika.DragonAPI.Base.CoreContainer;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.Satisforestry.Blocks.TileNodeHarvester;


public class ContainerSFMiner extends CoreContainer {

	private final TileNodeHarvester tile;

	public ContainerSFMiner(EntityPlayer player, TileNodeHarvester te) {
		super(player, te, te.getOutput());
		tile = te;

		this.addSlot(0, 80, 55);

		this.addPlayerInventoryWithOffset(player, 0, 32);
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

}
