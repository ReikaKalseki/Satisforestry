package Reika.Satisforestry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;

import Reika.DragonAPI.Base.CoreContainer;
import Reika.Satisforestry.Blocks.TileNodeHarvester;


public class ContainerSFMiner extends CoreContainer {

	private final TileNodeHarvester tile;

	public ContainerSFMiner(EntityPlayer player, TileNodeHarvester te) {
		super(player, te, te.getOutput());
		tile = te;

		this.addSlot(0, 80, 35);

		this.addPlayerInventory(player);
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

}
