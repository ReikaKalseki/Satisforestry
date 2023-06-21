package Reika.Satisforestry.Blocks;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import Reika.Satisforestry.Miner.TileNodeHarvester.TileNodeHarvesterEU;
import Reika.Satisforestry.Miner.TileNodeHarvester.TileNodeHarvesterRC;
import Reika.Satisforestry.Miner.TileNodeHarvester.TileNodeHarvesterRF;

public class BlockNodeHarvester extends BlockSFHarvester {

	public BlockNodeHarvester(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createTileEntity(World world, int meta) {
		switch(meta) {
			case 0:
				return new TileNodeHarvesterRF();
			case 1:
				return new TileNodeHarvesterEU();
			case 2:
				return new TileNodeHarvesterRC();
			default:
				return null;
		}
	}


}
