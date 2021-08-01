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

	public static PinkForestBiomeHandler biomeHandler;
	public static PinkTreeHandler treeHandler;
	public static PinkForestCaveHandler caveHandler;
	public static PinkForestResourceNodeHandler resourceNodeHandler;

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

		public ItemStack generateRandomResourceFromNode(TileEntity node, Random rand);

		public HashMap<ItemStack, Double> getPotentialItemsHere(TileEntity node);

		public void registerCustomNodeEffect(String name, NodeEffectCallback eff);

	}

}
