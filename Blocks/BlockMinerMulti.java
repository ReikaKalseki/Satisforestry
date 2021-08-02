package Reika.Satisforestry.Blocks;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Base.BlockMultiBlock;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.StructuredBlockArray;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.Satisforestry.MinerStructure;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Registry.SFBlocks;

public class BlockMinerMulti extends BlockMultiBlock<ForgeDirection> {

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
	public ForgeDirection checkForFullMultiBlock(World world, int x, int y, int z, ForgeDirection placeDir) {
		TileEntity te = this.getTileEntityForPosition(world, x, y, z);
		//ReikaJavaLibrary.pConsole(te);
		if (te instanceof TileNodeHarvester) {
			return MinerStructure.getStructureDirection(world, te.xCoord, te.yCoord, te.zCoord);
		}
		return null;
	}

	@Override
	public void breakMultiBlock(World world, int x, int y, int z) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.extraSpread = true;
		blocks.recursiveMultiAddWithBounds(world, x, y, z, x-SEARCH, y-SEARCH*2, z-SEARCH, x+SEARCH, y+SEARCH*2, z+SEARCH, this, SFBlocks.HARVESTER.getBlockInstance());
		for (int i = 0; i < 6; i++) {
			ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
			blocks.recursiveMultiAddWithBounds(world, x+dir.offsetX, y+dir.offsetY, z+dir.offsetZ, x-SEARCH, y-SEARCH*2, z-SEARCH, x+SEARCH, y+SEARCH*2, z+SEARCH, this, SFBlocks.HARVESTER.getBlockInstance());
		}
		for (int i = 0; i < blocks.getSize(); i++) {
			Coordinate c = blocks.getNthBlock(i);
			int meta = c.getBlockMetadata(world);
			if (meta >= 8) {
				world.setBlockMetadataWithNotify(c.xCoord, c.yCoord, c.zCoord, meta-8, 3);
			}
		}
		TileEntity te = this.getTileEntityForPosition(world, x, y, z);
		if (te instanceof TileNodeHarvester) {
			((TileNodeHarvester)te).setHasStructure(null);
		}
	}

	@Override
	protected void onCreateFullMultiBlock(World world, int x, int y, int z, ForgeDirection dir) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.extraSpread = true;
		blocks.recursiveMultiAddWithBounds(world, x, y, z, x-SEARCH, y-SEARCH*2, z-SEARCH, x+SEARCH, y+SEARCH*2, z+SEARCH, this, SFBlocks.HARVESTER.getBlockInstance());
		for (int i = 0; i < blocks.getSize(); i++) {
			Coordinate c = blocks.getNthBlock(i);
			int meta = c.getBlockMetadata(world);
			if (meta < 8) {
				world.setBlockMetadataWithNotify(c.xCoord, c.yCoord, c.zCoord, meta+8, 3);
			}
		}
		TileEntity te = this.getTileEntityForPosition(world, x, y, z);
		if (te instanceof TileNodeHarvester) {
			((TileNodeHarvester)te).setHasStructure(dir);
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
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.extraSpread = true;
		blocks.recursiveMultiAddWithBounds(world, x, y, z, x-SEARCH, y-SEARCH*2, z-SEARCH, x+SEARCH, y+SEARCH*2, z+SEARCH, this, SFBlocks.HARVESTER.getBlockInstance());
		for (int i = 0; i < blocks.getSize(); i++) {
			Coordinate c = blocks.getNthBlock(i);
			Block b = c.getBlock(world);
			if (b == SFBlocks.HARVESTER.getBlockInstance())
				return c.getTileEntity(world);
		}
		return null;
	}

}
