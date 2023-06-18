package Reika.Satisforestry.Blocks;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import Reika.DragonAPI.Instantiable.Data.BlockStruct.FilledBlockArray.BlockMatchFailCallback;
import Reika.Satisforestry.Miner.FrackerStructure;
import Reika.Satisforestry.Miner.TileFrackingPressurizer;
import Reika.Satisforestry.Registry.SFBlocks;

public class BlockFrackerMulti extends BlockSFMultiBase<Boolean> {

	public static enum FrackerBlocks {
		ORANGE,
		DARK,
		SILVER,
		GRAY,
		//CORE,
		TUBE,
		FLUIDIN,
		HUB,
		POWER,
		//THUMPER,
		;

		public static final FrackerBlocks[] list = values();
	}

	public BlockFrackerMulti(Material mat) {
		super(mat);
	}

	@Override
	public boolean hasTileEntity(int meta) {
		if (meta < 8)
			return false;
		FrackerBlocks mb = FrackerBlocks.list[meta&7];
		return mb == FrackerBlocks.HUB || mb == FrackerBlocks.POWER || mb == FrackerBlocks.FLUIDIN;
	}

	@Override
	protected boolean shouldTurnToSlab(int meta) {
		return FrackerBlocks.list[meta&7] == FrackerBlocks.DARK;
	}

	@Override
	public TileEntity createTileEntity(World world, int meta) {
		if (meta < 8)
			return null;
		switch(FrackerBlocks.list[meta&7]) {
			case HUB:
				return new TileShaftConnection();
			case POWER:
				return new TilePowerConnection();
			case FLUIDIN:
				return new TileFrackerFluidPort();
			default:
				return null;
		}
	}

	@Override
	protected String getIconFolderName() {
		return "fracker";
	}

	@Override
	protected String getLocaleKeyName() {
		return "sffracker";
	}

	@Override
	public Boolean checkForFullMultiBlock(World world, int x, int y, int z, ForgeDirection placeDir, BlockMatchFailCallback call) {
		TileEntity te = this.getTileEntityForPosition(world, x, y, z);
		return te instanceof TileFrackingPressurizer && FrackerStructure.getFrackerStructure(world, te.xCoord, te.yCoord, te.zCoord).matchInWorld(call);
	}

	@Override
	public int getNumberVariants() {
		return FrackerBlocks.list.length;
	}

	@Override
	protected SFBlocks getTileBlockType() {
		return SFBlocks.FRACKER;
	}

	@Override
	protected int getBlockSearchXZ() {
		return 12;
	}

	@Override
	protected int getBlockSearchY() {
		return 16;
	}

	public static class TileFrackerFluidPort extends TileMinerConnection<TileFrackingPressurizer> implements IFluidHandler {

		@Override
		public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
			TileFrackingPressurizer te = this.getRoot();
			if (te == null)
				return 0;
			return te.addFrackingFluid(resource, doFill);
		}

		@Override
		public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
			return null;
		}

		@Override
		public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
			return null;
		}

		@Override
		public boolean canFill(ForgeDirection from, Fluid fluid) {
			TileFrackingPressurizer te = this.getRoot();
			return te != null && te.canAccept(fluid);
		}

		@Override
		public boolean canDrain(ForgeDirection from, Fluid fluid) {
			return false;
		}

		@Override
		public FluidTankInfo[] getTankInfo(ForgeDirection from) {
			return new FluidTankInfo[0];
		}

	}
}
