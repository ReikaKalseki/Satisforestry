package Reika.Satisforestry.Biome.Generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Data.BlockStruct.AbstractSearch.PropagationCondition;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.BlockArray;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.SFBlocks;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.BiomePinkForest.BiomeSection;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Biome.DecoratorPinkForest.OreClusterType;
import Reika.Satisforestry.Biome.DecoratorPinkForest.OreSpawnLocation;
import Reika.Satisforestry.Blocks.BlockTerrain.TerrainType;

public class WorldGenPonds extends WorldGenerator {

	private final boolean forceGeneration;

	public WorldGenPonds(boolean force) {
		forceGeneration = force;
	}

	@Override
	public boolean generate(World world, Random rand, int x0, int y0, int z0) {
		if (!forceGeneration && Satisforestry.pinkforest.getSubBiome(world, x0, z0) != BiomeSection.SWAMP)
			return false;

		for (int i = 0; i < 4; i++) {
			int x = ReikaRandomHelper.getRandomPlusMinus(x0, 6, rand);
			int z = ReikaRandomHelper.getRandomPlusMinus(z0, 6, rand);
			int y = DecoratorPinkForest.getTrueTopAt(world, x, z);

			if (!isValidBlock(world, x, y, z))
				continue;

			Coordinate root = new Coordinate(x, y, z);

			final int rx = ReikaRandomHelper.getRandomBetween(6, 12, rand);
			final int rz = ReikaRandomHelper.getRandomBetween(6, 12, rand);

			PropagationCondition prop = new PropagationCondition() {

				@Override
				public boolean isValidLocation(World world, int x, int y, int z, Coordinate from) {
					return isValidBlock(world, x, y, z) && ReikaMathLibrary.isPointInsideEllipse(x-root.xCoord, 0, z-root.zCoord, rx, 1, rz);
				}

			};

			BlockArray arr = new BlockArray();
			arr.iterativeAddCallbackWithBounds(world, x0, y, z0, x-rx, y-2, z-rz, x+rx, y+20, z+rz, prop);

			Set<Coordinate> blocks = arr.keySet();

			if (!blocks.isEmpty() && arr.getMaxY() <= y+7) {
				HashMap<Coordinate, Integer> top = new HashMap();
				double averageTopY = 0;
				for (Coordinate c : blocks) {
					Coordinate key = c.to2D();
					Coordinate above = c.offset(0, 1, 0);
					Coordinate below = c.offset(0, -1, 0);
					if (!blocks.contains(above)) {
						top.put(key, c.yCoord);
						averageTopY += c.yCoord;
					}
				}
				averageTopY /= top.size();
				HashSet<Coordinate> footprint = new HashSet();
				for (Entry<Coordinate, Integer> c : top.entrySet()) {
					int dy = c.getValue();
					if (dy <= averageTopY+1) {
						footprint.add(c.getKey().setY(dy));
					}
				}
				HashSet<Coordinate> adj = new HashSet();
				HashSet<Coordinate> cut = new HashSet();
				HashMap<Coordinate, Integer> floor = new HashMap();
				HashMap<Coordinate, Integer> waterLevel = new HashMap();
				double dwx = ReikaRandomHelper.getRandomBetween(0.67, 1.125, rand);
				double dwz = ReikaRandomHelper.getRandomBetween(0.67, 1.125, rand);
				for (Coordinate c : footprint) {
					double dx = c.xCoord-x;
					double dz = c.zCoord-z;
					double dist = ReikaMathLibrary.py3d(dx/dwx, 0, dz/dwz);
					int depth = 0;
					if (dist < 4)
						depth = 2;
					else if (dist < 7)
						depth = 1;
					int minY = Math.max(c.yCoord-depth, (int)(averageTopY-3));
					Coordinate key = c.to2D();
					waterLevel.put(key, c.yCoord);
					for (int dy = minY; dy <= c.yCoord; dy++) {
						Coordinate c2 = c.setY(dy);
						cutBlock(world, c2);
						adj.addAll(c2.getAdjacentCoordinates());
						cut.add(c2);
						floor.put(key, minY-1);
					}
				}
				adj.removeAll(cut);
				HashSet<Coordinate> seams = new HashSet();
				for (Coordinate c2 : adj) {
					Integer floorAt = floor.get(c2.to2D());
					if (floorAt == null || floorAt.intValue() != c2.yCoord) {
						if (c2.softBlock(world) || !DecoratorPinkForest.isTerrain(world, c2.xCoord, c2.yCoord, c2.zCoord))
							continue;
					}
					int dy = c2.yCoord;
					cutBlock(world, c2);
					c2.setBlock(world, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal());
					boolean flag = false;
					while (ReikaWorldHelper.softBlocks(world, c2.xCoord, c2.yCoord-1, c2.zCoord)) {
						c2 = c2.offset(0, -1, 0);
						c2.setBlock(world, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal());
						flag = true;
					}
					if (flag) {
						c2 = c2.to2D();
						if (top.containsKey(c2.offset(0, 0, 1)) && top.containsKey(c2.offset(0, 0, -1)) && top.containsKey(c2.offset(1, 0, 0)) && top.containsKey(c2.offset(-1, 0, 0))) {
							seams.add(c2.setY(dy));
						}
					}
				}
				HashSet<Coordinate> channels = new HashSet();
				for (int i2 = 0; i2 < seams.size()/4; i2++) {
					Coordinate c2 = ReikaJavaLibrary.getRandomCollectionEntry(rand, seams);
					c2.setBlock(world, Blocks.flowing_water);
					channels.add(c2);
					for (Coordinate c3 : c2.getAdjacentCoordinates()) {
						if (c3.yCoord == c2.yCoord && DecoratorPinkForest.isTerrain(world, c3.xCoord, c3.yCoord, c3.zCoord)) {
							c3.setBlock(world, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal());
						}
					}
				}
				for (Coordinate c2 : channels) {
					for (Coordinate c3 : c2.getAdjacentCoordinates()) {
						if (c3.getBlock(world) == SFBlocks.TERRAIN.getBlockInstance()) {
							if (c3.offset(1, 0, 0).getBlock(world).getMaterial() == Material.water && c3.offset(-1, 0, 0).getBlock(world).getMaterial() == Material.water) {
								if (c3.offset(0, 0, 1).getBlock(world).getMaterial() == Material.water && c3.offset(0, 0, -1).getBlock(world).getMaterial() == Material.water) {
									c3.setBlock(world, Blocks.flowing_water);
								}
							}
						}
					}
				}
				Coordinate ctr = new Coordinate(x, 0, z);
				if (floor.containsKey(ctr)) {
					int my = floor.get(ctr)+1;
					int h = ReikaRandomHelper.getRandomBetween(1, 3, rand);
					int maxy = h+waterLevel.get(ctr);
					boolean thick = rand.nextInt(3) == 0;
					for (int dy = my; dy <= maxy; dy++) {
						int l = dy-my;
						world.setBlock(ctr.xCoord, dy, ctr.zCoord, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
						world.setBlock(ctr.xCoord+1, dy, ctr.zCoord, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
						world.setBlock(ctr.xCoord-1, dy, ctr.zCoord, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
						world.setBlock(ctr.xCoord, dy, ctr.zCoord+1, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
						world.setBlock(ctr.xCoord, dy, ctr.zCoord-1, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
						if ((thick || l <= h/3) && dy < maxy-1) {
							world.setBlock(ctr.xCoord+1, dy, ctr.zCoord+1, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
							world.setBlock(ctr.xCoord-1, dy, ctr.zCoord+1, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
							world.setBlock(ctr.xCoord+1, dy, ctr.zCoord-1, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
							world.setBlock(ctr.xCoord-1, dy, ctr.zCoord-1, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
						}
					}
					world.setBlock(ctr.xCoord, maxy+1, ctr.zCoord, SFBlocks.TERRAIN.getBlockInstance(), TerrainType.PONDROCK.ordinal(), 2);
				}

				OreSpawnLocation.PONDS.setRNG(rand);
				int n = ReikaRandomHelper.getRandomBetween(3, 7, rand);
				double da = 360D/n;
				for (int i2 = 0; i2 < n; i2++) {
					double ang = da*i2;
					double rang = Math.toRadians(ReikaRandomHelper.getRandomPlusMinus(ang, da/2.4D, rand));
					double dx = Math.cos(rang);
					double dz = Math.sin(rang);
					for (int tries = 0; tries < 3; tries++) {
						double dr = ReikaRandomHelper.getRandomBetween(4D, 9D);
						Coordinate c2 = new Coordinate(ctr.xCoord+dx*dr, 0, ctr.zCoord+dz*dr);
						Integer level = waterLevel.get(c2);
						if (level != null) {
							OreClusterType ore = DecoratorPinkForest.generateOreClumpAt(world, c2.xCoord, level, c2.zCoord, rand, OreSpawnLocation.PONDS, 2);
							if (ore != null) {
								world.setBlock(c2.xCoord, level+1, c2.zCoord, ore.oreBlock.blockID, ore.oreBlock.metadata, 2);
								break;
							}
						}
					}
				}
				//ReikaJavaLibrary.pConsole("Generated a swamp pond at "+x+", "+y+", "+z);
				if (rand.nextInt(5) >= 2)
					return true;
			}
		}

		return false;
	}

	private static void cutBlock(World world, Coordinate c) {
		cutBlock(world, c.xCoord, c.yCoord, c.zCoord);
	}

	private static void cutBlock(World world, int x, int y, int z) {
		Block above = world.getBlock(x, y+1, z);
		if (above.getMaterial() == Material.plants || above instanceof BlockBush)
			world.setBlock(x, y+1, z, Blocks.air, 0, 2);
		world.setBlock(x, y, z, Blocks.water, 0, 3);
		if (!above.canBlockStay(world, x, y+1, z)) {
			world.setBlock(x, y+1, z, Blocks.air, 0, 2);
		}
	}

	private static boolean isValidBlock(World world, int x, int y, int z) {
		return DecoratorPinkForest.isTerrain(world, x, y, z) && hasSolidOnAllSides(world, x, y, z);
	}

	private static boolean hasSolidOnAllSides(World world, int x, int y, int z) {
		for (int i = 0; i < 6; i++) {
			ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
			if (dir == ForgeDirection.UP)
				continue;
			int dx = x+dir.offsetX;
			int dy = y+dir.offsetY;
			int dz = z+dir.offsetZ;
			Block b = world.getBlock(dx, dy, dz);
			if (b.getMaterial().isLiquid())
				continue;
			if (ReikaWorldHelper.softBlocks(world, dx, dy, dz) || !b.getMaterial().blocksMovement())
				return false;
		}
		return true;
	}

}
