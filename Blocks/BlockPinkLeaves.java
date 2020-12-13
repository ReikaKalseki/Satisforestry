package Reika.Satisforestry.Blocks;

import net.minecraft.block.BlockOldLeaf;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import Reika.Satisforestry.Satisforestry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPinkLeaves extends BlockOldLeaf {

	public static enum LeafTypes {
		TREE,
		GIANTTREE,
		BUSH1,
		BUSH2,
		BUSH3;

		private static final LeafTypes[] list = values();
	}

	public BlockPinkLeaves() {
		super();
		this.setLightOpacity(0);
	}

	@Override
	public int getRenderColor(int mta) {
		return Satisforestry.pinkforest.getBiomeFoliageColor(0, 64, 0);
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		LeafTypes l = LeafTypes.list[world.getBlockMetadata(x, y, z)%8];
		if (l == LeafTypes.GIANTTREE) {
			y -= 50; //was 18 then 24
		}
		return Satisforestry.pinkforest.getBiomeFoliageColor(x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ico) {
		blockIcon = ico.registerIcon("Satisforestry:pink-tree-leaf");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int s, int meta) {
		return blockIcon;
	}

}
