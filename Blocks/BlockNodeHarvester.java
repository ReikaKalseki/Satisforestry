package Reika.Satisforestry.Blocks;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import Reika.DragonAPI.Base.BlockTEBase;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.TileNodeHarvester.TileNodeHarvesterRF;

public class BlockNodeHarvester extends BlockTEBase {

	public BlockNodeHarvester(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setResistance(30);
		this.setLightOpacity(0);
	}

	@Override
	public boolean hasTileEntity(int meta) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int meta) {
		switch(meta) {
			case 0:
				return new TileNodeHarvesterRF();
			default:
				return null;
		}
	}



}
