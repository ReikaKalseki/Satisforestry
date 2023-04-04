package Reika.Satisforestry.Blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import Reika.DragonAPI.Base.BlockTEBase;
import Reika.DragonAPI.Base.TileEntityBase;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.FilledBlockArray;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockFrackerMulti.FrackerBlocks;
import Reika.Satisforestry.Blocks.BlockFrackingAux.TileFrackingAux;
import Reika.Satisforestry.Miner.TileFrackingPressurizer.TileFrackingPressurizerEU;
import Reika.Satisforestry.Miner.TileFrackingPressurizer.TileFrackingPressurizerRC;
import Reika.Satisforestry.Miner.TileFrackingPressurizer.TileFrackingPressurizerRF;
import Reika.Satisforestry.Registry.SFBlocks;

public class BlockFrackingPressurizer extends BlockTEBase {

	public BlockFrackingPressurizer(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setResistance(30);
		this.setLightOpacity(0);
	}

	@Override
	public void getSubBlocks(Item it, CreativeTabs tab, List li) {
		for (int i = 0; i < 4; i++) {
			li.add(new ItemStack(it, 1, i));
		}
	}

	@Override
	public boolean hasTileEntity(int meta) {
		return meta <= 3;
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
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
			case 3:
				return new TileFrackingExtractor();
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



	public static class TileFrackingExtractor extends TileEntityBase implements IFluidHandler {

		private boolean isAccessingStructure;

		private boolean structure;

		@Override
		public void updateEntity(World world, int x, int y, int z, int meta) {
			if (structure && !world.isRemote) {
				TileEntity te = worldObj.getTileEntity(x, y+1, z);
				if (te instanceof IFluidHandler) {
					IFluidHandler ifl = (IFluidHandler)te;
					TileFrackingAux node = this.getNode();
					if (node != null) {
						FluidStack max = node.drain(1000, false);
						if (max != null && max.amount > 0) {
							int fit = ifl.canFill(ForgeDirection.DOWN, max.getFluid()) ? ifl.fill(ForgeDirection.DOWN, max, false) : 0;
							if (fit > 0) {
								FluidStack get = node.drain(fit, true);
								ifl.fill(ForgeDirection.DOWN, get, true);
							}
						}
					}
				}
			}
		}

		@Override
		protected void onAdjacentBlockUpdate() {
			if (isAccessingStructure)
				return;
			isAccessingStructure = true;
			boolean flag = this.getStructure().matchInWorld();
			if (flag != structure) {
				structure = flag;
				this.updateStructureBlocks(flag);
			}
			isAccessingStructure = false;
		}

		private void updateStructureBlocks(boolean flag) {
			for (Coordinate c : this.getStructure().keySet()) {
				int meta = c.getBlockMetadata(worldObj);
				int put = flag ? meta+8 : meta%8;
				c.setBlockMetadata(worldObj, put);
			}
		}

		public FilledBlockArray getStructure() {
			return getStructure(worldObj, xCoord, yCoord, zCoord);
		}

		public static FilledBlockArray getStructure(World world, int x, int y, int z) {
			FilledBlockArray arr = new FilledBlockArray(world);
			arr.setBlock(x, y-1, z, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.TUBE.ordinal());

			arr.setBlock(x-1, y-1, z, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.DARK.ordinal());
			arr.setBlock(x+1, y-1, z, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.DARK.ordinal());
			arr.setBlock(x, y-1, z-1, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.DARK.ordinal());
			arr.setBlock(x, y-1, z+1, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.DARK.ordinal());

			arr.setBlock(x-1, y, z, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.ORANGE.ordinal());
			arr.setBlock(x+1, y, z, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.ORANGE.ordinal());
			arr.setBlock(x, y, z-1, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.ORANGE.ordinal());
			arr.setBlock(x, y, z+1, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.ORANGE.ordinal());

			for (Coordinate c : arr.keySet()) {
				arr.addBlock(c.xCoord, c.yCoord, c.zCoord, arr.getBlockAt(c.xCoord, c.yCoord, c.zCoord), arr.getMetaAt(c.xCoord, c.yCoord, c.zCoord)+8);
			}

			//arr.setBlock(x, y+1, z, SFBlocks.MINERMULTI.getBlockInstance(), MinerBlocks.SILVER.ordinal());
			return arr;
		}

		@Override
		protected void writeSyncTag(NBTTagCompound NBT) {
			super.writeSyncTag(NBT);

			NBT.setBoolean("struct", structure);
		}

		@Override
		protected void readSyncTag(NBTTagCompound NBT) {
			super.readSyncTag(NBT);

			structure = NBT.getBoolean("struct");
		}

		public boolean hasStructure() {
			return structure;
		}

		private TileFrackingAux getNode() {
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-2, zCoord);
			return te instanceof TileFrackingAux ? (TileFrackingAux)te : null;
		}

		@Override
		public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
			return 0;
		}

		@Override
		public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {/*
			if (from != ForgeDirection.UP || !structure)
				return null;
			TileFrackingAux te = this.getNode();
			return te != null ? te.drain(resource.amount, doDrain) : null;*/
			return null;
		}

		@Override
		public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {/*
			if (from != ForgeDirection.UP || !structure)
				return null;
			TileFrackingAux te = this.getNode();
			return te != null ? te.drain(maxDrain, doDrain) : null;*/
			return null;
		}

		@Override
		public boolean canFill(ForgeDirection from, Fluid fluid) {
			return false;
		}

		@Override
		public boolean canDrain(ForgeDirection from, Fluid fluid) {
			return false;//from == ForgeDirection.UP && structure;
		}

		@Override
		public FluidTankInfo[] getTankInfo(ForgeDirection from) {
			return new FluidTankInfo[0];
		}

		@Override
		public Block getTileEntityBlockID() {
			return SFBlocks.FRACKER.getBlockInstance();
		}

		@Override
		protected void animateWithTick(World world, int x, int y, int z) {

		}

		@Override
		public int getRedstoneOverride() {
			return 0;
		}

		@Override
		protected String getTEName() {
			return "Resource Well Extractor";
		}

		@Override
		public boolean shouldRenderInPass(int pass) {
			return pass == 0;
		}

	}



}
