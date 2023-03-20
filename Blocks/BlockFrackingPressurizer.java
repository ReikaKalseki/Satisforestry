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
import Reika.Satisforestry.Miner.TileFrackingPressurizer.TileFrackingPressurizerEU;
import Reika.Satisforestry.Miner.TileFrackingPressurizer.TileFrackingPressurizerRC;
import Reika.Satisforestry.Miner.TileFrackingPressurizer.TileFrackingPressurizerRF;

public class BlockFrackingPressurizer extends BlockTEBase {

	public BlockFrackingPressurizer(Material mat) {
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
				return new TileFrackingPressurizerRF();
			case 1:
				return new TileFrackingPressurizerEU();
			case 2:
				return new TileFrackingPressurizerRC();
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

	@Override
	public int getRenderType() {
		return -1;
	}



}
