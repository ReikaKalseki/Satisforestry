package Reika.Satisforestry.Miner;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.FilledBlockArray;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Interfaces.BlockCheck;
import Reika.DragonAPI.ModInteract.ItemHandlers.BCPipeHandler;
import Reika.DragonAPI.ModInteract.ItemHandlers.ThermalDuctHandler;
import Reika.RotaryCraft.Base.BlockRotaryCraftMachine;
import Reika.Satisforestry.Blocks.BlockFrackerMulti.FrackerBlocks;
import Reika.Satisforestry.Registry.SFBlocks;

public class FrackerStructure {

	private static final BlockCheck AREA_CHECK = new BlockCheck() {

		@Override
		public boolean matchInWorld(World world, int x, int y, int z) {
			Block b = world.getBlock(x, y, z);
			if (b.isAir(world, x, y, z))
				return true;
			if (ModList.BCTRANSPORT.isLoaded() && b == BCPipeHandler.getInstance().pipeID)
				return true;
			if (ModList.THERMALDYNAMICS.isLoaded() && ThermalDuctHandler.getInstance().isDuct(b))
				return true;
			if (ModList.ROTARYCRAFT.isLoaded() && this.isValidRCMachine(b))
				return true;
			if (world.getBlock(x, y-1, z) == SFBlocks.FRACKERMULTI.getBlockInstance() && world.getBlockMetadata(x, y-1, z) == FrackerBlocks.POWER.ordinal())
				return true;
			return false;
		}

		@ModDependent(ModList.ROTARYCRAFT)
		private boolean isValidRCMachine(Block b) {
			return b instanceof BlockRotaryCraftMachine;
		}

		@Override
		public boolean match(Block b, int meta) {
			return b.getMaterial() == Material.air;
		}

		@Override
		public boolean match(BlockCheck bc) {
			return bc == this;
		}

		@Override
		public void place(World world, int x, int y, int z, int flags) {
			world.setBlockToAir(x, y, z);
		}

		@Override
		public ItemStack asItemStack() {
			return null;
		}

		@Override
		public ItemStack getDisplay() {
			return null;
		}

		@Override
		public BlockKey asBlockKey() {
			return new BlockKey(Blocks.air);
		}

	};

	private static final HashSet<Coordinate> footprint = new HashSet();

	public static boolean isUnderFrackingFootprint(World world, int dx, int dz) {
		if (footprint.isEmpty()) {
			for (Coordinate c : getFrackerStructure(world, 0, 0, 0).keySet()) {
				footprint.add(c.to2D());
			}
		}
		return footprint.contains(new Coordinate(dx, 0, dz));
	}

	private static void setBlock(FilledBlockArray array, int x0, int y0, int z0, int dx, int dy, int dz, Block b) {
		setBlock(array, x0, y0, z0, dx, dy, dz, b, -1);
	}

	private static void setBlock(FilledBlockArray array, int x0, int y0, int z0, int dx, int dy, int dz, Block b, int meta) {/*
		if (b == Blocks.air) {
			if (dx-x0 > 3 && dy-y0 > 5)
				return;
		}*/
		if (b == Blocks.air) {
			if (dy == y0 || (dy == y0+1 && Math.abs(dx-x0) > 1 && Math.abs(dz-z0) > 1) || (Math.abs(x0-dx) < 2 && Math.abs(z0-dz) < 2 && dy <= y0+9))
				array.setEmpty(dx, dy, dz, false, false);
			else
				array.setBlock(dx, dy, dz, AREA_CHECK);
		}
		else {
			array.setBlock(dx, dy, dz, b, meta);
		}
	}

	public static void toggleRSLamps(TileFrackingPressurizer te, boolean set) {
		Block b = set ? Blocks.air : Blocks.redstone_lamp;
		for (int i = 9; i <= 11; i++)
			te.worldObj.setBlock(te.xCoord, te.yCoord+i, te.zCoord, b);
	}

	public static FilledBlockArray getFrackerStructure(World world, int x, int y, int z) {
		FilledBlockArray array = new FilledBlockArray(world);
		Block b = SFBlocks.FRACKERMULTI.getBlockInstance();

		int i = x-6;
		int j = y;
		int k = z-6;

		//array.setBlock(i + 6, j + 0, k + 6, SFBlocks.FRACKER.getBlockInstance(), -1);

		for (int dx = x-6; dx <= x+6; dx++) {
			for (int dz = z-6; dz <= z+6; dz++) {
				for (int dy = y; dy <= y+16; dy++) {
					array.setBlock(i + 6, j + 10, k + 6, Blocks.air);
				}
			}
		}

		array.setBlock(i + 6, j + 9, k + 6, Blocks.redstone_lamp);
		array.setBlock(i + 6, j + 10, k + 6, Blocks.redstone_lamp);
		array.setBlock(i + 6, j + 11, k + 6, Blocks.redstone_lamp);

		array.setBlock(i + 2, j + 2, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 3, j + 2, k + 6, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 6, j + 2, k + 2, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 2, k + 3, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 6, j + 2, k + 9, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 6, j + 2, k + 10, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 9, j + 2, k + 6, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 10, j + 2, k + 6, b, FrackerBlocks.GRAY.ordinal());

		array.setBlock(i + 0, j + 0, k + 5, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 0, j + 0, k + 6, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 0, j + 0, k + 7, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 1, j + 0, k + 5, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 1, j + 0, k + 6, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 1, j + 0, k + 7, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 1, j + 1, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 1, j + 5, k + 6, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 2, j + 0, k + 5, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 2, j + 0, k + 7, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 2, j + 1, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 2, j + 4, k + 6, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 2, j + 5, k + 6, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 3, j + 0, k + 4, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 3, j + 0, k + 8, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 3, j + 3, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 3, j + 4, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 4, j + 0, k + 3, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 4, j + 0, k + 4, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 4, j + 0, k + 5, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 4, j + 0, k + 7, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 4, j + 0, k + 8, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 4, j + 0, k + 9, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 4, j + 1, k + 5, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 4, j + 1, k + 6, b, FrackerBlocks.FLUIDIN.ordinal());
		array.setBlock(i + 4, j + 1, k + 7, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 4, j + 2, k + 5, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 4, j + 2, k + 6, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 4, j + 2, k + 7, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 4, j + 3, k + 5, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 4, j + 3, k + 6, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 4, j + 3, k + 7, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 4, j + 4, k + 4, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 4, j + 4, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 4, j + 4, k + 6, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 4, j + 4, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 4, j + 4, k + 8, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 4, j + 5, k + 4, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 4, j + 5, k + 5, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 4, j + 5, k + 6, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 4, j + 5, k + 7, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 4, j + 5, k + 8, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 4, j + 6, k + 4, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 4, j + 6, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 4, j + 6, k + 6, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 4, j + 6, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 4, j + 6, k + 8, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 4, j + 7, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 4, j + 7, k + 6, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 4, j + 7, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 4, j + 8, k + 5, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 4, j + 8, k + 6, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 4, j + 8, k + 7, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 5, j + 0, k + 0, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 5, j + 0, k + 1, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 5, j + 0, k + 2, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 5, j + 0, k + 4, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 5, j + 0, k + 5, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 5, j + 0, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 5, j + 0, k + 7, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 5, j + 0, k + 8, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 5, j + 0, k + 10, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 5, j + 0, k + 11, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 5, j + 0, k + 12, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 5, j + 1, k + 4, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 5, j + 1, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 1, k + 6, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 1, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 1, k + 8, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 5, j + 2, k + 4, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 5, j + 2, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 2, k + 6, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 2, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 2, k + 8, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 5, j + 3, k + 4, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 5, j + 3, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 3, k + 6, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 3, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 3, k + 8, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 5, j + 4, k + 4, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 4, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 4, k + 6, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 4, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 4, k + 8, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 5, k + 4, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 5, j + 5, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 5, k + 6, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 5, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 5, j + 5, k + 8, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 5, j + 6, k + 4, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 6, k + 5, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 5, j + 6, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 5, j + 6, k + 7, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 5, j + 6, k + 8, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 7, k + 4, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 7, k + 5, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 5, j + 7, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 5, j + 7, k + 7, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 5, j + 7, k + 8, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 8, k + 4, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 5, j + 8, k + 5, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 5, j + 8, k + 6, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 5, j + 8, k + 7, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 5, j + 8, k + 8, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 5, j + 9, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 9, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 10, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 10, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 11, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 11, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 12, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 12, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 13, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 13, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 14, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 5, j + 14, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 6, j + 0, k + 0, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 6, j + 0, k + 1, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 6, j + 0, k + 5, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 0, k + 7, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 0, k + 11, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 6, j + 0, k + 12, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 6, j + 1, k + 1, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 1, k + 2, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 1, k + 4, b, FrackerBlocks.FLUIDIN.ordinal());
		array.setBlock(i + 6, j + 1, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 6, j + 1, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 6, j + 1, k + 8, b, FrackerBlocks.FLUIDIN.ordinal());
		array.setBlock(i + 6, j + 1, k + 10, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 1, k + 11, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 2, k + 4, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 6, j + 2, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 6, j + 2, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 6, j + 2, k + 8, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 6, j + 3, k + 3, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 3, k + 4, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 6, j + 3, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 6, j + 3, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 6, j + 3, k + 8, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 6, j + 3, k + 9, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 4, k + 2, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 6, j + 4, k + 3, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 4, k + 4, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 6, j + 4, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 6, j + 4, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 6, j + 4, k + 8, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 6, j + 4, k + 9, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 4, k + 10, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 6, j + 5, k + 1, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 6, j + 5, k + 2, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 6, j + 5, k + 4, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 6, j + 5, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 6, j + 5, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 6, j + 5, k + 8, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 6, j + 5, k + 10, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 6, j + 5, k + 11, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 6, j + 6, k + 4, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 6, j + 6, k + 5, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 6, k + 6, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 6, j + 6, k + 7, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 6, k + 8, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 6, j + 7, k + 4, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 6, j + 7, k + 5, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 7, k + 6, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 6, j + 7, k + 7, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 7, k + 8, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 6, j + 8, k + 3, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 6, j + 8, k + 4, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 6, j + 8, k + 5, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 6, j + 8, k + 6, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 6, j + 8, k + 7, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 6, j + 8, k + 8, b, FrackerBlocks.HUB.ordinal());
		array.setBlock(i + 6, j + 9, k + 3, b, FrackerBlocks.POWER.ordinal());
		//array.setBlock(i + 6, j + 9, k + 6, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 6, j + 10, k + 3, b, FrackerBlocks.POWER.ordinal());
		array.setBlock(i + 7, j + 0, k + 0, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 7, j + 0, k + 1, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 7, j + 0, k + 2, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 7, j + 0, k + 4, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 7, j + 0, k + 5, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 7, j + 0, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 7, j + 0, k + 7, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 7, j + 0, k + 8, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 7, j + 0, k + 10, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 7, j + 0, k + 11, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 7, j + 0, k + 12, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 7, j + 1, k + 4, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 7, j + 1, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 1, k + 6, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 1, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 1, k + 8, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 7, j + 2, k + 4, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 7, j + 2, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 2, k + 6, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 2, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 2, k + 8, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 7, j + 3, k + 4, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 7, j + 3, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 3, k + 6, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 3, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 3, k + 8, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 7, j + 4, k + 4, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 4, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 4, k + 6, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 4, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 4, k + 8, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 5, k + 4, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 7, j + 5, k + 5, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 5, k + 6, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 5, k + 7, b, FrackerBlocks.TUBE.ordinal());
		array.setBlock(i + 7, j + 5, k + 8, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 7, j + 6, k + 4, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 6, k + 5, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 7, j + 6, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 7, j + 6, k + 7, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 7, j + 6, k + 8, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 7, k + 4, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 7, k + 5, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 7, j + 7, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 7, j + 7, k + 7, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 7, j + 7, k + 8, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 8, k + 4, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 7, j + 8, k + 5, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 7, j + 8, k + 6, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 7, j + 8, k + 7, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 7, j + 8, k + 8, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 7, j + 9, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 9, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 10, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 10, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 11, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 11, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 12, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 12, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 13, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 13, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 14, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 7, j + 14, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 8, j + 0, k + 3, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 8, j + 0, k + 4, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 8, j + 0, k + 5, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 8, j + 0, k + 7, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 8, j + 0, k + 8, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 8, j + 0, k + 9, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 8, j + 1, k + 5, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 8, j + 1, k + 6, b, FrackerBlocks.FLUIDIN.ordinal());
		array.setBlock(i + 8, j + 1, k + 7, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 8, j + 2, k + 5, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 8, j + 2, k + 6, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 8, j + 2, k + 7, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 8, j + 3, k + 5, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 8, j + 3, k + 6, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 8, j + 3, k + 7, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 8, j + 4, k + 4, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 8, j + 4, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 8, j + 4, k + 6, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 8, j + 4, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 8, j + 4, k + 8, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 8, j + 5, k + 4, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 8, j + 5, k + 5, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 8, j + 5, k + 6, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 8, j + 5, k + 7, b, FrackerBlocks.ORANGE.ordinal());
		array.setBlock(i + 8, j + 5, k + 8, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 8, j + 6, k + 4, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 8, j + 6, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 8, j + 6, k + 6, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 8, j + 6, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 8, j + 6, k + 8, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 8, j + 7, k + 5, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 8, j + 7, k + 6, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 8, j + 7, k + 7, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 8, j + 8, k + 5, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 8, j + 8, k + 6, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 8, j + 8, k + 7, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 9, j + 0, k + 4, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 9, j + 0, k + 8, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 9, j + 3, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 9, j + 4, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 10, j + 0, k + 5, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 10, j + 0, k + 7, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 10, j + 1, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 10, j + 4, k + 6, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 10, j + 5, k + 6, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 11, j + 0, k + 5, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 11, j + 0, k + 6, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 11, j + 0, k + 7, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 11, j + 1, k + 6, b, FrackerBlocks.GRAY.ordinal());
		array.setBlock(i + 11, j + 5, k + 6, b, FrackerBlocks.SILVER.ordinal());
		array.setBlock(i + 12, j + 0, k + 5, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 12, j + 0, k + 6, b, FrackerBlocks.DARK.ordinal());
		array.setBlock(i + 12, j + 0, k + 7, b, FrackerBlocks.DARK.ordinal());

		return array;
	}

}
