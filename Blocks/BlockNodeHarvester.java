package Reika.Satisforestry.Blocks;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import Reika.DragonAPI.Base.BlockTEBase;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.TileNodeHarvester.TileNodeHarvesterEU;
import Reika.Satisforestry.Blocks.TileNodeHarvester.TileNodeHarvesterRC;
import Reika.Satisforestry.Blocks.TileNodeHarvester.TileNodeHarvesterRF;

public class BlockNodeHarvester extends BlockTEBase {

	public BlockNodeHarvester(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setResistance(30);
		this.setLightOpacity(0);
	}

	@Override
	public void getSubBlocks(Item it, CreativeTabs tab, List li) {
		for (int i = 0; i < 3; i++) {
			li.add(new ItemStack(it, 1, i));
		}
	}

	@Override
	public boolean hasTileEntity(int meta) {
		return meta <= 2;
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

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}



}
