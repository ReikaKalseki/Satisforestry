package Reika.Satisforestry.API;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class SFAPI {

	public static PinkForestBiomeHandler biomeHandler = new DummyPlaceholder();
	public static PinkTreeHandler treeHandler = new DummyPlaceholder();
	public static PinkForestCaveHandler caveHandler = new DummyPlaceholder();
	public static PinkForestResourceNodeHandler resourceNodeHandler = new DummyPlaceholder();

	public static interface PinkForestBiomeHandler {

		public boolean isPinkForest(BiomeGenBase b);

		public int getBaseTerrainHeight(BiomeGenBase b);

		public int getTrueTopAt(World world, int x, int z);

	}

	public static interface PinkTreeHandler {

		public PinkTreeType[] getTypes();

		public boolean placeTree(World world, Random rand, int x, int y, int z, PinkTreeType type);

		public PinkTreeType getTypeFromLog(IBlockAccess world, int x, int y, int z);

		public PinkTreeType getTypeFromLeaves(IBlockAccess world, int x, int y, int z);

	}

	public static interface PinkForestCaveHandler {

		public boolean isInCave(World world, double x, double y, double z);

		public boolean isSpecialCaveBlock(Block b);

	}

	public static interface PinkForestResourceNodeHandler {

		public ItemStack generateRandomResourceFromNode(TileEntity node, Random rand, boolean manualMining);

		public HashMap<ItemStack, Double> getPotentialItemsHere(TileEntity node);

		public void registerCustomNodeEffect(String name, NodeEffectCallback eff);

	}

	private static class DummyPlaceholder implements PinkForestBiomeHandler, PinkForestResourceNodeHandler, PinkForestCaveHandler, PinkTreeHandler {

		@Override
		public PinkTreeType[] getTypes() {
			return new PinkTreeType[0];
		}

		@Override
		public boolean placeTree(World world, Random rand, int x, int y, int z, PinkTreeType type) {
			return false;
		}

		@Override
		public PinkTreeType getTypeFromLog(IBlockAccess world, int x, int y, int z) {
			return null;
		}

		@Override
		public PinkTreeType getTypeFromLeaves(IBlockAccess world, int x, int y, int z) {
			return null;
		}

		@Override
		public boolean isInCave(World world, double x, double y, double z) {
			return false;
		}

		@Override
		public boolean isSpecialCaveBlock(Block b) {
			return false;
		}

		@Override
		public ItemStack generateRandomResourceFromNode(TileEntity node, Random rand, boolean manualMining) {
			return null;
		}

		@Override
		public HashMap<ItemStack, Double> getPotentialItemsHere(TileEntity node) {
			return new HashMap();
		}

		@Override
		public void registerCustomNodeEffect(String name, NodeEffectCallback eff) {

		}

		@Override
		public boolean isPinkForest(BiomeGenBase b) {
			return false;
		}

		@Override
		public int getBaseTerrainHeight(BiomeGenBase b) {
			return 0;
		}

		@Override
		public int getTrueTopAt(World world, int x, int z) {
			return 0;
		}

	}

}
