package Reika.Satisforestry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import Reika.Satisforestry.API.NodeEffectCallback;
import Reika.Satisforestry.API.PinkTreeType;
import Reika.Satisforestry.API.SFAPI;
import Reika.Satisforestry.API.SFAPI.PinkForestBiomeHandler;
import Reika.Satisforestry.API.SFAPI.PinkForestCaveHandler;
import Reika.Satisforestry.API.SFAPI.PinkForestResourceNodeHandler;
import Reika.Satisforestry.API.SFAPI.PinkTreeHandler;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Biome.Biomewide.BiomewideFeatureGenerator;
import Reika.Satisforestry.Biome.Biomewide.UraniumCave;
import Reika.Satisforestry.Biome.Generator.PinkTreeGeneratorBase.PinkTreeTypes;
import Reika.Satisforestry.Blocks.BlockResourceNode.TileResourceNode;
import Reika.Satisforestry.Config.ResourceItem;

public class APIObjects {

	public static void load() {
		SFAPI.biomeHandler = new SFBiomeHandler();
		SFAPI.treeHandler = new SFTreeHandler();
		SFAPI.caveHandler = new SFCaveHandler();
		SFAPI.resourceNodeHandler = new SFNodeHandler();
	}

	private static class SFBiomeHandler implements PinkForestBiomeHandler {

		@Override
		public boolean isPinkForest(BiomeGenBase b) {
			return Satisforestry.isPinkForest(b);
		}

		@Override
		public int getBaseTerrainHeight(BiomeGenBase b) {
			return 112;
		}

		@Override
		public int getTrueTopAt(World world, int x, int z) {
			return DecoratorPinkForest.getTrueTopAt(world, x, z);
		}

	}

	private static class SFTreeHandler implements PinkTreeHandler {

		@Override
		public boolean placeTree(World world, Random rand, int x, int y, int z, PinkTreeType type) {
			return ((PinkTreeTypes)type).constructTreeGenerator().generate(world, rand, x, y, z);
		}

		@Override
		public PinkTreeType getTypeFromLog(IBlockAccess world, int x, int y, int z) {
			return PinkTreeTypes.getLogType(world, x, y, z);
		}

		@Override
		public PinkTreeType getTypeFromLeaves(IBlockAccess world, int x, int y, int z) {
			return PinkTreeTypes.getLeafType(world, x, y, z);
		}

		@Override
		public PinkTreeType[] getTypes() {
			return Arrays.copyOf(PinkTreeTypes.list, PinkTreeTypes.list.length);
		}

	}

	private static class SFCaveHandler implements PinkForestCaveHandler {

		public boolean isInCave(World world, double x, double y, double z) {
			return BiomewideFeatureGenerator.instance.isInCave(world, x, y, z);
		}

		@Override
		public boolean isSpecialCaveBlock(Block b) {
			return UraniumCave.instance.isSpecialCaveBlock(b);
		}

	}

	private static class SFNodeHandler implements PinkForestResourceNodeHandler {

		@Override
		public ItemStack generateRandomResourceFromNode(TileEntity node, Random rand, boolean manualMining) {
			return ((TileResourceNode)node).getRandomNodeItem(manualMining);
		}

		@Override
		public HashMap<ItemStack, Double> getPotentialItemsHere(TileEntity node) {
			ResourceItem item = ((TileResourceNode)node).getResource();
			return item.getItemSet(((TileResourceNode)node).getPurity());
		}

		public void registerCustomNodeEffect(String name, NodeEffectCallback eff) {
			ResourceItem.EffectTypes.addCustomCallback(name, eff);
		}

	}

}
