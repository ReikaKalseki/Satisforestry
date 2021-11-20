package Reika.Satisforestry.Blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import Reika.DragonAPI.Instantiable.MetadataItemBlock;
import Reika.Satisforestry.Miner.TileNodeHarvester;


public class ItemBlockNodeHarvester extends MetadataItemBlock {

	public ItemBlockNodeHarvester(Block b) {
		super(b);
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer ep, List li, boolean vb) {
		TileNodeHarvester te = (TileNodeHarvester)field_150939_a.createTileEntity(ep.worldObj, is.getItemDamage());
		li.add("Requires "+EnumChatFormatting.WHITE+te.getPowerType()+EnumChatFormatting.GRAY+" power.");
		if (GuiScreen.isCtrlKeyDown()) {
			li.add(String.format("Efficiency: %s%.1f%s", EnumChatFormatting.WHITE.toString(), te.getSpeedFactor()*100, "%"));
			for (int i = 0; i <= 3; i++) {
				te.overclockDisplay = i;
				li.add(String.format("Power Cost Per Cycle (%.0f%s speed): %s%s", te.getOverclockingLevel(true)*100, "%", EnumChatFormatting.WHITE.toString(), te.getOperationPowerCost(true)));
			}
		}
		else {
			li.add(EnumChatFormatting.GREEN+"Hold LCTRL to see speed/power data.");
		}
	}

}
