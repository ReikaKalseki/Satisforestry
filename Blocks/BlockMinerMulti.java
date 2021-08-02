package Reika.Satisforestry.Blocks;

import java.util.ArrayList;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Base.BlockMultiBlock;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.StructuredBlockArray;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.Satisforestry.Satisforestry;

public class BlockMinerMulti extends BlockMultiBlock {

	private static final int SEARCH = 8;

	public BlockMinerMulti(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setResistance(30);
		this.setLightOpacity(0);
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
	protected final String getFullIconPath(int i) {
		return "satisforestry:miner/"+i;
	}

	@Override
	public final ArrayList<String> getMessages(World world, int x, int y, int z, int side) {
		TileEntity te = this.getTileEntityForPosition(world, x, y, z);
		return te instanceof TileNodeHarvester ? ((TileNodeHarvester)te).getMessages(world, x, y, z, side) : new ArrayList();
	}

	public final String getName(int meta) {
		return StatCollector.translateToLocal("multiblock.sfminer."+(meta&7));
	}

	@Override
	public int getNumberTextures() {
		return 9;
	}

	@Override
	public boolean checkForFullMultiBlock(World world, int x, int y, int z, ForgeDirection dir) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void breakMultiBlock(World world, int x, int y, int z) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z, this, x-SEARCH, y-SEARCH, z-SEARCH, x+SEARCH, y+SEARCH, z+SEARCH, 1);
		blocks.recursiveAddWithBoundsRanged(world, x+1, y, z, this, x-SEARCH, y-SEARCH, z-SEARCH, x+SEARCH, y+SEARCH, z+SEARCH, 1);
		blocks.recursiveAddWithBoundsRanged(world, x-1, y, z, this, x-SEARCH, y-SEARCH, z-SEARCH, x+SEARCH, y+SEARCH, z+SEARCH, 1);
		blocks.recursiveAddWithBoundsRanged(world, x, y+1, z, this, x-SEARCH, y-SEARCH, z-SEARCH, x+SEARCH, y+SEARCH, z+SEARCH, 1);
		blocks.recursiveAddWithBoundsRanged(world, x, y-1, z, this, x-SEARCH, y-SEARCH, z-SEARCH, x+SEARCH, y+SEARCH, z+SEARCH, 1);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z+1, this, x-SEARCH, y-SEARCH, z-SEARCH, x+SEARCH, y+SEARCH, z+SEARCH, 1);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z-1, this, x-SEARCH, y-SEARCH, z-SEARCH, x+SEARCH, y+SEARCH, z+SEARCH, 1);
		for (int i = 0; i < blocks.getSize(); i++) {
			Coordinate c = blocks.getNthBlock(i);
			int meta = c.getBlockMetadata(world);
			if (meta >= 8) {
				world.setBlockMetadataWithNotify(c.xCoord, c.yCoord, c.zCoord, meta-8, 3);
			}
		}
		TileEntity te = this.getTileEntityForPosition(world, x, y, z);
		if (te instanceof TileNodeHarvester) {
			((TileNodeHarvester)te).setHasStructure(false);
		}
	}

	@Override
	protected void onCreateFullMultiBlock(World world, int x, int y, int z) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.recursiveAddWithBoundsRanged(world, x, y, z, this, x-SEARCH, y-SEARCH, z-SEARCH, x+SEARCH, y+SEARCH, z+SEARCH, 1);
		for (int i = 0; i < blocks.getSize(); i++) {
			Coordinate c = blocks.getNthBlock(i);
			int meta = c.getBlockMetadata(world);
			if (meta < 8) {
				world.setBlockMetadataWithNotify(c.xCoord, c.yCoord, c.zCoord, meta+8, 3);
			}
		}
		TileEntity te = this.getTileEntityForPosition(world, x, y, z);
		if (te instanceof TileNodeHarvester) {
			((TileNodeHarvester)te).setHasStructure(true);
		}
	}

	@Override
	public int getNumberVariants() {
		return 8;
	}

	@Override
	public int getTextureIndex(IBlockAccess world, int x, int y, int z, int side, int meta) {
		return meta >= 8 ? 8 : meta;
	}

	@Override
	public int getItemTextureIndex(int meta, int side) {
		return meta&7;
	}

	@Override
	public boolean canTriggerMultiBlockCheck(World world, int x, int y, int z, int meta) {
		return true;
	}

	@Override
	protected TileEntity getTileEntityForPosition(World world, int x, int y, int z) {
		// TODO Auto-generated method stub
		return null;
	}

}
