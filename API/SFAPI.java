package Reika.Satisforestry.API;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
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
	//public static PinkForestEntityHandler entityHandler = new DummyPlaceholder();
	public static PinkForestSpawningHandler spawningHandler = new DummyPlaceholder();
	public static SFLookups genericLookups = new DummyPlaceholder();

	public static interface PinkForestBiomeHandler {

		public BiomeGenBase getBiome();

		public boolean isPinkForest(BiomeGenBase b);

		/** Returns the base (offset from base MC) height of the terrain in the biome. */
		public int getBaseTerrainHeight(BiomeGenBase b);

		/** Like getTopSolidOrLiquid, but passes through the pink tree canopy. Returns the coord of the ground, not the lowest air. */
		public int getTrueTopAt(World world, int x, int z);

	}

	public static interface PinkTreeHandler {

		/** Get the values() of the PinkTreeTypes enum. */
		public PinkTreeType[] getTypes();

		public boolean placeTree(World world, Random rand, int x, int y, int z, PinkTreeType type);

		public PinkTreeType getTypeFromLog(IBlockAccess world, int x, int y, int z);

		public PinkTreeType getTypeFromLeaves(IBlockAccess world, int x, int y, int z);

	}
	/*
	public static interface PinkForestEntityHandler {

		public Collection<Class<? extends EntityLiving>> getEntityTypes();

	}*/

	public static interface PinkForestSpawningHandler {

		/** Returns the spawnpoint of this entity, if one exists. Entities from "spawn points" are spawned to guard an area and are tied to that region.
		 They are also always hostile, and will infinitely respawn until the player has killed all of them. See {@link PointSpawnLocation} for details. */
		public PointSpawnLocation getEntitySpawnPoint(EntityLiving e);

		public PointSpawnLocation getNearestSpawnPoint(EntityPlayer ep, double r);

		public PointSpawnLocation getNearestSpawnPointOfType(EntityPlayer ep, double r, Class<? extends EntityLiving> c);

	}

	public static interface PinkForestCaveHandler {

		public boolean isInCave(World world, double x, double y, double z);

		/** Whether the given block is a special block used to form the structure of the cave or its functionality. */
		public boolean isSpecialCaveBlock(Block b);

		public double getDistanceToCaveCenter(World world, double x, double y, double z);

	}

	public static interface PinkForestResourceNodeHandler {

		/** Generate a random harvest result from a resource node TE. */
		public ItemStack generateRandomResourceFromNode(TileEntity node, Random rand, boolean manualMining);

		/** Get the overall WeightedRandom harvest table, before things like speed or manual modifiers. */
		public HashMap<ItemStack, Double> getPotentialItemsHere(TileEntity node);

		public void registerCustomNodeEffect(String name, NodeEffectCallback eff);

	}

	public static interface SFLookups {

		/** Returns the custom Thaumcraft aspect added by SF, if TC is installed. */
		public Object getAspect();

		public Item getPaleberries();

		public Class<? extends EntityLiving> getSpitterClass();

		public Class<? extends EntityLiving> getStingerClass();

		public Class<? extends EntityLiving> getDoggoClass();

	}

	private static class DummyPlaceholder implements PinkForestBiomeHandler, PinkForestResourceNodeHandler, PinkForestCaveHandler, PinkTreeHandler, SFLookups, PinkForestSpawningHandler {

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

		@Override
		public Object getAspect() {
			return null;
		}

		@Override
		public Item getPaleberries() {
			return null;
		}

		@Override
		public BiomeGenBase getBiome() {
			return null;
		}

		@Override
		public PointSpawnLocation getEntitySpawnPoint(EntityLiving e) {
			return null;
		}

		@Override
		public PointSpawnLocation getNearestSpawnPoint(EntityPlayer ep, double r) {
			return null;
		}

		@Override
		public double getDistanceToCaveCenter(World world, double x, double y, double z) {
			return 0;
		}

		@Override
		public PointSpawnLocation getNearestSpawnPointOfType(EntityPlayer ep, double r, Class<? extends EntityLiving> c) {
			return null;
		}

		@Override
		public Class<? extends EntityLiving> getSpitterClass() {
			return null;
		}

		@Override
		public Class<? extends EntityLiving> getStingerClass() {
			return null;
		}

		@Override
		public Class<? extends EntityLiving> getDoggoClass() {
			return null;
		}

	}

}
