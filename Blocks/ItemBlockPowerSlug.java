package Reika.Satisforestry.Blocks;

import net.minecraft.block.Block;

import Reika.DragonAPI.Instantiable.MetadataItemBlock;


public class ItemBlockPowerSlug extends MetadataItemBlock {

	public ItemBlockPowerSlug(Block b) {
		super(b);
	}

	@Override
	public int getMetadata(int meta) {
		return meta;//%3;
	}

}
