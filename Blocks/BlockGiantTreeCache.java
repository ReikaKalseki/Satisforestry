package Reika.Satisforestry.Blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.Generator.GiantPinkTreeGenerator;


public class BlockGiantTreeCache extends BlockContainer {

	public BlockGiantTreeCache(Material m) {
		super(m);
		this.setBlockUnbreakable();
		this.setResistance(60000);
	}

	@Override
	public int getRenderType() {
		return -1;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileGiantTreeCache();
	}

	@Override
	public boolean isLeaves(IBlockAccess world, int x, int y, int z) {
		return true;
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, int x, int y, int z) {
		return true;
	}

	public static class TileGiantTreeCache extends TileEntity {

		private NBTTagCompound treeData;
		public static boolean isGenerating = false;

		private int ticksExisted = 0;

		@Override
		public void updateEntity() {
			if (!worldObj.isRemote && this.isAreaLoaded()) {
				ticksExisted++;
				if (ticksExisted >= 5) {
					GiantPinkTreeGenerator gen = GiantPinkTreeGenerator.readNBT(treeData);
					isGenerating = true;
					boolean flag = gen.generate(worldObj, null, xCoord, yCoord, zCoord);
					Satisforestry.logger.log("Delegated giant tree spawn: "+flag+" @ "+new Coordinate(this)+" @ "+worldObj.getTotalWorldTime());
					isGenerating = false;
					if (worldObj.getBlock(xCoord, yCoord-1, zCoord) == Blocks.dirt)
						worldObj.setBlock(xCoord, yCoord-1, zCoord, Blocks.grass);
					worldObj.setBlockToAir(xCoord, yCoord, zCoord);
				}
			}
		}

		private boolean isAreaLoaded() {
			int x = xCoord >> 4;
			int z = zCoord >> 4;
			for (int i = -2; i <= 2; i++) {
				for (int k = -2; k <= 2; k++) {
					if (!ReikaWorldHelper.isChunkPastCompletelyFinishedGenerating(worldObj, x, z))
						return false;
				}
			}
			return true;
		}

		public void setTree(GiantPinkTreeGenerator gen) {
			treeData = gen.getNBT();
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			treeData = NBT.getCompoundTag("tree");
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			if (treeData != null)
				NBT.setTag("tree", treeData);
		}

	}
}
