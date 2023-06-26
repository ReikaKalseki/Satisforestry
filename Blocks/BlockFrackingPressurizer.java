package Reika.Satisforestry.Blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import Reika.DragonAPI.Base.TileEntityBase;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.FilledBlockArray;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Interfaces.TileEntity.BreakAction;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.ReikaDirectionHelper;
import Reika.RotaryCraft.API.Interfaces.RCPipe;
import Reika.Satisforestry.Blocks.BlockFrackerMulti.FrackerBlocks;
import Reika.Satisforestry.Blocks.BlockFrackingAux.TileFrackingAux;
import Reika.Satisforestry.Miner.TileFrackingPressurizer.TileFrackingPressurizerEU;
import Reika.Satisforestry.Miner.TileFrackingPressurizer.TileFrackingPressurizerRC;
import Reika.Satisforestry.Miner.TileFrackingPressurizer.TileFrackingPressurizerRF;
import Reika.Satisforestry.Registry.SFBlocks;

public class BlockFrackingPressurizer extends BlockSFHarvester {

	public BlockFrackingPressurizer(Material mat) {
		super(mat);
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
	protected int getSurplusVariants() {
		return 1;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase e, ItemStack is) {
		TileFrackingExtractor te = (TileFrackingExtractor)world.getTileEntity(x, y, z);
		te.facing = ReikaDirectionHelper.getFromLookDirection(e, false).getOpposite();
	}

	public static class TileFrackingExtractor extends TileEntityBase implements IFluidHandler, BreakAction {

		private boolean isAccessingStructure;

		private boolean structure;

		private ForgeDirection facing;

		@Override
		protected void onFirstTick(World world, int x, int y, int z) {
			this.onAdjacentBlockUpdate();
		}

		public void breakBlock() {
			this.updateStructureBlocks(false);
		}

		@Override
		public void updateEntity(World world, int x, int y, int z, int meta) {
			if (!world.isRemote) {
				if (structure && facing != null) {
					TileEntity te = this.getAdjacentTileEntity(facing);
					TileFrackingAux node = this.getNode();
					if (node != null) {
						FluidStack max = node.drain(1000, false);
						if (max != null && max.amount > 0) {
							if (te instanceof IFluidHandler) {
								IFluidHandler ifl = (IFluidHandler)te;
								ForgeDirection dir = facing.getOpposite();
								int fit = ifl.canFill(dir, max.getFluid()) ? ifl.fill(dir, max, false) : 0;
								if (fit > 0) {
									FluidStack get = node.drain(fit, true);
									ifl.fill(dir, get, true);
								}
							}
							else if (te instanceof RCPipe) {
								RCPipe rc = (RCPipe)te;
								if (rc.addFluid(max.getFluid(), max.amount))
									node.drain(max, true);
							}
						}
					}
				}
				else if (this.getTicksExisted()%20 == 0) {
					this.onAdjacentBlockUpdate();
				}
			}
		}

		@Override
		protected void onAdjacentBlockUpdate() {
			if (isAccessingStructure || worldObj.isRemote)
				return;
			isAccessingStructure = true;
			boolean flag = this.getStructure().matchInWorld();
			if (flag != structure) {
				structure = flag;
				this.updateStructureBlocks(flag);
				this.syncAllData(false);
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
			arr.setBlock(x, y-2, z, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.TUBE.ordinal());

			arr.setBlock(x-1, y-2, z, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.DARK.ordinal());
			arr.setBlock(x+1, y-2, z, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.DARK.ordinal());
			arr.setBlock(x, y-2, z-1, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.DARK.ordinal());
			arr.setBlock(x, y-2, z+1, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.DARK.ordinal());

			arr.setBlock(x-1, y-1, z, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.ORANGE.ordinal());
			arr.setBlock(x+1, y-1, z, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.ORANGE.ordinal());
			arr.setBlock(x, y-1, z-1, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.ORANGE.ordinal());
			arr.setBlock(x, y-1, z+1, SFBlocks.FRACKERMULTI.getBlockInstance(), FrackerBlocks.ORANGE.ordinal());

			for (Coordinate c : arr.keySet()) {
				arr.addBlock(c.xCoord, c.yCoord, c.zCoord, arr.getBlockAt(c.xCoord, c.yCoord, c.zCoord), arr.getMetaAt(c.xCoord, c.yCoord, c.zCoord)+8);
			}

			return arr;
		}

		@Override
		protected void writeSyncTag(NBTTagCompound NBT) {
			super.writeSyncTag(NBT);

			NBT.setBoolean("struct", structure);
			NBT.setInteger("dir", facing != null ? facing.ordinal() : -1);
		}

		@Override
		protected void readSyncTag(NBTTagCompound NBT) {
			super.readSyncTag(NBT);

			structure = NBT.getBoolean("struct");
			int dir = NBT.getInteger("dir");
			facing = dir < 0 ? null : dirs[dir];
		}

		public boolean hasStructure() {
			return structure;
		}

		private TileFrackingAux getNode() {
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord-3, zCoord);
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

		@Override
		public AxisAlignedBB getRenderBoundingBox() {
			AxisAlignedBB box = ReikaAABBHelper.getBlockAABB(this);
			if (this.hasStructure()) {
				box = box.addCoord(xCoord, yCoord-2, zCoord);
				box = box.expand(2, 1, 2);
			}
			return box;
		}

		public ForgeDirection getFacing() {
			return facing != null ? facing : ForgeDirection.EAST;
		}

	}



}
